package com.github.groundbreakingmc.gigachat.utils.configvalues;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.commands.main.MainCommandExecutor;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.constructors.DefaultChat;
import com.github.groundbreakingmc.gigachat.utils.ChatUtil;
import com.github.groundbreakingmc.gigachat.utils.StringValidator;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.ChatColorizer;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.PermissionColorizer;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.ColorizerFactory;
import com.github.groundbreakingmc.mylib.config.ConfigLoader;
import com.github.groundbreakingmc.mylib.utils.event.ListenerRegisterUtil;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public final class ChatValues {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private Colorizer formatColorizer;

    private final PermissionColorizer chatsColorizer;

    private boolean isCharsValidatorBlockMessage;
    private boolean capsValidatorBlockMessageSend;
    private boolean wordsValidatorBlockMessageSend;

    private SoundSettings charValidatorDenySound;
    private SoundSettings capsValidatorDenySound;
    private SoundSettings wordsValidatorDenySound;

    private final StringValidator stringValidator = new StringValidator();

    private Map<Character, Chat> chats;
    private Chat defaultChat;

    public ChatValues(final GigaChat plugin) {
        this.plugin = plugin;
        this.chatsColorizer = new ChatColorizer(plugin);
    }

    public void setupValues() {
        final FileConfiguration config = ConfigLoader.builder(this.plugin, this.plugin.getCustomLogger())
                .fileName("chats.yml")
                .fileVersion(1.0)
                .fileVersionPath("settings.config-version")
                .setDefaults(false)
                .build();

        this.setupChats(config);
        this.setupSettings(config);
        this.setupValidators(config);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings != null) {
            final String priority = settings.getString("listener-priority").toUpperCase();
            final EventPriority eventPriority = ListenerRegisterUtil.getEventPriority(priority);
            if (eventPriority == null) {
                this.plugin.getCustomLogger().warn("Failed to parse value from \"settings.listener-priority\" from file \"\". Please check your configuration file, or delete it and restart your server!");
                this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            } else {
                final boolean ignoreCancelled = settings.getBoolean("ignore-cancelled", true);
                ListenerRegisterUtil.unregister(this.plugin.getChatListener());
                ListenerRegisterUtil.register(
                        this.plugin,
                        this.plugin.getChatListener(),
                        AsyncPlayerChatEvent.class,
                        eventPriority,
                        ignoreCancelled,
                        (listener, event) -> this.plugin.getChatListener().onMessageSend((AsyncPlayerChatEvent) event)
                );
            }

            this.formatColorizer = ColorizerFactory.createColorizer(config.getString("settings.format-colorizer-mode"));
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"settings\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupChats(final FileConfiguration config) {
        final ConfigurationSection chatsSection = config.getConfigurationSection("chats");
        if (chatsSection != null) {

            final Map<Character, Chat> chatsTemp = new HashMap<>();

            final Set<String> chatsKeys = chatsSection.getKeys(false);
            chatsKeys.remove("default");

            MainCommandExecutor.setChats(ImmutableList.copyOf(chatsKeys));

            final DefaultChat defaultChatConstructor = ChatUtil.createDefaultChat(this.plugin, chatsSection);

            for (final String key : chatsKeys) {
                final ConfigurationSection keySection = chatsSection.getConfigurationSection(key);
                if (keySection == null) {
                    this.plugin.getCustomLogger().warn("Failed to load section \"chats." + key + "\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
                    this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                    continue;
                }

                final Chat chat = ChatUtil.createChat(this.plugin, keySection, key, defaultChatConstructor);

                final String symbolString = keySection.getString("symbol");
                if (symbolString.equalsIgnoreCase("default")) {
                    this.defaultChat = chat;
                } else {
                    chatsTemp.put(symbolString.charAt(0), chat);
                }
            }

            this.chats = ImmutableMap.copyOf(chatsTemp);
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"chats\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupValidators(final FileConfiguration config) {
        final ConfigurationSection validators = config.getConfigurationSection("validators");
        if (validators != null) {
            // TODO Убрать это убожество
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
            this.isCharsValidatorBlockMessage = charsValidator.getBoolean("block-message");

            final boolean isCharsValidatorEnabled = charsValidator.getBoolean("enable");
            final char[] textValidatorAllowedChars = charsValidator.getString("allowed").toCharArray();
            final char textValidatorCensorshipChar = charsValidator.getString("censorship-char").charAt(0);
            this.stringValidator.setupCharsValidator(isCharsValidatorEnabled, textValidatorAllowedChars, textValidatorCensorshipChar);

            this.charValidatorDenySound = SoundSettings.get(charsValidator.getString("deny-sound"));
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"validators.chars\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupCapsValidator(final ConfigurationSection validators) {
        final ConfigurationSection caseValidator = validators.getConfigurationSection("caps");
        if (caseValidator != null) {
            this.capsValidatorBlockMessageSend = caseValidator.getBoolean("block-message-send");

            final boolean isCapsValidatorEnabled = caseValidator.getBoolean("enable");
            final int capsValidatorMaxPercentage = caseValidator.getInt("max-percent");
            this.stringValidator.setupCapsValidator(isCapsValidatorEnabled, capsValidatorMaxPercentage);

            this.capsValidatorDenySound = SoundSettings.get(caseValidator.getString("deny-sound"));
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"validators.caps\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    private void setupWordsValidator(final ConfigurationSection validators) {
        final ConfigurationSection wordsValidator = validators.getConfigurationSection("words");
        if (wordsValidator != null) {
            this.wordsValidatorBlockMessageSend = wordsValidator.getBoolean("block-message-send");

            final boolean isWordsValidatorEnabled = wordsValidator.getBoolean("enable");
            final List<String> wordsValidatorBlockedWords = ImmutableList.copyOf(wordsValidator.getStringList("blocked"));
            final char wordsValidatorCensorshipChar = wordsValidator.getString("censorship-char").charAt(0);
            this.stringValidator.setupWordsValidator(isWordsValidatorEnabled, wordsValidatorBlockedWords, wordsValidatorCensorshipChar);

            this.wordsValidatorDenySound = SoundSettings.get(wordsValidator.getString("deny-sound"));
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"validators.words\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }
}
