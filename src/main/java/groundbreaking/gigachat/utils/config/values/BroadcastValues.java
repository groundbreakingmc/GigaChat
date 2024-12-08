package groundbreaking.gigachat.utils.config.values;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.colorizer.basic.Colorizer;
import groundbreaking.gigachat.utils.colorizer.messages.BroadcastColorizer;
import groundbreaking.gigachat.utils.colorizer.messages.PermissionsColorizer;
import groundbreaking.gigachat.utils.config.ConfigLoader;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;

@Getter
public final class BroadcastValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private String format;

    private int cooldown;

    private boolean isHoverEnabled;

    private String hoverAction;
    private String hoverValue;
    private String hoverText;

    private boolean isSoundEnabled;

    private Sound sound;

    private float soundVolume;
    private float soundPitch;

    private Colorizer colorizer;

    private final PermissionsColorizer messageColorizer;

    public BroadcastValues(final GigaChat plugin) {
        this.plugin = plugin;
        this.messageColorizer = new BroadcastColorizer(plugin);
    }

    public void setValues() {
        final FileConfiguration config = new ConfigLoader(this.plugin).loadAndGet("broadcast", 1.0);

        final ConfigurationSection broadcast = config.getConfigurationSection("settings");
        if (broadcast != null) {
            this.colorizer = this.plugin.getColorizer(config, "settings.serializer");
            this.format = this.colorizer.colorize(broadcast.getString("format"));
            this.cooldown = broadcast.getInt("cooldown");

            this.setupHover(broadcast);
            this.setupSound(broadcast);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings\" from file \"broadcast.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupHover(final ConfigurationSection broadcast) {
        final ConfigurationSection hover = broadcast.getConfigurationSection("hover");
        if (hover != null) {
            this.isHoverEnabled = hover.getBoolean("enable");
            this.hoverAction = hover.getString("click-action");
            this.hoverValue = hover.getString("click-value");
            this.hoverText = hover.getString("text");
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings.hover\" from file \"broadcast.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupSound(final ConfigurationSection settings) {
        final String soundString = settings.getString("sound");
        if (soundString == null) {
            this.plugin.getMyLogger().warning("Failed to load sound on path \"settings.sound\" from file \"broadcast.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.isSoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isSoundEnabled = false;
        } else {
            this.isSoundEnabled = true;
            final String[] params = soundString.split(";");
            this.sound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.BLOCK_BREWING_STAND_BREW;
            this.soundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            this.soundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;
        }
    }
}
