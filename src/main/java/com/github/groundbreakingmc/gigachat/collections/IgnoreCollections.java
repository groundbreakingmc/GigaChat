package com.github.groundbreakingmc.gigachat.collections;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class IgnoreCollections {

    private static final Map<UUID, Set<UUID>> ignoredChat = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, Set<UUID>> ignoredPrivate = new Object2ObjectOpenHashMap<>();

    private IgnoreCollections() {

    }

    public static boolean isIgnoredChatEmpty() {
        return ignoredChat.isEmpty();
    }

    public static boolean isIgnoredPrivateEmpty() {
        return ignoredPrivate.isEmpty();
    }

    public static boolean ignoredChatContains(final UUID targetUUID) {
        return !ignoredChat.isEmpty() && ignoredChat.containsKey(targetUUID);
    }

    public static boolean ignoredPrivateContains(final UUID targetUUID) {
        return !ignoredPrivate.isEmpty() && ignoredPrivate.containsKey(targetUUID);
    }

    public static boolean playerIgnoresChatAnyOne(final UUID targetUUID) {
        return !ignoredChat.isEmpty()
                && ignoredChat.containsKey(targetUUID)
                && ignoredChat.get(targetUUID) != null
                && !ignoredChat.get(targetUUID).isEmpty();
    }

    public static boolean playerIgnoresPrivateAnyOne(final UUID targetUUID) {
        return !ignoredPrivate.isEmpty()
                && ignoredPrivate.containsKey(targetUUID)
                && ignoredPrivate.get(targetUUID) != null
                && !ignoredPrivate.get(targetUUID).isEmpty();
    }

    public static boolean ignoredChatContains(final UUID searchUUID, final UUID targetUuid) {
        return !ignoredChat.isEmpty() && ignoredChat.get(searchUUID).contains(targetUuid);
    }

    public static boolean ignoredPrivateContains(final UUID searchUUID, final UUID targetUuid) {
        return !ignoredPrivate.isEmpty() && ignoredPrivate.get(searchUUID).contains(targetUuid);
    }

    public static void addToIgnoredChat(final UUID targetUuid, final Set<UUID> list) {
        ignoredChat.put(targetUuid, list);
    }


    public static void addToIgnoredPrivate(final UUID targetUuid, final Set<UUID> list) {
        ignoredPrivate.put(targetUuid, list);
    }

    public static void addToIgnoredChat(final UUID searchUuid, final UUID targetUuid) {
        ignoredChat.get(searchUuid).add(targetUuid);
    }

    public static void addToIgnoredPrivate(final UUID searchUuid, final UUID targetUuid) {
        ignoredPrivate.get(searchUuid).add(targetUuid);
    }

    public static void removeFromIgnoredChat(final UUID targetUuid) {
        ignoredChat.remove(targetUuid);
    }

    public static void removeFromIgnoredPrivate(final UUID targetUuid) {
        ignoredPrivate.remove(targetUuid);
    }

    public static void removeFromIgnoredChat(final UUID searchUuid, final UUID targetUuid) {
        ignoredChat.get(searchUuid).remove(targetUuid);
    }

    public static void removeFromIgnoredPrivate(final UUID searchUuid, final UUID targetUuid) {
        ignoredPrivate.get(searchUuid).remove(targetUuid);
    }

    public static boolean isIgnoredChat(final UUID searchUuid, final UUID targetUuid) {
        if (ignoredChat.isEmpty() || !ignoredChat.containsKey(searchUuid)) {
            return false;
        }

        return ignoredChat.get(searchUuid).contains(targetUuid);
    }

    public static boolean isIgnoredPrivate(final UUID searchUuid, final UUID targetUuid) {
        if (ignoredPrivate.isEmpty() || !ignoredPrivate.containsKey(searchUuid)) {
            ignoredPrivate.put(searchUuid, new HashSet<>());
            return false;
        }

        return ignoredPrivate.get(searchUuid).contains(targetUuid);
    }
}
