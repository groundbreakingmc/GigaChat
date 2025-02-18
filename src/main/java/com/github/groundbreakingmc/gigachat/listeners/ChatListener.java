package com.github.groundbreakingmc.gigachat.listeners;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.commands.main.arguments.DisableServerChatArgument;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.gigachat.utils.HoverUtils;
import com.github.groundbreakingmc.gigachat.utils.StringValidator;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.colorizer.ColorizerUtils;
import com.github.groundbreakingmc.gigachat.utils.configvalues.ChatValues;
import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class ChatListener implements Listener {

    private final GigaChat plugin;
    private final ChatValues chatValues;
    private final Messages messages;

    public ChatListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatValues = plugin.getChatValues();
        this.messages = plugin.getMessages();
    }

    @EventHandler
    public void onMessageSend(final AsyncPlayerChatEvent event) {
        final Player sender = event.getPlayer();

        if (this.isDisabled(sender, event)) {
            return;
        }

        String message = event.getMessage();
        final Chat chat = this.getChat(message);

        if (chat.hasCooldown(sender, this.messages)) {
            event.setCancelled(true);
            return;
        }

        message = this.getValidMessage(sender, message);
        if (message == null) {
            event.setCancelled(true);
            return;
        }

        if (chat != this.chatValues.getDefaultChat()) {
            message = message.substring(1);
        }

        final Set<Player> recipients = chat.getRecipients(sender);

        final String prefix = this.plugin.getChat().getPlayerPrefix(sender);
        final String suffix = this.plugin.getChat().getPlayerSuffix(sender);
        final String groupName = this.plugin.getPerms().getPrimaryGroup(sender);
        final String color = chat.getColor(groupName);

        final String spyFormat = chat.getSpyFormat();
        if (spyFormat != null && !spyFormat.isEmpty()) {
            final Set<Player> spyListeners = chat.getSpyListeners();
            if (!spyListeners.isEmpty()) {
                final Set<Player> spyListenersCopy = new HashSet<>(spyListeners);
                spyListenersCopy.remove(sender);
                this.sendSpy(sender, chat, message, spyFormat, recipients, spyListenersCopy, prefix, suffix, color);
            }
        }

        final String chatFormat = chat.getFormat();
        final String formattedMessage = this.getFormattedMessage(sender, message, chatFormat, prefix, suffix, color);

        final Hover adminHover = chat.getAdminHover();
        if (adminHover.isEnabled()) {
            final Set<Player> adminRecipients = this.getAdminRecipients(recipients);
            this.sendHover(sender, formattedMessage, adminHover, adminRecipients, prefix, suffix, color);
        }

        chat.addCooldown(sender);

        final Hover hover = chat.getHover();
        if (hover.isEnabled()) {
            this.sendHover(sender, formattedMessage, hover, recipients, prefix, suffix, color);
            this.plugin.getServer().getConsoleSender().sendMessage(formattedMessage);
            event.setCancelled(true);
        } else {
            event.getRecipients().clear();
            event.getRecipients().addAll(recipients);
            event.setFormat(formattedMessage);
        }
        if (chat.isNoOneHeard(sender, recipients)) {
            sender.sendMessage(this.messages.getNoOneHear());
        }

        this.log(sender, chat, message);
    }

    private boolean isDisabled(final Player sender, final AsyncPlayerChatEvent event) {
        if (DisableServerChatArgument.isChatDisabled() && !sender.hasPermission("gigachat.bypass.disabledchat")) {
            sender.sendMessage(this.messages.getServerChatIsDisabled());
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    private Chat getChat(final String message) {
        if (message.length() == 1) {
            return this.chatValues.getDefaultChat();
        }

        final char firstChar = message.charAt(0);
        return this.chatValues.getChats().getOrDefault(firstChar, this.chatValues.getDefaultChat());
    }

    @Nullable // TODO Remove this shit
    private String getValidMessage(final Player sender, String message) {
        if (sender.hasPermission("gigachat.bypass.validator.chat.*")) {
            return message;
        }

        final StringValidator stringValidator = this.chatValues.getStringValidator();
        if (!sender.hasPermission("gigachat.bypass.validator.chat.chars")
                && stringValidator.hasBlockedChars(message)) {
            if (this.chatValues.isCharsValidatorBlockMessage()) {
                final String denyMessage = this.messages.getCharsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                if (this.chatValues.getCapsValidatorDenySound() != null) {
                    PlayerUtils.playSound(sender, this.chatValues.getCharValidatorDenySound());
                }
                return null;
            }

            message = stringValidator.getFormattedCharsMessage(message);
        }

        if (!sender.hasPermission("gigachat.bypass.validator.chat.caps")
                && stringValidator.isUpperCasePercentageExceeded(message)) {
            if (this.chatValues.isCapsValidatorBlockMessageSend()) {
                final String denyMessage = this.messages.getCharsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                if (this.chatValues.getCapsValidatorDenySound() != null) {
                    PlayerUtils.playSound(sender, this.chatValues.getCapsValidatorDenySound());
                }
                return null;
            }

            message = message.toLowerCase();
        }

        if (!sender.hasPermission("gigachat.bypass.validator.chat.words")
                && stringValidator.hasBlockedWords(message)) {
            if (this.chatValues.isWordsValidatorBlockMessageSend()) {
                final String denyMessage = this.messages.getWordsValidationFailedMessage();
                if (!denyMessage.isEmpty()) {
                    sender.sendMessage(denyMessage);
                }
                if (this.chatValues.getWordsValidatorDenySound() != null) {
                    PlayerUtils.playSound(sender, this.chatValues.getWordsValidatorDenySound());
                }
                return null;
            }

            message = stringValidator.getFormattedWordsMessage(message);
        }

        return message;
    }

    private void sendHover(final Player sender, final String message, final Hover hover, final Set<Player> recipients,
                           final String prefix, final String suffix, final String color) {
        final String hoverText = hover.hoverText()
                .replace("{player}", sender.getName())
                .replace("{prefix}", prefix)
                .replace("{suffix}", suffix)
                .replace("{color}", color);
        final Colorizer colorizer = this.chatValues.getFormatColorizer();
        final BaseComponent[] components = HoverUtils.get(sender, hover, hoverText, message, colorizer);
        for (final Player recipient : recipients) {
            recipient.spigot().sendMessage(components);
        }
    }

    private void sendSpy(final Player sender, final Chat chat,
                         final String message, final String spyFormat,
                         final Set<Player> recipients,
                         final Set<Player> spyRecipients,
                         final String prefix, final String suffix, final String color) {
        final String formattedMessage = this.getFormattedMessage(sender, message, spyFormat, prefix, suffix, color);

        final Hover hover = chat.getHover();
        if (hover.isEnabled()) {
            this.sendHover(sender, formattedMessage, hover, recipients, prefix, suffix, color);
            return;
        }

        for (final Player recipient : spyRecipients) {
            if (!recipients.contains(recipient)) {
                recipient.sendMessage(formattedMessage);
            }
        }
    }

    private Set<Player> getAdminRecipients(final Set<Player> recipients) {
        final Set<Player> adminRecipients = new HashSet<>();
        for (final Iterator<Player> iterator = recipients.iterator(); iterator.hasNext(); ) {
            final Player target = iterator.next();
            if (target.hasPermission("gigachat.adminhover")) {
                adminRecipients.add(target);
                iterator.remove();
            }
        }

        return adminRecipients;
    }

    public String getFormattedMessage(final Player sender, String message, final String format,
                                      final String prefix, final String suffix, final String color) {
        final String formattedMessage = this.chatValues.getFormatColorizer().colorize(
                Utils.replacePlaceholders(
                        sender,
                        format.replace("{player}", sender.getName())
                                .replace("{prefix}", prefix)
                                .replace("{suffix}", suffix)
                                .replace("{color}", color)
                )
        );

        message = this.chatValues.getChatsColorizer().colorize(sender, message);
        return formattedMessage.replace("{message}", message);
    }

    private void log(final Player sender, final Chat chat, final String message) {
        this.plugin.getChatLogger().log(() ->
                "[" + sender.getName() + " -> " + chat.getName() + "] " + ColorizerUtils.getClear(message)
        );
    }
}
