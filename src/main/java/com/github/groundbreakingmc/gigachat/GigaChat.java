package com.github.groundbreakingmc.gigachat;

import com.github.groundbreakingmc.gigachat.automessages.AutoMessages;
import com.github.groundbreakingmc.gigachat.collections.CooldownCollections;
import com.github.groundbreakingmc.gigachat.collections.DisabledPrivateMessagesCollection;
import com.github.groundbreakingmc.gigachat.collections.PmSoundsCollection;
import com.github.groundbreakingmc.gigachat.commands.main.MainCommandExecutor;
import com.github.groundbreakingmc.gigachat.database.Database;
import com.github.groundbreakingmc.gigachat.exceptions.NotPaperException;
import com.github.groundbreakingmc.gigachat.listeners.ChatListener;
import com.github.groundbreakingmc.gigachat.listeners.DisconnectListener;
import com.github.groundbreakingmc.gigachat.listeners.NewbieChatListener;
import com.github.groundbreakingmc.gigachat.listeners.NewbieCommandListener;
import com.github.groundbreakingmc.gigachat.utils.configvalues.*;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.LegacyColorizer;
import com.github.groundbreakingmc.mylib.colorizer.VanillaColorizer;
import com.github.groundbreakingmc.mylib.logger.LegacyLogger;
import com.github.groundbreakingmc.mylib.logger.Logger;
import com.github.groundbreakingmc.mylib.logger.LoggerFactory;
import com.github.groundbreakingmc.mylib.logger.ModernLogger;
import com.github.groundbreakingmc.mylib.updateschecker.UpdatesChecker;
import com.github.groundbreakingmc.mylib.utils.server.ServerInfo;
import com.github.groundbreakingmc.mylib.utils.vault.VaultUtils;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class GigaChat extends JavaPlugin {

    private static boolean loaded = false;

    private final Logger customLogger;
    private final Colorizer versionColorizer;

    private Chat chat;
    private Permission perms;

    private final AutoMessages autoMessages;

    private final AutoMessagesValues autoMessagesValues;
    private final BroadcastValues broadcastValues;
    private final ChatValues chatValues;
    private final ConfigValues configValues;
    private final Messages messages;
    private final NewbieChatValues newbieChatValues;
    private final NewbieCommandsValues newbieCommandsValues;
    private final PrivateMessagesValues pmValues;

    private final CooldownCollections cooldownCollections;
    private final PmSoundsCollection pmSoundsCollection;

    private final DisabledPrivateMessagesCollection disabled;

    private final ChatListener chatListener;
    private final NewbieCommandListener newbieCommandListener;
    private final NewbieChatListener newbieChatListener;

    private final Database database;

    private final UpdatesChecker updatesChecker;

    public GigaChat() {
        if (!ServerInfo.isPaperOrFork()) {
            throw new NotPaperException(this);
        }

        if (loaded) {
            throw new UnsupportedOperationException("Plugin can not be loaded 2 times!");
        }

        loaded = true;

        this.customLogger = LoggerFactory.createLogger(this);
        this.versionColorizer = ServerInfo.getSubVersion(this) >= 16
                ? new LegacyColorizer()
                : new VanillaColorizer();
        this.database = new Database(this);
        this.updatesChecker = new UpdatesChecker(
                this,
                this.customLogger,
                "https://raw.githubusercontent.com/groundbreakingmc/GigaChat/main/update",
                "https://github.com/grounbreakingmc/GigaChat/issues",
                "gigachat update"
        );

        this.messages = new Messages(this);
        this.autoMessagesValues = new AutoMessagesValues(this);
        this.broadcastValues = new BroadcastValues(this);
        this.chatValues = new ChatValues(this);
        this.configValues = new ConfigValues(this);
        this.newbieChatValues = new NewbieChatValues(this);
        this.pmValues = new PrivateMessagesValues(this);
        this.newbieCommandsValues = new NewbieCommandsValues(this);
        this.cooldownCollections = new CooldownCollections(this);
        this.disabled = new DisabledPrivateMessagesCollection();
        this.chatListener = new ChatListener(this);
        this.newbieCommandListener = new NewbieCommandListener(this);
        this.newbieChatListener = new NewbieChatListener(this);
        this.autoMessages = new AutoMessages(this);
        this.pmSoundsCollection = new PmSoundsCollection();
    }

    @Override
    public void onEnable() {
        final long startTime = System.currentTimeMillis();

        new Metrics(this, 23871);

        this.logLoggerType();

        this.chat = VaultUtils.getChatProvider();
        this.perms = VaultUtils.getPermissionProvider();

        super.saveDefaultConfig();
        this.setupConfigValues();

        this.database.createDatabaseTables();

        this.autoMessages.run();

        this.registerEvents();
        this.registerMainPluginCommand();

        final long endTime = System.currentTimeMillis();
        this.customLogger.info("Plugin successfully started in " + (endTime - startTime) + "ms.");
    }

    @Override
    public void onDisable() {
        this.database.closeConnection();
        this.autoMessages.shutdown();
        super.getServer().getScheduler().cancelTasks(this);
    }

    private void logLoggerType() {
        if (this.customLogger instanceof ModernLogger) {
            this.customLogger.info("Plugin will use modern ComponentLogger for logging.");
        } else if (this.customLogger instanceof LegacyLogger) {
            this.customLogger.info("Plugin will use default legacy BukkitLogger for logging. Because your server version is under 19!");
        }
    }

    public void setupConfigValues() {
        this.configValues.setupValues();
        this.messages.setupMessages();
        this.autoMessagesValues.setValues();
        this.broadcastValues.setupValues();
        this.chatValues.setupValues();
        final PluginManager pluginManager = super.getServer().getPluginManager();
        if (pluginManager.getPlugin("NewbieGuard") != null) {
            this.newbieChatValues.setValues();
            this.newbieCommandsValues.setValues();
        } else {
            this.customLogger.info("Newbie protections will be disabled because NewbieGuard is detected.");
        }
        this.pmValues.setValues();
        this.cooldownCollections.setCooldowns();
    }

    private void registerEvents() {
        final PluginManager pluginManager = super.getServer().getPluginManager();
        pluginManager.registerEvents(new DisconnectListener(this), this);
    }

    private void registerMainPluginCommand() {
        final PluginCommand mainCommand = super.getCommand("gigachat");
        final MainCommandExecutor mainCommandExecutor = new MainCommandExecutor(this);
        mainCommand.setExecutor(mainCommandExecutor);
        mainCommand.setTabCompleter(mainCommandExecutor);
    }
}
