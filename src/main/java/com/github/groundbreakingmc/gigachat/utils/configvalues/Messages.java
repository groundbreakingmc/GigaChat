package com.github.groundbreakingmc.gigachat.utils.configvalues;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.ColorizerFactory;
import com.github.groundbreakingmc.mylib.config.ConfigLoader;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

@Getter
public final class Messages {

    @Getter(AccessLevel.NONE)
    private final GigaChat plugin;

    private String playerOnly;
    private String noPermission;
    private String reloadMessage;
    private String nobodyToAnswer;
    private String playerNotFound;
    private String soundNotFound;
    private String chatCooldownMessage;
    private String commandCooldownMessage;
    private String pmUsageError;
    private String ignoreUsageError;
    private String enchantedIgnoreUsageError;
    private String replyUsageError;
    private String setpmsoundUsageError;
    private String broadcastUsageError;
    private String disableAutoMessagesUsageError;
    private String spyUsageError;
    private String nonExistArgument;
    private String argumentUsageError;
    private String soundAdditionalArgs;
    private String cannotChatWithHimself;
    private String cannotIgnoreHimself;
    private String recipientIgnoresSender;
    private String senderIgnoresRecipient;
    private String hasDisabledPm;
    private String isAfk;
    private String spyEnabled;
    private String spyDisabled;
    private String chatIgnoreEnabled;
    private String chatIgnoreDisabled;
    private String ownChatEnabled;
    private String ownChatDisabled;
    private String privateIgnoreEnabled;
    private String privateIgnoreDisabled;
    private String pmEnabled;
    private String pmDisabled;
    private String chatHasBeenClearedByAdministrator;
    private String chatHasBeenCleared;
    private String serverChatEnabled;
    private String serverChatDisabled;
    private String serverChatIsDisabled;
    private String noOneHear;
    private String targetPmSoundSet;
    private String targetPmSoundRemoved;
    private String pmSoundSet;
    private String pmSoundRemoved;
    private String localSpyEnabled;
    private String localSpyDisabled;
    private String charsValidationFailedMessage;
    private String capsValidationFailedMessage;
    private String wordsValidationFailedMessage;
    private String helpMessage;
    private String autoMessagesEnabledOther;
    private String autoMessagesDisabledOther;
    private String autoMessagesEnabledByOther;
    private String autoMessagesDisabledByOther;
    private String autoMessagesEnabled;
    private String autoMessagesDisabled;
    private String chatsSpyEnabled;
    private String chatsSpyDisabled;
    private String chatsSpyEnabledOther;
    private String chatsSpyDisabledOther;
    private String chatsSpyEnabledByOther;
    private String chatsSpyDisabledByOther;
    private String chatNotFound;
    private Map<String, String> chatNames;

    @Getter
    private static String hours;
    @Getter
    private static String minutes;
    @Getter
    private static String seconds;

    public Messages(final GigaChat plugin) {
        this.plugin = plugin;
    }

    public void setupMessages() {
        final FileConfiguration config = ConfigLoader.builder(this.plugin, this.plugin.getCustomLogger())
                .fileName("messages.yml")
                .fileVersion(1.0)
                .fileVersionPath("settings.config-version")
                .build();

        final Colorizer colorizer = ColorizerFactory.createColorizer(config.getString("settings.colorizer-mode"));

        this.setupMessages(config, colorizer);
        this.setupTimes(config);
    }

    private void setupMessages(final FileConfiguration config, final Colorizer colorizer) {
        this.playerOnly = getMessage(config, "player-only", colorizer);
        this.noPermission = this.getMessage(config, "no-perm", colorizer);
        this.reloadMessage = this.getMessage(config, "reload", colorizer);
        this.nobodyToAnswer = this.getMessage(config, "nobody-to-answer", colorizer);
        this.playerNotFound = this.getMessage(config, "player-not-found", colorizer);
        this.soundNotFound = this.getMessage(config, "sound-not-found", colorizer);
        this.chatCooldownMessage = this.getMessage(config, "chat-cooldown-message", colorizer);
        this.commandCooldownMessage = this.getMessage(config, "command-cooldown-message", colorizer);
        this.pmUsageError = this.getMessage(config, "pm-usage-error", colorizer);
        this.ignoreUsageError = this.getMessage(config, "ignore-usage-error", colorizer);
        this.enchantedIgnoreUsageError = this.getMessage(config, "enchanted-ignore-usage-error", colorizer);
        this.replyUsageError = this.getMessage(config, "reply-usage-error", colorizer);
        this.setpmsoundUsageError = this.getMessage(config, "setpmsound-usage-error", colorizer);
        this.broadcastUsageError = this.getMessage(config, "broadcast-usage-error", colorizer);
        this.disableAutoMessagesUsageError = this.getMessage(config, "disable-auto-messages-usage-error", colorizer);
        this.spyUsageError = this.getMessage(config, "spy-usage-error", colorizer);
        this.nonExistArgument = this.getMessage(config, "non-exist-arg", colorizer);
        this.argumentUsageError = this.getMessage(config, "arg-usage-error", colorizer);
        this.soundAdditionalArgs = this.getMessage(config, "sound-additional-args", colorizer);
        this.cannotChatWithHimself = this.getMessage(config, "cannot-pm-himself", colorizer);
        this.cannotIgnoreHimself = this.getMessage(config, "cannot-ignore-himself", colorizer);
        this.recipientIgnoresSender = this.getMessage(config, "recipient-ignores-sender", colorizer);
        this.senderIgnoresRecipient = this.getMessage(config, "sender-ignores-himself", colorizer);
        this.hasDisabledPm = this.getMessage(config, "has-disabled-private-messages", colorizer);
        this.isAfk = this.getMessage(config, "is-afk", colorizer);
        this.spyEnabled = this.getMessage(config, "socialspy-enabled", colorizer);
        this.spyDisabled = this.getMessage(config, "socialspy-disabled", colorizer);
        this.chatIgnoreEnabled = this.getMessage(config, "chat-ignore-enabled", colorizer);
        this.chatIgnoreDisabled = this.getMessage(config, "chat-ignore-disabled", colorizer);
        this.ownChatEnabled = this.getMessage(config, "own-chat-enabled", colorizer);
        this.ownChatDisabled = this.getMessage(config, "own-chat-disabled", colorizer);
        this.privateIgnoreEnabled = this.getMessage(config, "private-ignore-enabled", colorizer);
        this.privateIgnoreDisabled = this.getMessage(config, "private-ignore-disabled", colorizer);
        this.pmEnabled = this.getMessage(config, "private-messages-enabled", colorizer);
        this.pmDisabled = this.getMessage(config, "private-messages-disabled", colorizer);
        this.chatHasBeenClearedByAdministrator = this.getMessage(config, "chat-has-been-cleared-by-administrator", colorizer);
        this.chatHasBeenCleared = this.getMessage(config, "chat-has-been-cleared", colorizer);
        this.serverChatEnabled = this.getMessage(config, "server-chat-enabled", colorizer);
        this.serverChatDisabled = this.getMessage(config, "server-chat-disabled", colorizer);
        this.serverChatIsDisabled = this.getMessage(config, "server-chat-is-disabled", colorizer);
        this.noOneHear = this.getMessage(config, "no-one-hear", colorizer);
        this.targetPmSoundSet = this.getMessage(config, "target-pm-sound-set", colorizer);
        this.targetPmSoundRemoved = this.getMessage(config, "target-pm-sound-removed", colorizer);
        this.pmSoundSet = this.getMessage(config, "pm-sound-set", colorizer);
        this.pmSoundRemoved = this.getMessage(config, "pm-sound-removed", colorizer);
        this.localSpyEnabled = this.getMessage(config, "local-spy-enabled", colorizer);
        this.localSpyDisabled = this.getMessage(config, "local-spy-disabled", colorizer);
        this.charsValidationFailedMessage = this.getMessage(config, "chars-check-failed-message", colorizer);
        this.capsValidationFailedMessage = this.getMessage(config, "caps-check-failed-message", colorizer);
        this.wordsValidationFailedMessage = this.getMessage(config, "words-check-failed-message", colorizer);
        this.helpMessage = this.getMessage(config, "help-message", colorizer);
        this.autoMessagesEnabledOther = this.getMessage(config, "auto-messages-enabled-other", colorizer);
        this.autoMessagesDisabledOther = this.getMessage(config, "auto-messages-disabled-other", colorizer);
        this.autoMessagesEnabledByOther = this.getMessage(config, "auto-messages-enabled-by-other", colorizer);
        this.autoMessagesDisabledByOther = this.getMessage(config, "auto-messages-disabled-by-other", colorizer);
        this.autoMessagesEnabled = this.getMessage(config, "auto-messages-enabled", colorizer);
        this.autoMessagesDisabled = this.getMessage(config, "auto-messages-disabled", colorizer);

        final ConfigurationSection chatsSpyMode = config.getConfigurationSection("chats-spy-mode");
        if (chatsSpyMode != null) {
            this.chatsSpyEnabled = this.getMessage(chatsSpyMode, "enabled", "chats-spy-mode.enabled", colorizer);
            this.chatsSpyDisabled = this.getMessage(chatsSpyMode, "disabled", "chats-spy-mode.disabled", colorizer);
            this.chatsSpyEnabledOther = this.getMessage(chatsSpyMode, "enabled-other", "chats-spy-mode.enabled-other", colorizer);
            this.chatsSpyDisabledOther = this.getMessage(chatsSpyMode, "disabled-other", "chats-spy-mode.disabled-other", colorizer);
            this.chatsSpyEnabledByOther = this.getMessage(chatsSpyMode, "enabled-by-other", "chats-spy-mode.enabled-by-other", colorizer);
            this.chatsSpyDisabledByOther = this.getMessage(chatsSpyMode, "disabled-by-other", "chats-spy-mode.disabled-by-other", colorizer);
            this.chatNotFound = this.getMessage(chatsSpyMode, "chat-not-found", "chats-spy-mode.chat-not-found", colorizer);

            final ConfigurationSection chats = chatsSpyMode.getConfigurationSection("chats");
            if (chats != null) {
                final Map<String, String> temp = new HashMap<>();
                for (final String key : chats.getKeys(false)) {
                    temp.put(key, chats.getString(key, key));
                }
                this.chatNames = ImmutableMap.copyOf(temp);
            } else {
                this.plugin.getCustomLogger().warn("Failed to load section \"chats-spy-mode.chats\" from file \"messages.yml\". Please check your configuration file, or delete it and restart your server!");
                this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            }
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"chats-spy-mode\" from file \"messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }

    public String getMessage(final FileConfiguration config, final String path, final Colorizer colorizer) {
        final String message = config.getString(path, "&4(!) &cFailed to get message from: " + path);
        return colorizer.colorize(message);
    }

    public String getMessage(final ConfigurationSection section, final String path, final String fullPath, final Colorizer colorizer) {
        final String message = section.getString(path, "&4(!) &cFailed to get message from: " + fullPath);
        return colorizer.colorize(message);
    }

    private void setupTimes(final FileConfiguration config) {
        final ConfigurationSection time = config.getConfigurationSection("time");
        if (time != null) {
            hours = time.getString("hours");
            minutes = time.getString("minutes");
            seconds = time.getString("seconds");
        } else {
            this.plugin.getCustomLogger().warn("Failed to load section \"time\" from file \"messages.yml\". Please check your configuration file, or delete it and restart your server!");
            this.plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
        }
    }
}
