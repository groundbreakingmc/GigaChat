package com.github.groundbreakingmc.gigachat.commands.other;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.database.Database;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import com.github.groundbreakingmc.mylib.collections.expiring.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatSpyExecutor {

    private final GigaChat plugin;
    private final Messages messages;
    private final Database database;

    public ChatSpyExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.database = plugin.getDatabase();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull Chat chat, final ExpiringMap<UUID, Long> spyCooldowns, final int spyCooldown) {
        if (!(sender instanceof final Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("gigachat.command.spy." + chat.getName())) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        final UUID senderUUID = playerSender.getUniqueId();
        if (!sender.hasPermission("gigachat.bypass.spycooldown." + chat.getName()) && spyCooldowns.containsKey(senderUUID)) {
            final int time = (int) (spyCooldown / 1000 + (spyCooldowns.get(senderUUID) - System.currentTimeMillis()) / 1000);
            final String restTime = Utils.getTime(time);
            final String cooldownMessage = messages.getCommandCooldownMessage().replace("{time}", restTime);
            sender.sendMessage(cooldownMessage);
            return true;
        }

        final String chatName = chat.getName();
        final Set<Player> players = chat.getSpyListeners();

        if (players.contains(senderUUID)) {
            return this.process(
                    players::remove,
                    playerSender,
                    senderUUID,
                    Database.REMOVE_CHAT_FOR_PLAYER_FROM_CHAT_LISTENERS,
                    chatName,
                    this.messages.getChatsSpyDisabled(),
                    "disabled"
            );
        }

        return this.process(
                players::add,
                playerSender,
                senderUUID,
                Database.ADD_PLAYER_TO_CHAT_LISTENERS,
                chatName,
                this.messages.getChatsSpyEnabled(),
                "enabled"
        );
    }

    private boolean process(
            final Consumer<Player> consumer,
            final Player sender, final UUID senderUUID,
            final String query, final String chatName, final String message,
            final String mode) {
        final String chatNameReplacement = this.messages.getChatNames().getOrDefault(chatName, chatName);

        consumer.accept(sender);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (final Connection connection = this.database.getConnection()) {
                this.database.executeUpdateQuery(query, connection, senderUUID.toString(), chatName);
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        });

        sender.sendMessage(message.replace("{chat}", chatNameReplacement));

        this.plugin.getCommandLogger().log(() ->
                "[SPY] [" + sender.getName() + "] " + mode + " spy in " + chatName + " chat"
        );

        return true;
    }
}
