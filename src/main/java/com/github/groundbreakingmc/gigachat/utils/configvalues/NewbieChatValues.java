package com.github.groundbreakingmc.gigachat.utils.configvalues;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.listeners.NewbieChatListener;
import com.github.groundbreakingmc.gigachat.utils.counter.Counter;
import com.github.groundbreakingmc.gigachat.utils.counter.FirstEntryCounter;
import com.github.groundbreakingmc.gigachat.utils.counter.OnlineTimeCounter;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.ColorizerFactory;
import com.github.groundbreakingmc.mylib.utils.event.ListenerRegisterUtil;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Getter
public final class NewbieChatValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private Counter counter;

    private boolean isGiveBypassPermissionEnabled;

    private int requiredTime;
    private int requiredTimeToGetBypassPerm;

    private String denyMessage;

    private SoundSettings denySound;

    public NewbieChatValues(final GigaChat plugin) {
        this.plugin = plugin;
    }

    public void setValues() {
        final FileConfiguration config = com.github.groundbreakingmc.mylib.config.ConfigLoader.builder(this.plugin, this.plugin.getCustomLogger())
                .fileName("newbie-chat.yml")
                .fileVersion(1.0)
                .fileVersionPath("settings.config-version")
                .build();

        this.setupSettings(config);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");

        final NewbieChatListener newbieChatListener = this.plugin.getNewbieChatListener();
        if (settings.getBoolean("enable") && this.plugin.getServer().getPluginManager().getPlugin("NewbieGuard") == null) {
            final String priority = settings.getString("listener-priority").toUpperCase();
            final EventPriority eventPriority = ListenerRegisterUtil.getEventPriority(priority);
            if (eventPriority == null) {
                this.plugin.getCustomLogger().warn("Failed to parse value from \"settings.listener-priority\" from file \"\". Please check your configuration file, or delete it and restart your server!");
                this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            } else {
                final boolean ignoreCancelled = settings.getBoolean("ignore-cancelled", true);
                ListenerRegisterUtil.unregister(newbieChatListener);
                ListenerRegisterUtil.register(
                        this.plugin,
                        newbieChatListener,
                        AsyncPlayerChatEvent.class,
                        eventPriority,
                        ignoreCancelled,
                        (listener, event) -> newbieChatListener.onMessageSend((AsyncPlayerChatEvent) event)
                );
            }

            this.counter = settings.getBoolean("count-time-from-first-join") ? new FirstEntryCounter() : new OnlineTimeCounter();
            this.requiredTime = settings.getInt("required-time");
            this.isGiveBypassPermissionEnabled = settings.getBoolean("if-reached.give-permission");
            this.requiredTimeToGetBypassPerm = settings.getInt("if-reached.required-time");

            final Colorizer colorizer = ColorizerFactory.createColorizer(settings.getString("colorizer-mode"));
            this.denyMessage = colorizer.colorize(settings.getString("deny-message"));

            final String soundString = settings.getString("deny-sound");
            this.denySound = soundString == null || soundString.equalsIgnoreCase("disable") ? null : SoundSettings.get(soundString);
        } else {
            ListenerRegisterUtil.unregister(newbieChatListener);
        }
    }
}
