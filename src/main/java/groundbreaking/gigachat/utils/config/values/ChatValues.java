package groundbreaking.gigachat.utils.config.values;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.constructors.Chat;
import groundbreaking.gigachat.exceptions.FormatNullException;
import groundbreaking.gigachat.utils.StringUtil;
import groundbreaking.gigachat.utils.colorizer.basic.IColorizer;
import groundbreaking.gigachat.utils.colorizer.messages.AbstractColorizer;
import groundbreaking.gigachat.utils.colorizer.messages.ChatColorizer;
import groundbreaking.gigachat.utils.config.ConfigLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

@Getter
public final class ChatValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private String localFormat, localSpyFormat, globalFormat;

    private int localDistance;

    private int localCooldown, globalCooldown;

    private char globalSymbol;

    private boolean
            noOneHearEnabled,
            noOneHearHideHidden,
            noOneHearHideVanished,
            noOneHearHideSpectators;

    private boolean isGlobalForce;

    private final Map<String, String>
            localGroupsColors = new HashMap<>(),
            globalGroupsColors = new HashMap<>();

    private String priority;

    private IColorizer formatsColorizer;

    private final AbstractColorizer chatsColorizer;

    private boolean isHoverEnabled, isAdminHoverEnabled;

    private String
            hoverText, hoverAction, hoverValue,
            adminHoverText, adminHoverAction, adminHoverValue;

    private boolean
            isCharsValidatorBlockMessage, isCharsValidatorDenySoundEnabled,
            capsValidatorBlockMessageSend, isCapsValidatorDenySoundEnabled,
            wordsValidatorBlockMessageSend, isWordsValidatorDenySoundEnabled;

    private Sound
            textValidatorDenySound,
            capsValidatorDenySound,
            wordsValidatorDenySound;

    private float
            textValidatorDenySoundVolume, textValidatorDenySoundPitch,
            capsValidatorDenySoundVolume, capsValidatorDenySoundPitch,
            wordsValidatorDenySoundVolume, wordsValidatorDenySoundPitch;

    private final StringUtil stringUtil = new StringUtil();

    private final Object2ObjectOpenHashMap<Character, Chat> chats = new Object2ObjectOpenHashMap<>();
    private Chat defaultChat;

    public ChatValues(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatsColorizer = new ChatColorizer(plugin);
    }

    public void setValues() {
        final FileConfiguration config = new ConfigLoader(this.plugin).loadAndGet("chats", 1.0);

        this.setupChats(config);
        this.setupSettings(config);
        this.setupHover(config);
        this.setupAdminHover(config);
        this.setupValidators(config);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings != null) {
            this.priority = settings.getString("listener-priority").toUpperCase(Locale.ENGLISH);
            this.formatsColorizer = plugin.getColorizer(config, "settings.serializer-for-formats");
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"settings\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupChats(final FileConfiguration config) {
        final ConfigurationSection chats = config.getConfigurationSection("chats");
        if (chats != null) {
            final Set<String> chatsKeys = chats.getKeys(false);

            for (final String key : chatsKeys) {
                final ConfigurationSection keySection = chats.getConfigurationSection(key);
                final String format = keySection.getString("format");
                if (format == null) {
                    throw new FormatNullException("Chat format cannot be null! Path: \"chats." + key + ".format\"");
                }
                final String symbolString = keySection.getString("symbol");
                final String spyFormat = keySection.getString("spy-format");
                final int distance = keySection.getInt("distance");
                final int cooldown = keySection.getInt("cooldown", 1500);

                final ConfigurationSection noOneHeardYou = keySection.getConfigurationSection("no-one-heard-you");
                boolean isNoOneHeardEnabled = false;
                boolean isNoOneHeardHideHidden = true;
                boolean isNoOneHeardHideVanished = true;
                boolean isNoOneHeardHideSpectators = true;
                if (noOneHeardYou != null) {
                    isNoOneHeardEnabled = keySection.getBoolean("no-one-heard-you.enable");
                    isNoOneHeardHideHidden = keySection.getBoolean("no-one-heard-you.hide-hidden");
                    isNoOneHeardHideVanished = keySection.getBoolean("no-one-heard-you.hide-vanished");
                    isNoOneHeardHideSpectators = keySection.getBoolean("no-one-heard-you.hide-spectators");
                }

                final HashMap<String, String> groupsColors = new HashMap<>();

                final ConfigurationSection groupsColorsSection = keySection.getConfigurationSection("groups-colors");
                if (groupsColorsSection != null) {
                    for (final String groupKey : groupsColorsSection.getKeys(false)) {
                        groupsColors.put(groupKey, groupsColorsSection.getString(groupKey));
                    }
                }

                final Chat chat = new Chat(key, format, spyFormat, distance, cooldown, isNoOneHeardEnabled, isNoOneHeardHideHidden, isNoOneHeardHideVanished, isNoOneHeardHideSpectators, groupsColors);
                if (symbolString.equalsIgnoreCase("default")) {
                    defaultChat = chat;
                } else {
                    final char symbol = symbolString.charAt(0);
                    this.chats.put(symbol, chat);
                }
            }
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"chats\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupHover(final FileConfiguration config) {
        final ConfigurationSection hover = config.getConfigurationSection("hover");
        if (hover != null) {
            this.isHoverEnabled = hover.getBoolean("enable");
            this.hoverAction = hover.getString("click-action");
            this.hoverValue = hover.getString("click-value");
            this.hoverText = hover.getString("text");
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"hover\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupAdminHover(final FileConfiguration config) {
        final ConfigurationSection hover = config.getConfigurationSection("admin-hover");
        if (hover != null) {
            this.isAdminHoverEnabled = hover.getBoolean("enable");
            this.adminHoverAction = hover.getString("click-action");
            this.adminHoverValue = hover.getString("click-value");
            this.adminHoverText = hover.getString("text");
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"admin-hover\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupValidators(final FileConfiguration config) {
        final ConfigurationSection validators = config.getConfigurationSection("validators");
        if (validators != null) {
            this.setupCharsValidator(validators);
            this.setupCapsValidator(validators);
            this.setupWordsValidator(validators);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"validators\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCharsValidator(final ConfigurationSection validators) {
        final ConfigurationSection charsValidator = validators.getConfigurationSection("chars");
        if (charsValidator != null) {
            final boolean isCharsValidatorEnabled = charsValidator.getBoolean("enable");
            this.isCharsValidatorBlockMessage = charsValidator.getBoolean("block-message");
            final char textValidatorCensorshipChar = charsValidator.getString("censorship-char").charAt(0);
            final char[] textValidatorAllowedChars = charsValidator.getString("allowed").toCharArray();

            this.stringUtil.setupCharsValidator(isCharsValidatorEnabled, textValidatorAllowedChars, textValidatorCensorshipChar);

            this.setupCharsValidatorDenySound(charsValidator);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"validators.chars\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCapsValidator(final ConfigurationSection validators) {
        final ConfigurationSection caseValidator = validators.getConfigurationSection("caps");
        if (caseValidator != null) {
            final boolean isCapsValidatorEnabled = caseValidator.getBoolean("enable");
            final int capsValidatorMaxPercentage = caseValidator.getInt("max-percent");
            this.capsValidatorBlockMessageSend = caseValidator.getBoolean("block-message-send");

            this.stringUtil.setupCapsValidator(isCapsValidatorEnabled, capsValidatorMaxPercentage);

            this.setupCapsValidatorDenySound(caseValidator);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"validators.caps\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupWordsValidator(final ConfigurationSection validators) {
        final ConfigurationSection wordsValidator = validators.getConfigurationSection("words");
        if (wordsValidator != null) {
            final boolean isWordsValidatorEnabled = wordsValidator.getBoolean("enable");
            final char wordsValidatorCensorshipChar = wordsValidator.getString("censorship-char").charAt(0);
            final List<String> wordsValidatorBlockedWords = wordsValidator.getStringList("blocked");
            this.wordsValidatorBlockMessageSend = wordsValidator.getBoolean("block-message-send");

            this.stringUtil.setupWordsValidator(isWordsValidatorEnabled, wordsValidatorBlockedWords, wordsValidatorCensorshipChar);

            this.setupWordsValidatorDenySound(wordsValidator);
        } else {
            this.plugin.getMyLogger().warning("Failed to load section \"validators.words\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCharsValidatorDenySound(final ConfigurationSection caseCheck) {
        final String soundString = caseCheck.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getMyLogger().warning("Failed to load sound on path \"validators.chars.deny-sound\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.isCharsValidatorDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isCharsValidatorDenySoundEnabled = false;
        } else {
            final String[] params = soundString.split(";");
            this.textValidatorDenySound = params.length == 1 && params[0] != null ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.ENTITY_VILLAGER_NO;
            this.textValidatorDenySoundVolume = params.length == 2 && params[1] != null ? Float.parseFloat(params[1]) : 1.0f;
            this.textValidatorDenySoundPitch = params.length == 3 && params[2] != null ? Float.parseFloat(params[2]) : 1.0f;
            this.isCharsValidatorDenySoundEnabled = true;
        }
    }

    private void setupCapsValidatorDenySound(final ConfigurationSection capsValidator) {
        final String soundString = capsValidator.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getMyLogger().warning("Failed to load sound on path \"validators.caps.deny-sound\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.isCapsValidatorDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isCapsValidatorDenySoundEnabled = false;
        } else {
            final String[] params = soundString.split(";");
            this.capsValidatorDenySound = params.length == 1 && params[0] != null ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.ENTITY_VILLAGER_NO;
            this.capsValidatorDenySoundVolume = params.length == 2 && params[1] != null ? Float.parseFloat(params[1]) : 1.0f;
            this.capsValidatorDenySoundPitch = params.length == 3 && params[2] != null ? Float.parseFloat(params[2]) : 1.0f;
            this.isCapsValidatorDenySoundEnabled = true;
        }
    }

    private void setupWordsValidatorDenySound(final ConfigurationSection wordsValidator) {
        final String soundString = wordsValidator.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getMyLogger().warning("Failed to load sound on path \"validators.words.deny-sound\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.isWordsValidatorDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isWordsValidatorDenySoundEnabled = false;
        } else {
            final String[] params = soundString.split(";");
            this.wordsValidatorDenySound = params.length == 1 && params[0] != null ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.ENTITY_VILLAGER_NO;
            this.wordsValidatorDenySoundVolume = params.length == 2 && params[1] != null ? Float.parseFloat(params[1]) : 1.0f;
            this.wordsValidatorDenySoundPitch = params.length == 3 && params[2] != null ? Float.parseFloat(params[2]) : 1.0f;
            this.isWordsValidatorDenySoundEnabled = true;
        }
    }
}
