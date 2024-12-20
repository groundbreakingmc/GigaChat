package com.github.groundbreakingmc.gigachat.listeners;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.*;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.database.DatabaseHandler;
import com.github.groundbreakingmc.gigachat.database.DatabaseQueries;
import com.github.groundbreakingmc.gigachat.utils.config.values.ChatValues;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    private final PmSoundsCollection pmSoundsCollection;
    private final DisabledPrivateMessagesCollection disabledPrivateMessagesCollection;

    public DisconnectListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatValues = plugin.getChatValues();
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
        final UUID playerUUID = event.getPlayer().getUniqueId();
        this.removeData(playerUUID);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(final PlayerKickEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        this.removeData(playerUUID);
    }

    private void loadData(final UUID playerUUID) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (final Connection connection = DatabaseHandler.getConnection()) {
                if (DatabaseQueries.containsPlayerInTable(DatabaseQueries.DISABLED_CHAT_CONTAINS_PLAYER, connection, playerUUID)) {
                    DisabledChatCollection.add(playerUUID);
                }
                if (DatabaseQueries.containsPlayerInTable(DatabaseQueries.CHECK_IF_PLAYER_DISABLED_PRIVATE_MESSAGES, connection, playerUUID)) {
                    this.disabledPrivateMessagesCollection.add(playerUUID);
                }
                final Set<UUID> ignoredChat = DatabaseQueries.getListOfIgnoredPlayers(DatabaseQueries.GET_IGNORED_PLAYERS_FROM_CHAT, connection, playerUUID);
                if (!ignoredChat.isEmpty()) {
                    IgnoreCollections.addToIgnoredChat(playerUUID, ignoredChat);
                }
                final Set<UUID> ignoredPrivate = DatabaseQueries.getListOfIgnoredPlayers(DatabaseQueries.GET_IGNORED_PRIVATE, connection, playerUUID);
                if (!ignoredPrivate.isEmpty()) {
                    IgnoreCollections.addToIgnoredPrivate(playerUUID, ignoredPrivate);
                }
                final Sound sound = DatabaseQueries.getPlayerSelectedSound(connection, playerUUID);
                if (sound != null) {
                    this.pmSoundsCollection.setSound(playerUUID, sound);
                }
                if (DatabaseQueries.containsPlayerInTable(DatabaseQueries.CHECK_IF_PLAYER_ENABLED_SOCIAL_SPY, connection, playerUUID)) {
                    SocialSpyCollection.add(playerUUID);
                }
                if (DatabaseQueries.containsPlayerInTable(DatabaseQueries.CHECK_IF_PLAYER_ENABLED_AUTO_MESSAGES, connection, playerUUID)) {
                    AutoMessagesCollection.add(playerUUID);
                }

                this.loadPlayerListenData(connection, playerUUID);
            } catch(final SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void loadPlayerListenData(final Connection connection, final UUID playerUUID) throws SQLException {
        final List<String> chatsWherePlayerListen = DatabaseQueries.getChatsWherePlayerIsListening(connection, playerUUID);
        final Object2ObjectOpenHashMap<Character, Chat> chats = this.chatValues.getChats();
        for (final Map.Entry<Character, Chat> entry : chats.object2ObjectEntrySet()) {
            final Chat chat = entry.getValue();
            if (chatsWherePlayerListen.contains(chat.getName())) {
                chat.getSpyListeners().add(playerUUID);
                chatsWherePlayerListen.remove(chat.getName());
            }
        }
        for (int i = 0; i < chatsWherePlayerListen.size(); i++) {
            final String chatName = chatsWherePlayerListen.get(i);
            DatabaseQueries.executeUpdateQuery(DatabaseQueries.REMOVE_CHAT_FOR_PLAYER_FROM_CHATS_LISTENERS, connection, playerUUID.toString(), chatName);
        }
    }

    private void removeData(final UUID playerUUID) {
        DisabledChatCollection.remove(playerUUID);
        this.disabledPrivateMessagesCollection.remove(playerUUID);
        IgnoreCollections.removeFromIgnoredChat(playerUUID);
        IgnoreCollections.removeFromIgnoredPrivate(playerUUID);
        this.pmSoundsCollection.remove(playerUUID);
        ReplyCollection.removeFromAll(playerUUID);
        SocialSpyCollection.remove(playerUUID);
        AutoMessagesCollection.remove(playerUUID);
    }
}