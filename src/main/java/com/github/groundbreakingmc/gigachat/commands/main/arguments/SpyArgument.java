package com.github.groundbreakingmc.gigachat.commands.main.arguments;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.Argument;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.database.Database;
import com.github.groundbreakingmc.gigachat.utils.configvalues.ChatValues;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

public final class SpyArgument extends Argument {

    private final ChatValues chatValues;

    public SpyArgument(final GigaChat plugin) {
        super(plugin, "spy", "gigachat.command.spy.other");
        this.chatValues = plugin.getChatValues();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(super.getMessages().getSpyUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(super.getMessages().getPlayerNotFound());
            return true;
        }

        final Chat chat = this.findChat(args[2]);
        if (chat != null) {
            return process(sender, target, chat);
        }

        sender.sendMessage(super.getMessages().getChatNotFound());
        return true;
    }

    private Chat findChat(final String specifiedName) {
        if (this.chatValues.getDefaultChat().getName().equals(specifiedName)) {
            return this.chatValues.getDefaultChat();
        }

        for (final Chat chat : this.chatValues.getChats().values()) {
            if (chat.getName().equals(specifiedName)) {
                return chat;
            }
        }

        return null;
    }

    private boolean process(final CommandSender sender, final Player target, final Chat chat) {
        final Set<UUID> players = chat.getSpyListeners();
        final String chatName = chat.getName();
        final String replacement = super.getMessages().getChatNames().getOrDefault(chatName, chatName);
        final String targetName = target.getName();
        final UUID targetUUID = target.getUniqueId();
        final String mode;

        if (players.contains(targetUUID)) {
            sender.sendMessage(super.getMessages().getChatsSpyDisabledOther().replace("{player}", targetName).replace("{chat}", replacement));
            this.sendMessage(target, super.getMessages().getChatsSpyDisabledByOther(), replacement);
            players.remove(target.getUniqueId());
            this.processDatabase(targetUUID, Database.REMOVE_PLAYER_FROM_SOCIAL_SPY);
            mode = "disabled";
        } else {
            sender.sendMessage(super.getMessages().getChatsSpyEnabledOther().replace("{player}", targetName).replace("{chat}", replacement));
            this.sendMessage(target, super.getMessages().getChatsSpyEnabledByOther(), replacement);
            players.add(target.getUniqueId());
            this.processDatabase(targetUUID, Database.ADD_PLAYER_TO_SOCIAL_SPY);
            mode = "enabled";
        }

        super.getPlugin().getPluginCommandLogger().log(() ->
                "[SPY] [" + sender.getName() + "] " + mode + " spy in " + chat.getName() + " chat for " + target.getName()
        );

        return true;
    }

    private void sendMessage(final Player target, final String message, final String replacement) {
        if (!message.isEmpty()) {
            target.sendMessage(message.replace("{chat}", replacement));
        }
    }

    private void processDatabase(final UUID targetUUID, final String query) {
        Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
            try (final Connection connection = super.getDatabase().getConnection()) {
                super.getDatabase().executeUpdateQuery(query, connection, targetUUID);
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        });
    }
}
