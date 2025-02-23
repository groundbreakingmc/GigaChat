package com.github.groundbreakingmc.gigachat.commands.privatemessages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.*;
import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.gigachat.database.Database;
import com.github.groundbreakingmc.gigachat.utils.HoverUtils;
import com.github.groundbreakingmc.gigachat.utils.StringValidator;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.afk.AfkChecker;
import com.github.groundbreakingmc.gigachat.utils.colorizer.ColorizerUtils;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.PermissionColorizer;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.PrivateMessagesColorizer;
import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import com.github.groundbreakingmc.gigachat.utils.configvalues.PrivateMessagesValues;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class PrivateMessageExecutor implements TabExecutor {

    private final GigaChat plugin;
    private final Messages messages;
    private final Database database;
    private final PrivateMessagesValues pmValues;
    private final CooldownCollections cooldownCollections;
    private final PmSoundsCollection pmSoundsCollection;
    private final DisabledPrivateMessagesCollection disabled;
    private final ConsoleCommandSender consoleSender;

    public final PermissionColorizer messagesColorizer;

    public PrivateMessageExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.database = plugin.getDatabase();
        this.pmValues = plugin.getPmValues();
        this.cooldownCollections = plugin.getCooldownCollections();
        this.pmSoundsCollection = plugin.getPmSoundsCollection();
        this.disabled = plugin.getDisabled();
        this.consoleSender = plugin.getServer().getConsoleSender();

        this.messagesColorizer = new PrivateMessagesColorizer(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("gigachat.command.pm")) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("disable")) {
            return this.processDisable(sender);
        }

        if (args.length < 2) {
            sender.sendMessage(this.messages.getPmUsageError());
            return true;
        }

        final Player recipient = Bukkit.getPlayer(args[0]);

        final boolean isPlayerSender;
        if (recipient == null
                || (isPlayerSender = sender instanceof Player)
                && !((Player) sender).canSee(recipient)
                || PlayerUtils.isVanished(recipient)) {
            sender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        if (recipient == sender) {
            sender.sendMessage(this.messages.getCannotChatWithHimself());
            return true;
        }

        if (isPlayerSender && this.hasCooldown((Player) sender)) {
            this.sendMessageHasCooldown((Player) sender);
            return true;
        }

        final UUID recipientUUID = recipient.getUniqueId();

        if (isPlayerSender && !sender.hasPermission("gigachat.bypass.ignore")) {
            final UUID senderUUID = ((Player) sender).getUniqueId();
            if (IgnoreCollections.isIgnoredPrivate(recipientUUID, senderUUID)) {
                sender.sendMessage(this.messages.getRecipientIgnoresSender());
                return true;
            }

            if (IgnoreCollections.isIgnoredPrivate(senderUUID, recipientUUID)) {
                sender.sendMessage(this.messages.getSenderIgnoresRecipient());
                return true;
            }

            if (this.disabled.contains(recipientUUID)) {
                sender.sendMessage(this.messages.getHasDisabledPm());
                return true;
            }
        }

        final AfkChecker afkChecker = this.plugin.getAfkChecker();
        if (afkChecker != null && afkChecker.isAfk(recipient)) {
            sender.sendMessage(this.messages.getIsAfk());
            return true;
        }

        String message = getMessage(sender, args, isPlayerSender);

        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            message = this.getValidMessage(playerSender, message);
            if (message == null) {
                return true;
            }
        }

        this.process(sender, recipient, recipientUUID, isPlayerSender, message);

        return true;
    }

    private boolean hasCooldown(final Player sender) {
        return this.cooldownCollections.hasCooldown(sender, "gigachat.bypass.cooldown.pm", this.cooldownCollections.getPrivateCooldowns());
    }

    private void sendMessageHasCooldown(final Player sender) {
        final long timeLeftInMillis = this.cooldownCollections.getPrivateCooldowns()
                .get(sender.getUniqueId()) - System.currentTimeMillis();
        final int result = (int) (this.pmValues.getPmCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        sender.sendMessage(message);
    }

    private boolean processDisable(final CommandSender sender) {
        if (!(sender instanceof final Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        final UUID senderUUID = playerSender.getUniqueId();
        if (this.disabled.contains(senderUUID)) {
            return this.process(
                    this.disabled::remove,
                    playerSender, senderUUID,
                    Database.REMOVE_PLAYER_FROM_DISABLED_PRIVATE_MESSAGES,
                    this.messages.getPmDisabled(),
                    "enabled"
            );
        }

        return this.process(
                this.disabled::add,
                playerSender, senderUUID,
                Database.ADD_PLAYER_TO_DISABLED_PRIVATE_MESSAGES,
                this.messages.getPmEnabled(),
                "disabled"
        );
    }

    private boolean process(final Consumer<UUID> function,
                            final Player sender, final UUID senderUUID,
                            final String query, final String message,
                            final String mode) {
        function.accept(senderUUID);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (final Connection connection = this.database.getConnection()) {
                this.database.executeUpdateQuery(query, connection, senderUUID.toString());
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        });

        sender.sendMessage(message);

        this.plugin.getCommandLogger().log(() ->
                "[PM] [" + sender.getName() + "] " + mode
        );

        return true;
    }

    private String getValidMessage(final Player sender, String message) {
        if (sender.hasPermission("gigachat.bypass.validator.pm.*")) {
            return message;
        }

        final StringValidator stringValidator = this.pmValues.getStringValidator();
        if (!sender.hasPermission("gigachat.bypass.validator.pm.chars")
                && stringValidator.hasBlockedChars(message)) {
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

        if (!sender.hasPermission("gigachat.bypass.validator.pm.caps")
                && stringValidator.isUpperCasePercentageExceeded(message)) {
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

        if (!sender.hasPermission("gigachat.bypass.validator.pm.words")
                && stringValidator.hasBlockedWords(message)) {
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

    private void process(final CommandSender sender, final Player recipient,
                         final UUID recipientUUID, final boolean isPlayerSender, final String message) {
        final Chat chat = this.plugin.getChat();
        final Hover hover = this.pmValues.getPmHover();
        final Colorizer formatColorizer = this.pmValues.getFormatColorizer();

        final String recipientPrefix = chat.getPlayerPrefix(recipient);
        final String recipientSuffix = chat.getPlayerSuffix(recipient);

        final String formattedMessageForSender;
        final String formattedMessageForRecipient;
        final String formattedMessageForConsole;
        final String formattedMessageForSocialSpy;
        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            final String senderPrefix = chat.getPlayerPrefix(playerSender);
            final String senderSuffix = chat.getPlayerSuffix(playerSender);

            formattedMessageForSender = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getSenderFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);
            formattedMessageForRecipient = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getRecipientFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);
            formattedMessageForConsole = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getConsoleFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);
            formattedMessageForSocialSpy = this.getFormattedMessage(playerSender, recipient, message, this.pmValues.getSocialSpyFormat(), senderPrefix, senderSuffix, recipientPrefix, recipientSuffix);

            final UUID senderUUID = ((Player) sender).getUniqueId();
            ReplyCollection.add(recipientUUID, senderUUID);
            this.processLogs(formattedMessageForConsole);
            SocialSpyCollection.sendAll(
                    playerSender, senderPrefix, senderSuffix,
                    recipient, recipientPrefix, recipientSuffix,
                    this.pmValues.getSpyHover(), formattedMessageForSocialSpy, formatColorizer
            );

            if (hover.isEnabled()) {
                final BaseComponent[] senderComponents = HoverUtils.get(recipient, senderPrefix, senderSuffix, hover, hover.hoverText(), formattedMessageForSender, formatColorizer);
                playerSender.sendMessage(senderComponents);

                final BaseComponent[] recipientComponents = HoverUtils.get(playerSender, senderPrefix, senderSuffix, hover, hover.hoverText(), formattedMessageForRecipient, formatColorizer);
                recipient.sendMessage(recipientComponents);
            }
        } else {
            formattedMessageForSender = this.getFormattedMessage(sender, recipient, message, this.pmValues.getSenderFormat(), recipientPrefix, recipientSuffix);
            formattedMessageForRecipient = this.getFormattedMessage(sender, recipient, message, this.pmValues.getRecipientFormat(), recipientPrefix, recipientSuffix);
        }

        if (!isPlayerSender || !hover.isEnabled()) {
            sender.sendMessage(formattedMessageForSender);
            recipient.sendMessage(formattedMessageForRecipient);
        }

        this.plugin.getPmLogger().log(() ->
                "[PM] [" + sender.getName() + " -> " + recipient.getName() + "] " + ColorizerUtils.getClear(message)
        );

        this.playSound(recipient);
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

    public String getFormattedMessage(final CommandSender sender, final Player recipient,
                                      final String message, final String format,
                                      final String recipientPrefix, final String recipientSuffix) {
        final String formatted = this.pmValues.getFormatColorizer().colorize(
                format.replace("{from-prefix}", "")
                        .replace("{from-name}", sender.getName())
                        .replace("{from-suffix}", "")
                        .replace("{to-prefix}", recipientPrefix)
                        .replace("{to-name}", recipient.getName())
                        .replace("{to-suffix}", recipientSuffix)
        );

        return formatted.replace("{message}", message);
    }

    private String getMessage(final CommandSender sender, final String[] args, final boolean isPlayerSender) {
        if (isPlayerSender) {
            return this.messagesColorizer.colorize((Player) sender, String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
        }

        return this.messagesColorizer.colorize(String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
    }

    private void playSound(final Player recipient) {
        if (this.pmValues.isReceiveSoundEnabled()) {
            final SoundSettings sound = this.pmSoundsCollection.getSound(recipient.getUniqueId());
            PlayerUtils.playSound(recipient, sound);
        }
    }

    private void processLogs(final String formattedMessage) {
        if (this.pmValues.isPrintLogsToConsole()) {
            this.consoleSender.sendMessage(formattedMessage);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final String input = args[0];
            final List<String> completions = new ArrayList<>();

            if (sender instanceof final Player playerSender) {
                final UUID senderUUID = playerSender.getUniqueId();
                for (final Player target : Bukkit.getOnlinePlayers()) {
                    final UUID targetUUID = target.getUniqueId();
                    if (IgnoreCollections.isIgnoredPrivate(senderUUID, targetUUID) || IgnoreCollections.isIgnoredPrivate(targetUUID, senderUUID)) {
                        continue;
                    }

                    final String targetName = target.getName();
                    if (StringUtil.startsWithIgnoreCase(targetName, input) && !PlayerUtils.isVanished(target)) {
                        completions.add(targetName);
                    }
                }

                if (sender.hasPermission("gigachat.disable.pm") && StringUtil.startsWithIgnoreCase("disable", input)) {
                    completions.add("disable");
                }
            } else {
                for (final Player target : Bukkit.getOnlinePlayers()) {
                    final String targetName = target.getName();
                    if (StringUtil.startsWithIgnoreCase(targetName, input)) {
                        completions.add(targetName);
                    }
                }
            }

            return completions;
        }

        return List.of();
    }
}
