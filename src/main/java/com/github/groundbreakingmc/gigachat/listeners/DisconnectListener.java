package com.github.groundbreakingmc.gigachat.listeners;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.*;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.database.Database;
import com.github.groundbreakingmc.gigachat.utils.configvalues.ChatValues;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DisconnectListener implements Listener {

    private final GigaChat plugin;
    private final ChatValues chatValues;
    private final Database database;
    private final PmSoundsCollection pmSoundsCollection;
    private final DisabledPrivateMessagesCollection disabledPrivateMessagesCollection;

    public DisconnectListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatValues = plugin.getChatValues();
        this.database = plugin.getDatabase();
        this.pmSoundsCollection = plugin.getPmSoundsCollection();
        this.disabledPrivateMessagesCollection = plugin.getDisabled();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        this.loadData(playerUUID);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        this.removeData(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(final PlayerKickEvent event) {
        this.removeData(event);
    }

    private void loadData(final UUID playerUUID) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (final Connection connection = this.database.getConnection()) {
                if (this.database.containsInTable(Database.DISABLED_CHAT_CONTAINS_PLAYER, connection, playerUUID)) {
                    this.plugin.getCustomLogger().info(Bukkit.getPlayer(playerUUID).getName() + " added");
                    DisabledChatCollection.add(playerUUID);
                }
                if (this.database.containsInTable(Database.CHECK_IF_PLAYER_DISABLED_PRIVATE_MESSAGES, connection, playerUUID)) {
                    this.disabledPrivateMessagesCollection.add(playerUUID);
                }
                final Set<UUID> ignoredChat = this.database.getListOfIgnoredPlayers(Database.GET_IGNORED_PLAYERS_FROM_CHAT, connection, playerUUID);
                if (!ignoredChat.isEmpty()) {
                    IgnoreCollections.addToIgnoredChat(playerUUID, ignoredChat);
                }
                final Set<UUID> ignoredPrivate = this.database.getListOfIgnoredPlayers(Database.GET_IGNORED_PRIVATE, connection, playerUUID);
                if (!ignoredPrivate.isEmpty()) {
                    IgnoreCollections.addToIgnoredPrivate(playerUUID, ignoredPrivate);
                }
                final Sound sound = this.database.getPlayerSelectedSound(connection, playerUUID);
                if (sound != null) {
                    this.pmSoundsCollection.setSound(playerUUID, sound);
                }
                if (this.database.containsInTable(Database.CHECK_IF_PLAYER_ENABLED_SOCIAL_SPY, connection, playerUUID)) {
                    SocialSpyCollection.add(playerUUID);
                }
                if (this.database.containsInTable(Database.CHECK_IF_PLAYER_ENABLED_AUTO_MESSAGES, connection, playerUUID)) {
                    AutoMessagesCollection.add(playerUUID);
                }

                this.loadPlayerListenData(connection, playerUUID);
            } catch(final SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void loadPlayerListenData(final Connection connection, final UUID playerUUID) throws SQLException {
        final List<String> chatsWherePlayerListen = this.database.getChatsWherePlayerIsListening(connection, playerUUID);
        final Map<Character, Chat> chats = this.chatValues.getChats();
        for (final Map.Entry<Character, Chat> entry : chats.entrySet()) {
            final Chat chat = entry.getValue();
            if (chatsWherePlayerListen.contains(chat.getName())) {
                chat.getSpyListeners().add(playerUUID);
                chatsWherePlayerListen.remove(chat.getName());
            }
        }
        for (int i = 0; i < chatsWherePlayerListen.size(); i++) {
            final String chatName = chatsWherePlayerListen.get(i);
            this.database.executeUpdateQuery(Database.REMOVE_CHAT_FOR_PLAYER_FROM_CHAT_LISTENERS, connection, playerUUID.toString(), chatName);
        }
    }

    private void removeData(final PlayerEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        DisabledChatCollection.remove(playerUUID);
        this.disabledPrivateMessagesCollection.remove(playerUUID);
        IgnoreCollections.removeFromIgnoredChat(playerUUID);
        IgnoreCollections.removeFromIgnoredPrivate(playerUUID);
        this.pmSoundsCollection.remove(playerUUID);
        ReplyCollection.removeFromAll(playerUUID);
        SocialSpyCollection.remove(playerUUID);
        AutoMessagesCollection.remove(playerUUID);
        for (final Map.Entry<Character, Chat> entry : this.chatValues.getChats().entrySet()) {
            entry.getValue().getSpyListeners().remove(playerUUID);
        }
    }
}
