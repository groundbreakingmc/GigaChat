package com.github.groundbreakingmc.gigachat.commands.privatemessages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.*;
import com.github.groundbreakingmc.gigachat.utils.StringValidator;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.colorizer.ColorizerUtils;
import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import com.github.groundbreakingmc.gigachat.utils.configvalues.PrivateMessagesValues;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class ReplyExecutor implements TabExecutor {

    private final GigaChat plugin;
    private final PrivateMessagesValues pmValues;
    private final Messages messages;
    private final ConsoleCommandSender consoleSender;
    private final CooldownCollections cooldownCollections;
    private final PmSoundsCollection pmSoundsCollection;

    public ReplyExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.pmValues = plugin.getPmValues();
        this.messages = plugin.getMessages();
        this.consoleSender = plugin.getServer().getConsoleSender();
        this.cooldownCollections = plugin.getCooldownCollections();
        this.pmSoundsCollection = plugin.getPmSoundsCollection();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof final Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("gigachat.command.reply")) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(this.messages.getReplyUsageError());
            return true;
        }

        if (this.hasCooldown(playerSender)) {
            this.sendMessageHasCooldown(playerSender);
            return true;
        }

        final UUID senderUUID = playerSender.getUniqueId();
        final UUID recipientUUID = ReplyCollection.getRecipientName(senderUUID);
        if (recipientUUID == null) {
            sender.sendMessage(this.messages.getNobodyToAnswer());
            return true;
        }

        final Player recipient = Bukkit.getPlayer(recipientUUID);
        if (recipient == null || !playerSender.canSee(recipient) || PlayerUtils.isVanished(recipient)) {
            sender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        if (!sender.hasPermission("gigachat.bypass.ignore.private")) {
            if (isIgnored(senderUUID, recipientUUID)) {
                sender.sendMessage(this.messages.getRecipientIgnoresSender());
                return true;
            }

            if (isIgnored(recipientUUID, senderUUID)) {
                sender.sendMessage(this.messages.getSenderIgnoresRecipient());
                return true;
            }
        }

        final String message = this.getValidMessage(playerSender, this.getMessage(playerSender, args));
        if (message == null) {
            return true;
        }

        final String senderPrefix = this.getPrefix(playerSender);
        final String senderSuffix = this.getSuffix(playerSender);
        final String recipientPrefix = this.getPrefix(recipient);
        final String recipientSuffix = getSuffix(recipient);

        final String formattedMessageForSender = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getSenderFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);
        final String formattedMessageForRecipient = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getRecipientFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);
        final String formattedMessageForConsole = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getConsoleFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);
        final String formattedMessageForSocialSpy = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getSocialSpyFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);

        ReplyCollection.add(recipientUUID, senderUUID);
        this.processLogs(formattedMessageForConsole);
        SocialSpyCollection.sendAll(playerSender, recipient, formattedMessageForSocialSpy);

        playerSender.sendMessage(formattedMessageForSender);
        recipient.sendMessage(formattedMessageForRecipient);

        this.playSound(recipient);

        this.plugin.getPmLogger().log(() ->
                "[PM-REPLY] [" + sender.getName() + " -> " + recipient.getName() + "] " + ColorizerUtils.getClear(message)
        );

        return true;
    }

    private boolean hasCooldown(final Player playerSender) {
        return this.cooldownCollections.hasCooldown(playerSender, "gigachat.bypass.cooldown.socialspy", this.cooldownCollections.getPrivateCooldowns());
    }

    private void sendMessageHasCooldown(final Player sender) {
        final long timeLeftInMillis = this.cooldownCollections.getPrivateCooldowns()
                .get(sender.getUniqueId()) - System.currentTimeMillis();
        final int result = (int) (this.pmValues.getPmCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        sender.sendMessage(message);
    }

    private boolean isIgnored(final UUID ignoringUUID, final UUID ignoredUUID) {
        return IgnoreCollections.isIgnoredPrivate(ignoringUUID, ignoredUUID);
    }

    private String getPrefix(final Player player) {
        return this.plugin.getChat().getPlayerPrefix(player);
    }

    private String getSuffix(final Player player) {
        return this.plugin.getChat().getPlayerSuffix(player);
    }

    public String getFormattedMessage(final Player player, final Player recipient,
                                      final String message, final String format,
                                      final String senderPrefix, final String senderSuffix,
                                      final String recipientPrefix, final String recipientSuffix) {
        final String formatted = this.pmValues.getFormatColorizer().colorize(
                Utils.replacePlaceholders(
                        player,
                        format.replace("{from-prefix}", senderPrefix)
                                .replace("{from-name}", player.getName())
                                .replace("{from-suffix}", senderSuffix)
                                .replace("{to-prefix}", recipientPrefix)
                                .replace("{to-name}", recipient.getName())
                                .replace("{to-suffix}", recipientSuffix)
                )
        );

        return formatted.replace("{message}", message);
    }

    private String getMessage(final Player sender, final String[] args) {
        return this.pmValues.getMessageColorizer().colorize(sender, String.join(" ", args).trim());
    }

    private void playSound(final Player recipient) {
        final Sound sound = this.pmSoundsCollection.getSound(recipient.getUniqueId());
        if (this.pmValues.isSoundEnabled()) {
            final Location recipientLocation = recipient.getLocation();
            final float volume = this.pmValues.getSoundVolume();
            final float pitch = this.pmValues.getSoundPitch();
            recipient.playSound(recipientLocation, sound, volume, pitch);
        }
    }

    private String getValidMessage(final Player sender, String message) {
        final StringValidator stringValidator = this.pmValues.getStringValidator();
        if (stringValidator.hasBlockedChars(message)) {
            if (this.pmValues.isCharsValidatorBlockMessage()) {
                final String denyMessage = this.messages.getCharsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySoundCharsCheckFailed(sender);
                return null;
            }

            message = stringValidator.getFormattedCharsMessage(message);
        }

        if (stringValidator.isUpperCasePercentageExceeded(message)) {
            if (this.pmValues.isCapsValidatorBlockMessageSend()) {
                final String denyMessage = this.messages.getCharsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySoundCapsCheckFailed(sender);
                return null;
            }

            message = message.toLowerCase();
        }

        if (stringValidator.hasBlockedWords(message)) {
            if (this.pmValues.isWordsValidatorBlockMessageSend()) {
                final String denyMessage = this.messages.getWordsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                this.playDenySoundWordsCheckFailed(sender);
                return null;
            }

            message = stringValidator.getFormattedWordsMessage(message);
        }

        return message;
    }

    private void playDenySoundCharsCheckFailed(final Player messageSender) {
        if (this.pmValues.isCharsValidatorDenySoundEnabled()) {
            final Location location = messageSender.getLocation();
            final Sound sound = this.pmValues.getTextValidatorDenySound();
            final float volume = this.pmValues.getTextValidatorDenySoundVolume();
            final float pitch = this.pmValues.getTextValidatorDenySoundPitch();

            messageSender.playSound(location, sound, volume, pitch);
        }
    }

    private void playDenySoundCapsCheckFailed(final Player messageSender) {
        if (this.pmValues.isCapsValidatorDenySoundEnabled()) {
            final Location location = messageSender.getLocation();
            final Sound sound = this.pmValues.getCapsValidatorDenySound();
            final float volume = this.pmValues.getCapsValidatorDenySoundVolume();
            final float pitch = this.pmValues.getCapsValidatorDenySoundPitch();

            messageSender.playSound(location, sound, volume, pitch);
        }
    }

    private void playDenySoundWordsCheckFailed(final Player messageSender) {
        if (this.pmValues.isWordsValidatorDenySoundEnabled()) {
            final Location location = messageSender.getLocation();
            final Sound sound = this.pmValues.getWordsValidatorDenySound();
            final float volume = this.pmValues.getWordsValidatorDenySoundVolume();
            final float pitch = this.pmValues.getWordsValidatorDenySoundPitch();

            messageSender.playSound(location, sound, volume, pitch);
        }
    }

    private void processLogs(final String formattedMessage) {
        if (this.pmValues.isPrintLogsToConsole()) {
            this.consoleSender.sendMessage(formattedMessage);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
