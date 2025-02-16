package com.github.groundbreakingmc.gigachat.commands.privatemessages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.CooldownCollections;
import com.github.groundbreakingmc.gigachat.collections.IgnoreCollections;
import com.github.groundbreakingmc.gigachat.database.Database;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import com.github.groundbreakingmc.gigachat.utils.configvalues.PrivateMessagesValues;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class IgnoreExecutor implements TabExecutor {

    private final GigaChat plugin;
    private final Messages messages;
    private final Database database;
    private final PrivateMessagesValues pmValues;
    private final CooldownCollections cooldownCollections;

    public IgnoreExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.database = plugin.getDatabase();
        this.pmValues = plugin.getPmValues();
        this.cooldownCollections = plugin.getCooldownCollections();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        if (this.hasCooldown(playerSender)) {
            this.sendMessageHasCooldown(playerSender);
            return true;
        }

        final boolean hasChatIgnorePerm = sender.hasPermission("gigachat.command.ignore.chat");
        final boolean hasPrivateIgnorePerm = sender.hasPermission("gigachat.command.ignore.private");

        if (args.length < (hasChatIgnorePerm && hasPrivateIgnorePerm ? 2 : 1)) {
            sender.sendMessage(this.messages.getIgnoreUsageError());
            return true;
        }

        final boolean isChat = hasChatIgnorePerm && args[0].equalsIgnoreCase("chat");
        final Player target = Bukkit.getPlayer(hasChatIgnorePerm && hasPrivateIgnorePerm ? args[1] : args[0]);

        if (target == null || !playerSender.canSee(target) || PlayerUtils.isVanished(target)) {
            playerSender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        if (target == playerSender) {
            playerSender.sendMessage(this.messages.getCannotIgnoreHimself());
            return true;
        }

        final UUID senderUUID = playerSender.getUniqueId();
        final UUID targetUUID = target.getUniqueId();
        if (isChat) {
            this.processChat(playerSender, target, senderUUID, targetUUID);
        } else {
            this.processPrivate(playerSender, target, senderUUID, targetUUID);
        }

        return true;
    }

    private boolean hasCooldown(final Player playerSender) {
        return this.cooldownCollections.hasCooldown(playerSender, "gigachat.bypass.cooldown.ignore", this.cooldownCollections.getIgnoreCooldowns());
    }

    private void sendMessageHasCooldown(final Player sender) {
        final long timeLeftInMillis = this.cooldownCollections.getIgnoreCooldowns()
                .get(sender.getUniqueId()) - System.currentTimeMillis();
        final int result = (int) (this.pmValues.getPmCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        sender.sendMessage(message);
    }

    private void processChat(final Player sender, final Player target, final UUID senderUUID, final UUID targetUUID) {
        if (IgnoreCollections.ignoredChatContains(senderUUID, targetUUID)) {
            this.process(
                    IgnoreCollections::removeFromIgnoredChat,
                    sender, senderUUID,
                    target, targetUUID,
                    IgnoreCollections.isIgnoredChatEmpty()
                            ? Database.REMOVE_PLAYER_FROM_IGNORE_CHAT
                            : Database.REMOVE_IGNORED_FROM_IGNORE_CHAT,
                    this.messages.getChatIgnoreDisabled(),
                    "unignored",
                    IgnoreCollections.isIgnoredChatEmpty()
                            ? new Object[]{senderUUID.toString()}
                            : new Object[]{senderUUID.toString(), targetUUID.toString()}
            );
            return;
        }

        this.process(
                IgnoreCollections::removeFromIgnoredChat,
                sender, senderUUID,
                target, targetUUID,
                Database.ADD_PLAYER_TO_IGNORE_CHAT,
                this.messages.getChatIgnoreEnabled(),
                "ignored",
                senderUUID.toString(), targetUUID.toString()
        );
    }

    private void processPrivate(final Player sender, final Player target,
                                final UUID senderUUID, final UUID targetUUID) {
        if (IgnoreCollections.ignoredPrivateContains(senderUUID, targetUUID)) {
            this.process(
                    IgnoreCollections::removeFromIgnoredPrivate,
                    sender, senderUUID,
                    target, targetUUID,
                    IgnoreCollections.isIgnoredPrivateEmpty()
                            ? Database.REMOVE_PLAYER_FROM_IGNORE_PRIVATE_PRIVATE
                            : Database.REMOVE_IGNORED_PLAYER_FROM_IGNORE_PRIVATE,
                    this.messages.getPrivateIgnoreDisabled(),
                    "unignored",
                    IgnoreCollections.isIgnoredPrivateEmpty()
                            ? new Object[]{senderUUID.toString()}
                            : new Object[]{senderUUID.toString(), targetUUID.toString()}
            );
            return;
        }

        this.process(
                IgnoreCollections::addToIgnoredPrivate,
                sender, senderUUID,
                target, targetUUID,
                Database.ADD_PLAYER_TO_IGNORE_PRIVATE,
                this.messages.getPrivateIgnoreEnabled(),
                "ignored",
                senderUUID.toString(), targetUUID.toString()
        );
    }

    private void process(
            final BiConsumer<UUID, UUID> consumer,
            final Player sender, final UUID senderUUID,
            final Player target, final UUID targetUUID,
            final String query, final String message,
            final String mode,
            final Object... queryParams) {
        consumer.accept(senderUUID, targetUUID);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (final Connection connection = this.database.getConnection()) {
                this.database.executeUpdateQuery(query, connection, queryParams);
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        });

        sender.sendMessage(message.replace("{player}", target.getName()));
        this.cooldownCollections.addCooldown(senderUUID, this.cooldownCollections.getIgnoreCooldowns());

        this.plugin.getCommandLogger().log(() ->
                "[IGNORE] [" + sender.getName() + "] " + mode + " " + target.getName()
        );
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof final Player playerSender)) {
            return List.of();
        }

        final List<String> completions = new ArrayList<>();
        final UUID senderUUID = playerSender.getUniqueId();
        final String input = args[args.length - 1];

        final boolean ignoreChat = sender.hasPermission("gigachat.command.ignore.chat");
        final boolean ignorePrivate = sender.hasPermission("gigachat.command.ignore.private");

        if (ignoreChat && ignorePrivate) {
            if (args.length == 1) {
                this.first(input, completions);
            } else if (args.length == 2) {
                final boolean usedChatArg = args[0].equals("chat");
                if (usedChatArg || args[0].equals("private")) {
                    this.second(playerSender, input, completions, this.getFunction(senderUUID, usedChatArg));
                }
            }
        } else if (args.length == 1 && ignoreChat || ignorePrivate) {
            this.second(playerSender, input, completions, this.getFunction(senderUUID, ignoreChat));
        }

        return completions;
    }

    private void first(final String input, final List<String> completions) {
        if (StringUtil.startsWithIgnoreCase("chat", input)) {
            completions.add("chat");
        }
        if (StringUtil.startsWithIgnoreCase("private", input)) {
            completions.add("private");
        }
    }

    private void second(final Player sender,
                        final String input,
                        final List<String> completions,
                        final Predicate<UUID> predicate) {
        for (final Player target : Bukkit.getOnlinePlayers()) {
            if (target == sender) {
                continue;
            }

            final String targetName = target.getName();
            final UUID targetUUID = target.getUniqueId();
            if (StringUtil.startsWithIgnoreCase(targetName, input)
                    && !PlayerUtils.isVanished(target)
                    && predicate.test(targetUUID)) {
                completions.add(targetName);
            }
        }
    }

    private Predicate<UUID> getFunction(final UUID senderUUID, final boolean condition) {
        return condition
                ? targetUUID -> !IgnoreCollections.isIgnoredChat(senderUUID, targetUUID)
                : targetUUID -> !IgnoreCollections.isIgnoredPrivate(senderUUID, targetUUID);
    }
}
