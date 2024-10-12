package groundbreaking.gigachat.utils.config.values;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.colorizer.basic.IColorizer;
import groundbreaking.gigachat.utils.config.ConfigLoader;
import groundbreaking.gigachat.utils.counter.FirstEntryCounter;
import groundbreaking.gigachat.utils.counter.ICounter;
import groundbreaking.gigachat.utils.counter.OnlineTimeCounter;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NewbieCommandsValues {

    private final GigaChat plugin;

    @Getter
    private boolean isEnabled;

    @Getter
    private String priority;

    @Getter
    private ICounter counter;

    @Getter
    private boolean isGiveBypassPermissionEnabled;

    @Getter
    private int requiredTime, requiredTimeToGetBypassPerm;

    @Getter
    private String denyMessage;

    @Getter
    private boolean isDenySoundEnabled;

    @Getter
    private Sound denySound;

    @Getter
    private float denySoundVolume, denySoundPitch;

    @Getter
    private final List<String> blockedCommands = new ArrayList<>();

    public NewbieCommandsValues(final GigaChat plugin) {
        this.plugin = plugin;
    }

    public void setValues() {
        final FileConfiguration config = new ConfigLoader(this.plugin).loadAndGet("newbie-commands", 1.0);
        final IColorizer colorizer = this.plugin.getColorizer(config, "settings.serializer");

        this.setupSettings(config, colorizer);
    }

    private void setupSettings(final FileConfiguration config, final IColorizer colorizer) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings != null) {
            this.isEnabled = settings.getBoolean("enable");
            this.priority = settings.getString("listener-priority").toLowerCase(Locale.ENGLISH);
            this.counter = settings.getBoolean("count-time-from-first-join") ? new FirstEntryCounter() : new OnlineTimeCounter();
            this.requiredTime = settings.getInt("required-time");
            this.isGiveBypassPermissionEnabled = settings.getBoolean("if-reached.give-permission");
            this.requiredTimeToGetBypassPerm = settings.getInt("if-reached.required-time");
            this.denyMessage = colorizer.colorize(settings.getString("deny-message"));

            this.setupSound(settings);

            this.blockedCommands.clear();
            this.blockedCommands.addAll(settings.getStringList("blocked-commands"));
        }
        else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings\" from file \"newbie-commands.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupSound(final ConfigurationSection settings) {
        final String soundString = settings.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getMyLogger().warning("Failed to load sound on path \"settings.deny-sound\" from file \"newbie-chat.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.isDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isDenySoundEnabled = false;
        } else {
            this.isDenySoundEnabled = true;
            final String[] params = soundString.split(";");
            this.denySound = params.length == 1 && params[0] != null ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.BLOCK_BREWING_STAND_BREW;
            this.denySoundVolume = params.length == 2 && params[1] != null ? Float.parseFloat(params[1]) : 1.0f;
            this.denySoundPitch = params.length == 3 && params[2] != null ? Float.parseFloat(params[2]) : 1.0f;
        }
    }
}
