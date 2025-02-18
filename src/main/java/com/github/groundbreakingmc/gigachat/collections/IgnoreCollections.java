package com.github.groundbreakingmc.gigachat.collections;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@UtilityClass
public final class IgnoreCollections {

    private static final Map<UUID, Set<UUID>> ignoredChat = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, Set<UUID>> ignoredPrivate = new Object2ObjectOpenHashMap<>();

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

    public static boolean ignoredChatContains(final UUID searchUUID, final UUID targetUUID) {
        if (ignoredChat.isEmpty()) {
            return false;
        }

        final Set<UUID> temp = ignoredChat.get(searchUUID);
        return !temp.isEmpty() && temp.contains(targetUUID);
    }

    public static boolean ignoredPrivateContains(final UUID searchUUID, final UUID targetUUID) {
        if (ignoredPrivate.isEmpty()) {
            return false;
        }

        final Set<UUID> temp = ignoredPrivate.get(searchUUID);
        return !temp.isEmpty() && temp.contains(targetUUID);
    }

    public static void addToIgnoredChat(final UUID targetUuid, final Set<UUID> list) {
        ignoredChat.put(targetUuid, list);
    }


    public static void addToIgnoredPrivate(final UUID targetUuid, final Set<UUID> list) {
        ignoredPrivate.put(targetUuid, list);
    }

    public static void addToIgnoredChat(final UUID searchUuid, final UUID targetUuid) {
        ignoredChat.computeIfAbsent(searchUuid, value -> new HashSet<>()).add(targetUuid);
    }

    public static void addToIgnoredPrivate(final UUID searchUuid, final UUID targetUuid) {
        ignoredPrivate.computeIfAbsent(searchUuid, value -> new HashSet<>()).add(targetUuid);
    }

    public static void removeFromIgnoredChat(final UUID targetUuid) {
        ignoredChat.remove(targetUuid);
    }

    public static void removeFromIgnoredPrivate(final UUID targetUuid) {
        ignoredPrivate.remove(targetUuid);
    }

    public static void removeFromIgnoredChat(final UUID searchUuid, final UUID targetUuid) {
        final Set<UUID> temp = ignoredChat.get(searchUuid);
        if (temp == null) {
            return;
        }

        if (temp.isEmpty()) {
            ignoredChat.remove(searchUuid);
        }

        temp.remove(targetUuid);
    }

    public static void removeFromIgnoredPrivate(final UUID searchUuid, final UUID targetUuid) {
        final Set<UUID> temp = ignoredPrivate.get(searchUuid);
        if (temp == null) {
            return;
        }

        if (temp.isEmpty()) {
            ignoredPrivate.remove(searchUuid);
        }

        temp.remove(targetUuid);
    }

    public static boolean isIgnoredChat(final UUID searchUuid, final UUID targetUuid) {
        if (ignoredChat.isEmpty()) {
            return false;
        }

        final Set<UUID> ignored = ignoredChat.get(searchUuid);
        if (ignored == null) {
            return false;
        }

        if (ignored.isEmpty()) {
            ignoredChat.remove(searchUuid);
            return false;
        }

        return ignored.contains(targetUuid);
    }

    public static boolean isIgnoredPrivate(final UUID searchUuid, final UUID targetUuid) {
        if (ignoredPrivate.isEmpty()) {
            return false;
        }

        final Set<UUID> ignored = ignoredPrivate.get(searchUuid);
        if (ignored == null) {
            return false;
        }

        if (ignored.isEmpty()) {
            ignoredPrivate.remove(searchUuid);
            return false;
        }

        return ignored.contains(targetUuid);
    }
}
