package com.github.groundbreakingmc.gigachat.collections;

import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.gigachat.utils.HoverUtils;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

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

    public void sendAll(final Player sender, final String senderPrefix, final String senderSuffix,
                        final Player recipient, final String recipientPrefix, final String recipientSuffix,
                        final @Nullable Hover hover, final String message, final Colorizer formatColorizer) {
        if (hover != null && hover.isEnabled()) {
            final String hoverText = hover.hoverText()
                    .replace("{recipient}", sender.getName())
                    .replace("{recipient-prefix}", recipientPrefix)
                    .replace("{recipient-suffix}", recipientSuffix);

            final BaseComponent[] components = HoverUtils.get(sender, senderPrefix, senderSuffix, hover, hoverText, message, formatColorizer);

            for (final UUID uuid : listening) {
                final Player player = Bukkit.getPlayer(uuid);
                if (player != null && player != sender && player != recipient) {
                    player.sendMessage(components);
                }
            }
        } else {
            for (final UUID uuid : listening) {
                final Player player = Bukkit.getPlayer(uuid);
                if (player != null && player != sender && player != recipient) {
                    player.sendMessage(message);
                }
            }
        }
    }
}
