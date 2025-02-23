package com.github.groundbreakingmc.gigachat.utils.configvalues;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.commands.privatemessages.IgnoreExecutor;
import com.github.groundbreakingmc.gigachat.commands.privatemessages.PrivateMessageExecutor;
import com.github.groundbreakingmc.gigachat.commands.privatemessages.ReplyExecutor;
import com.github.groundbreakingmc.gigachat.commands.privatemessages.SocialSpyExecutor;
import com.github.groundbreakingmc.gigachat.constructors.CommandParams;
import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.gigachat.utils.StringValidator;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.PermissionColorizer;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.PrivateMessagesColorizer;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.ColorizerFactory;
import com.github.groundbreakingmc.mylib.config.ConfigLoader;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

@Getter
public final class PrivateMessagesValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private boolean printLogsToConsole;

    private int pmCooldown;
    private int ignoreCooldown;
    private int spyCooldown;

    private Sound textValidatorDenySound;
    private Sound capsValidatorDenySound;
    private Sound wordsValidatorDenySound;

    private float textValidatorDenySoundVolume;
    private float textValidatorDenySoundPitch;
    private float capsValidatorDenySoundVolume;
    private float capsValidatorDenySoundPitch;
    private float wordsValidatorDenySoundVolume;
    private float wordsValidatorDenySoundPitch;

    private String senderFormat;
    private String recipientFormat;
    private String socialSpyFormat;
    private String consoleFormat;

    private boolean receiveSoundEnabled;

    @Nullable
    private Hover pmHover;
    @Nullable
    private Hover spyHover;

    private boolean isCharsValidatorBlockMessage;
    private boolean isCharsValidatorDenySoundEnabled;
    private boolean capsValidatorBlockMessageSend;
    private boolean isCapsValidatorDenySoundEnabled;
    private boolean wordsValidatorBlockMessageSend;
    private boolean isWordsValidatorDenySoundEnabled;

    @Getter(AccessLevel.NONE)
    private CommandParams pmCommand;
    @Getter(AccessLevel.NONE)
    private CommandParams replyCommand;
    @Getter(AccessLevel.NONE)
    private CommandParams ignoreCommand;
    @Getter(AccessLevel.NONE)
    private CommandParams spyCommand;

    private Colorizer formatColorizer;
    private final PermissionColorizer messageColorizer;

    private final StringValidator stringValidator = new StringValidator();

    public PrivateMessagesValues(final GigaChat plugin) {
        this.plugin = plugin;
        this.messageColorizer = new PrivateMessagesColorizer(plugin);
    }

    public void setValues() {
        final FileConfiguration config = ConfigLoader.builder(this.plugin, this.plugin.getCustomLogger())
                .fileName("private-messages.yml")
                .fileVersion(1.0)
                .fileVersionPath("settings.config-version")
                .build();

        this.setupSettings(config);
        this.setupCommands(config);
        this.setupFormats(config);
        this.setupHovers(config);
        this.setupValidators(config);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings != null) {
            this.printLogsToConsole = settings.getBoolean("print-to-console");

            this.setupSound(settings);

            this.formatColorizer = ColorizerFactory.createColorizer(config.getString("settings.format-colorizer-mode"));
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"settings\" from file \"private-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCommands(final FileConfiguration config) {
        final ConfigurationSection pmCommand = config.getConfigurationSection("private-message-command");
        if (pmCommand != null) {
            if (this.pmCommand == null) {
                this.pmCommand = new CommandParams(this.plugin, new PrivateMessageExecutor(this.plugin));
            }
            this.pmCommand.process(pmCommand);
            this.pmCooldown = pmCommand.getInt("cooldown");
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"private-message-command\" from file \"private-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }

        final ConfigurationSection replyCommand = config.getConfigurationSection("reply-command");
        if (replyCommand != null) {
            if (this.replyCommand == null) {
                this.replyCommand = new CommandParams(this.plugin, new ReplyExecutor(this.plugin));
            }
            this.replyCommand.process(replyCommand);
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"reply-command\" from file \"private-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }

        final ConfigurationSection ignoreCommand = config.getConfigurationSection("ignore-command");
        if (ignoreCommand != null) {
            if (this.ignoreCommand == null) {
                this.ignoreCommand = new CommandParams(this.plugin, new IgnoreExecutor(this.plugin));
            }
            this.ignoreCommand.process(ignoreCommand);
            this.ignoreCooldown = ignoreCommand.getInt("cooldown");
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"ignore-command\" from file \"private-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }

        final ConfigurationSection spyCommand = config.getConfigurationSection("socialspy-command");
        if (spyCommand != null) {
            if (this.spyCommand == null) {
                this.spyCommand = new CommandParams(this.plugin, new SocialSpyExecutor(this.plugin));
            }
            this.spyCommand.process(spyCommand);
            this.spyCooldown = spyCommand.getInt("cooldown");
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"socialspy-command\" from file \"private-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupFormats(final FileConfiguration config) {
        final ConfigurationSection formats = config.getConfigurationSection("formats");
        if (formats != null) {
            this.senderFormat = formats.getString("sender-format");
            this.recipientFormat = formats.getString("recipient-format");
            this.socialSpyFormat = formats.getString("socialspy-format");
            this.consoleFormat = formats.getString("console-format");
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"formats\" from file \"private-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupSound(final ConfigurationSection settings) {
        final String soundString = settings.getString("sound");
        if (soundString != null) {
            final SoundSettings soundSettings = (this.receiveSoundEnabled = soundString.equalsIgnoreCase("disabled"))
                    ? null
                    : SoundSettings.get(soundString);

            this.plugin.getPmSoundsCollection().setDefaultSound(soundSettings);
        } else {
            this.plugin.getCustomLogger().warn("Failed to load sound on path \"settings.sound\" from file \"private-messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupHovers(final FileConfiguration config) {
        final ConfigurationSection hover = config.getConfigurationSection("hover");
        if (hover != null) {
            this.pmHover = Hover.get(hover.getConfigurationSection("private-messages"), null);
            this.spyHover = Hover.get(hover.getConfigurationSection("socialspy"), null);
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"validators\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupValidators(final FileConfiguration config) {
        final ConfigurationSection validators = config.getConfigurationSection("validators");
        if (validators != null) {
            this.setupCharsValidator(validators);
            this.setupCapsValidator(validators);
            this.setupWordsValidator(validators);
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"validators\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCharsValidator(final ConfigurationSection validators) {
        final ConfigurationSection charsValidator = validators.getConfigurationSection("chars");
        if (charsValidator != null) {
            final boolean isCharsValidatorEnabled = charsValidator.getBoolean("enable");
            this.isCharsValidatorBlockMessage = charsValidator.getBoolean("block-message");
            final char textValidatorCensorshipChar = charsValidator.getString("censorship-char").charAt(0);
            final char[] textValidatorAllowedChars = charsValidator.getString("allowed").toCharArray();

            this.stringValidator.setupCharsValidator(isCharsValidatorEnabled, textValidatorAllowedChars, textValidatorCensorshipChar);

            this.setupCharsValidatorDenySound(charsValidator);
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"validators.chars\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCapsValidator(final ConfigurationSection validators) {
        final ConfigurationSection caseValidator = validators.getConfigurationSection("caps");
        if (caseValidator != null) {
            final boolean isCapsValidatorEnabled = caseValidator.getBoolean("enable");
            final int capsValidatorMaxPercentage = caseValidator.getInt("max-percent");
            this.capsValidatorBlockMessageSend = caseValidator.getBoolean("block-message-send");

            this.stringValidator.setupCapsValidator(isCapsValidatorEnabled, capsValidatorMaxPercentage);

            this.setupCapsValidatorDenySound(caseValidator);
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"validators.caps\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupWordsValidator(final ConfigurationSection validators) {
        final ConfigurationSection wordsValidator = validators.getConfigurationSection("words");
        if (wordsValidator != null) {
            final boolean isWordsValidatorEnabled = wordsValidator.getBoolean("enable");
            final char wordsValidatorCensorshipChar = wordsValidator.getString("censorship-char").charAt(0);
            final List<String> wordsValidatorBlockedWords = wordsValidator.getStringList("blocked");
            this.wordsValidatorBlockMessageSend = wordsValidator.getBoolean("block-message-send");

            this.stringValidator.setupWordsValidator(isWordsValidatorEnabled, wordsValidatorBlockedWords, wordsValidatorCensorshipChar);

            this.setupWordsValidatorDenySound(wordsValidator);
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"validators.words\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCharsValidatorDenySound(final ConfigurationSection caseCheck) {
        final String soundString = caseCheck.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getCustomLogger().warn("Failed to load sound on path \"validators.chars.deny-sound\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.isCharsValidatorDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isCharsValidatorDenySoundEnabled = false;
        } else {
            final String[] params = soundString.split(";");
            this.textValidatorDenySound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.ENTITY_VILLAGER_NO;
            this.textValidatorDenySoundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            this.textValidatorDenySoundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;
            this.isCharsValidatorDenySoundEnabled = true;
        }
    }

    private void setupCapsValidatorDenySound(final ConfigurationSection capsValidator) {
        final String soundString = capsValidator.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getCustomLogger().warn("Failed to load sound on path \"validators.caps.deny-sound\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.isCapsValidatorDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isCapsValidatorDenySoundEnabled = false;
        } else {
            final String[] params = soundString.split(";");
            this.capsValidatorDenySound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.ENTITY_VILLAGER_NO;
            this.capsValidatorDenySoundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            this.capsValidatorDenySoundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;
            this.isCapsValidatorDenySoundEnabled = true;
        }
    }

    private void setupWordsValidatorDenySound(final ConfigurationSection wordsValidator) {
        final String soundString = wordsValidator.getString("deny-sound");
        if (soundString == null) {
            this.plugin.getCustomLogger().warn("Failed to load sound on path \"validators.words.deny-sound\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            this.isWordsValidatorDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isWordsValidatorDenySoundEnabled = false;
        } else {
            final String[] params = soundString.split(";");
            this.wordsValidatorDenySound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.ENTITY_VILLAGER_NO;
            this.wordsValidatorDenySoundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            this.wordsValidatorDenySoundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;
            this.isWordsValidatorDenySoundEnabled = true;
        }
    }
}
