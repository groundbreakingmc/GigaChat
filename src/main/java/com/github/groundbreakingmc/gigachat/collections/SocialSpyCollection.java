package com.github.groundbreakingmc.gigachat.collections;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

@UtilityClass
public final class SocialSpyCollection {

    private final Set<UUID> listening = new ObjectOpenHashSet<>();

    public boolean contains(final UUID uuid) {
        return listening.contains(uuid);
    }

    public void add(final UUID uuid) {
        listening.add(uuid);
    }

    public void remove(final UUID uuid) {
        listening.remove(uuid);
    }

    public void sendAll(final Player sender, final Player recipient, final String message) {
        for (final UUID uuid : listening) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null && player != sender && player != recipient) {
                player.sendMessage(message);
            }
        }
    }
}
