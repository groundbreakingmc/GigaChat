package com.github.groundbreakingmc.gigachat.automessages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.utils.configvalues.AutoMessagesValues;
import com.github.groundbreakingmc.mylib.collections.cases.Pair;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class AutoMessages {

    private final GigaChat plugin;
    private final AutoMessagesValues autoMessagesValues;

    private final List<Pair<String, SoundSettings>> autoMessagesClone = new ObjectArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> task;

    public AutoMessages(final GigaChat plugin) {
        this.plugin = plugin;
        this.autoMessagesValues = plugin.getAutoMessagesValues();
    }

    public void run() {
        if (task != null && !task.isCancelled()) {
            return;
        }

        int interval = this.autoMessagesValues.getSendInterval();
        task = scheduler.scheduleWithFixedDelay(() -> {
            final Pair<String, SoundSettings> autoMessageConstructor = AutoMessages.this.getAutoMessage();
            if (autoMessageConstructor == null) {
                return;
            }

            final String autoMessage = autoMessageConstructor.getLeft();
            final SoundSettings sound = autoMessageConstructor.getRight();
            AutoMessages.this.send(autoMessage, sound);
        }, 0, interval, TimeUnit.SECONDS);
    }

    public void cancel() {
        if (task != null) {
            ScheduledFuture<?> savedTask = task;
            task = null;

            scheduler.execute(() -> {
                savedTask.cancel(false);
                autoMessagesClone.clear();
            });
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void send(final String autoMessage, final SoundSettings soundSettings) {
        for (final Player target : Bukkit.getOnlinePlayers()) {
            target.sendMessage(autoMessage);
            if (soundSettings != null) {
                PlayerUtils.playSound(target, soundSettings);
            }
        }
    }

    private Pair<String, SoundSettings> getAutoMessage() {
        if (this.autoMessagesClone.isEmpty()) {
            final List<Pair<String, SoundSettings>> autoMessages = this.autoMessagesValues.getAutoMessages();
            if (autoMessages == null || autoMessages.isEmpty()) {
                this.plugin.getCustomLogger().warn("Failed to load \"auto-messages\". Please check your configuration file, or delete it and restart your server!");
                this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                cancel();
                return null;
            }

            this.autoMessagesClone.addAll(autoMessages);
            if (this.autoMessagesValues.isRandom()) {
                Collections.shuffle(this.autoMessagesClone);
            }
        }

        return this.autoMessagesClone.remove(0);
    }
}