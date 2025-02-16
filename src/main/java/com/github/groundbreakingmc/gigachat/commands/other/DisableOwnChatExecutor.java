package com.github.groundbreakingmc.gigachat.commands.other;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.DisabledChatCollection;
import com.github.groundbreakingmc.gigachat.database.Database;
import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class DisableOwnChatExecutor implements TabExecutor {

    private final GigaChat plugin;
    private final Messages messages;
    private final Database database;

    public DisableOwnChatExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.database = plugin.getDatabase();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        if (!sender.hasPermission("gigachat.command.disableownchat")) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        return this.processDisable(playerSender);
    }

    private boolean processDisable(final Player sender) {
        final UUID senderUUID = sender.getUniqueId();
        if (DisabledChatCollection.contains(senderUUID)) {
            this.plugin.getCustomLogger().info(sender.getName() + " removed");
            return this.process(
                    DisabledChatCollection::remove,
                    sender,
                    senderUUID,
                    Database.REMOVE_PLAYER_FROM_DISABLED_CHAT,
                    this.messages.getOwnChatDisabled(),
                    "disabled"
            );
        }

        this.plugin.getCustomLogger().info(sender.getName() + " added");
        return this.process(
                DisabledChatCollection::add,
                sender,
                senderUUID,
                Database.ADD_PLAYER_TO_DISABLED_CHAT,
                this.messages.getOwnChatEnabled(),
                "enabled"
        );
    }

    private boolean process(
            final Consumer<UUID> consumer,
            final Player sender, final UUID senderUUID,
            final String query, final String message,
            final String mode) {
        consumer.accept(senderUUID);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (final Connection connection = this.database.getConnection()) {
                this.database.executeUpdateQuery(query, connection, senderUUID.toString());
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        });

        sender.sendMessage(message);

        this.plugin.getCommandLogger().log(() ->
                "[DISABLE-CHAT] [" + sender.getName() + "] " + mode
        );

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}