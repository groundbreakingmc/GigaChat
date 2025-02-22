package com.github.groundbreakingmc.gigachat.database;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.mylib.database.DatabaseUtils;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class Database extends com.github.groundbreakingmc.mylib.database.Database {

    /**
     * Adds the player name to the "disabledChat" table, to save the player's choice
     */
    public static final String ADD_PLAYER_TO_DISABLED_CHAT = "INSERT OR IGNORE INTO disabledChat(playerUUID) VALUES(?);";

    /**
     * Removes the player's name from the "disabledChat" table
     */
    public static final String REMOVE_PLAYER_FROM_DISABLED_CHAT = "DELETE FROM disabledChat WHERE playerUUID = ?;";

    /**
     * Checks the "disabledChat" table for a player's name, to see if he has chat disabled.
     */
    public static final String DISABLED_CHAT_CONTAINS_PLAYER = "SELECT EXISTS(SELECT 1 FROM disabledChat WHERE playerUUID = ?);";

    /**
     * Adds the player name to the "ignoreChat" table, to save the player's choice
     */
    public static final String ADD_PLAYER_TO_IGNORE_CHAT = "INSERT OR IGNORE INTO ignoreChat(playerUUID, ignoredUUID) VALUES(?, ?);";

    /**
     * Removes the player's name from the "ignoreChat" table
     */
    public static final String REMOVE_PLAYER_FROM_IGNORE_CHAT = "DELETE FROM ignoreChat WHERE playerUUID = ?;";

    /**
     * Removes the player's name from the "ignoreChat" table
     */
    public static final String REMOVE_IGNORED_FROM_IGNORE_CHAT = "DELETE FROM ignoreChat WHERE playerUUID = ? and ignoredUUID = ?;";

    /**
     * Checks the "ignoreChat" table for a player's name, to see if he ignores anybody.
     */
    public static final String GET_IGNORED_PLAYERS_FROM_CHAT = "SELECT ignoredUUID FROM ignoreChat WHERE playerUUID = ?;";

    /**
     * Adds the player name to the "disabledPrivateMessages" table, to save the player's choice
     */
    public static final String ADD_PLAYER_TO_DISABLED_PRIVATE_MESSAGES = "INSERT OR IGNORE INTO disabledPrivateMessages(playerUUID) VALUES(?);";

    /**
     * Removes the player's name from the "disabledPrivateMessages" table
     */
    public static final String REMOVE_PLAYER_FROM_DISABLED_PRIVATE_MESSAGES = "DELETE FROM disabledPrivateMessages WHERE playerUUID = ?;";

    /**
     * Checks the "disabledPrivateMessages" table for a player's name, to see if he has private messages disabled.
     */
    public static final String CHECK_IF_PLAYER_DISABLED_PRIVATE_MESSAGES = "SELECT EXISTS(SELECT 1 FROM disabledPrivateMessages WHERE playerUUID = ?);";

    /**
     * Adds the player name to the "ignorePrivate" table, to save the player's choice
     */
    public static final String ADD_PLAYER_TO_IGNORE_PRIVATE = "INSERT OR IGNORE INTO ignorePrivate(playerUUID, ignoredUUID) VALUES(?, ?);";

    /**
     * Removes the player's name from the "ignorePrivate" table
     */
    public static final String REMOVE_PLAYER_FROM_IGNORE_PRIVATE_PRIVATE = "DELETE FROM ignorePrivate WHERE playerUUID = ?;";

    /**
     * Removes the player's name from the "ignorePrivate" table
     */
    public static final String REMOVE_IGNORED_PLAYER_FROM_IGNORE_PRIVATE = "DELETE FROM ignorePrivate WHERE playerUUID = ? and ignoredUUID = ?;";

    /**
     * Retrieves all players ignored by the player from the "ignorePrivate" table.
     */
    public static final String GET_IGNORED_PRIVATE = "SELECT ignoredUUID FROM ignorePrivate WHERE playerUUID = ?;";

    /**
     * Adds the player name to the "privateMessagesSounds" table, to save the player's choice
     */
    public static final String ADD_PLAYER_PM_SOUND_TO_PRIVATE_MESSAGES_SOUNDS = """
                INSERT INTO privateMessagesSounds(playerUUID, soundSettings)
                VALUES(?, ?)
                ON CONFLICT(playerUUID) DO UPDATE SET soundSettings = excluded.soundSettings;
            """;

    /**
     * Removes the player's name from the "privateMessagesSounds" table
     */
    public static final String REMOVE_PLAYER_FROM_PRIVATE_MESSAGES_SOUNDS = "DELETE FROM privateMessagesSounds WHERE playerUUID = ?;";

    /**
     * Adds the player name to the "socialSpy" table, to save the player's choice
     */
    public static final String ADD_PLAYER_TO_SOCIAL_SPY = "INSERT OR IGNORE INTO socialSpy(playerUUID) VALUES(?);";

    /**
     * Removes the player's name from the "socialSpy" table
     */
    public static final String REMOVE_PLAYER_FROM_SOCIAL_SPY = "DELETE FROM socialSpy WHERE playerUUID = ?;";

    /**
     * Checks the "localSpy" table for a player's name, to see if he is spying on the private messages.
     */
    public static final String CHECK_IF_PLAYER_ENABLED_SOCIAL_SPY = "SELECT EXISTS(SELECT 1 FROM socialSpy WHERE playerUUID = ?);";

    /**
     * Adds the player name to the "autoMessages" table, to save the player's choice.
     */
    public static final String ADD_PLAYER_TO_AUTO_MESSAGES = "INSERT OR IGNORE INTO autoMessages(playerUUID) VALUES(?);";

    /**
     * Removes the player's name from the "autoMessages" table.
     */
    public static final String REMOVE_PLAYER_FROM_AUTO_MESSAGES = "DELETE FROM autoMessages WHERE playerUUID = ?;";

    /**
     * Checks the "autoMessages" table for a player's name, to see if he has disabled auto messages.
     */
    public static final String CHECK_IF_PLAYER_ENABLED_AUTO_MESSAGES = "SELECT EXISTS(SELECT 1 FROM autoMessages WHERE playerUUID = ?);";

    /**
     * Adds the player name to the "chatListeners" table, to save the player's choice.
     */
    public static final String ADD_PLAYER_TO_CHAT_LISTENERS = "INSERT OR IGNORE INTO chatListeners(playerUUID, chatName) VALUES(?, ?);";

    /**
     * Removes the chat for the player from the "chatListeners" table.
     */
    public static final String REMOVE_CHAT_FOR_PLAYER_FROM_CHAT_LISTENERS = "DELETE FROM chatListeners WHERE playerUUID = ? and chatName = ?;";

    public Database(GigaChat plugin) {
        super(DatabaseUtils.getSQLiteDriverUrl(plugin));
    }

    /**
     * Creates tables in the database if they do not already exist
     * If the tables are already present, no changes will be made
     */
    public void createDatabaseTables() {
        final String[] queries = {
                "CREATE TABLE IF NOT EXISTS disabledChat(playerUUID TEXT NOT NULL UNIQUE);",
                "CREATE TABLE IF NOT EXISTS disabledPrivateMessages(playerUUID TEXT NOT NULL UNIQUE);",
                "CREATE TABLE IF NOT EXISTS ignoreChat(playerUUID TEXT NOT NULL, ignoredUUID TEXT NOT NULL, PRIMARY KEY(playerUUID, ignoredUUID));",
                "CREATE TABLE IF NOT EXISTS ignorePrivate(playerUUID TEXT NOT NULL, ignoredUUID TEXT NOT NULL, PRIMARY KEY(playerUUID, ignoredUUID));",
                "CREATE TABLE IF NOT EXISTS privateMessagesSounds(playerUUID TEXT NOT NULL UNIQUE, soundSettings TEXT NOT NULL);",
                "CREATE TABLE IF NOT EXISTS socialSpy(playerUUID TEXT NOT NULL UNIQUE);",
                "CREATE TABLE IF NOT EXISTS autoMessages(playerUUID TEXT NOT NULL UNIQUE);",
                "CREATE TABLE IF NOT EXISTS chatListeners(playerUUID TEXT NOT NULL, chatName TEXT NOT NULL, PRIMARY KEY(playerUUID, chatName));"
        };

        try (final Connection connection = super.getConnection()) {
            super.createTables(connection, queries);
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Set<UUID> getListOfIgnoredPlayers(final String query, final Connection connection, final UUID playerUUID) throws SQLException {
        final Set<UUID> ignoredPlayers = new HashSet<>();
        try (final ResultSet result = super.getStatement(query, connection, playerUUID).executeQuery()) {
            while (result.next()) {
                final String string = result.getString("ignoredUUID");
                final UUID ignoredUUID = UUID.fromString(string);
                ignoredPlayers.add(ignoredUUID);
            }
        }

        return ignoredPlayers;
    }

    /**
     * Retrieves the sound selected by player from the "privateMessagesSounds" table.
     *
     * @param playerUUID UUID of the player
     * @return name of the sound player has chosen
     */
    public SoundSettings getPlayerSelectedSound(final Connection connection, final UUID playerUUID) throws SQLException {
        final String query = "SELECT soundSettings FROM privateMessagesSounds WHERE playerUUID = ?;";
        try (final ResultSet result = super.getStatement(query, connection, playerUUID).executeQuery()) {
            if (result.next()) {
                final String string = result.getString("soundSettings");
                return SoundSettings.get(string);
            }
        }

        return null;
    }

    /**
     * Returns the list from "chatListeners" table with chat names of chats where a player has spy mode enabled.
     *
     * @param playerUUID UUID of the player
     * @return list with the chat names
     */
    public List<String> getChatsWherePlayerIsListening(final Connection connection, final UUID playerUUID) throws SQLException {
        final String query = "SELECT chatName FROM chatListeners WHERE playerUUID = ?;";
        final List<String> chats = new ArrayList<>();
        try (final ResultSet result = super.getStatement(query, connection, playerUUID).executeQuery()) {
            while (result.next()) {
                final String chatName = result.getString("chatName");
                chats.add(chatName);
            }
        }

        return chats;
    }
}
