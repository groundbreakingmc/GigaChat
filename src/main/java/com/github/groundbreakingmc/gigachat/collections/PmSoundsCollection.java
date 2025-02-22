package com.github.groundbreakingmc.gigachat.collections;

import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public final class PmSoundsCollection {

    private final Map<UUID, SoundSettings> sounds = new Object2ObjectOpenHashMap<>();
    private SoundSettings defaultSound;

    public void setDefaultSound(final SoundSettings defaultSound) {
        this.defaultSound = defaultSound;
    }

    @NotNull
    public SoundSettings getSound(final UUID uuid) {
        if (this.sounds.isEmpty()) {
            return this.defaultSound;
        }

        return this.sounds.getOrDefault(uuid, this.defaultSound);
    }

    public void setSound(final UUID uuid, final SoundSettings sound) {
        this.sounds.put(uuid, sound);
    }

    public void remove(final UUID uuid) {
        this.sounds.remove(uuid);
    }

    public boolean contains(final UUID uuid) {
        return this.sounds.containsKey(uuid);
    }
}
