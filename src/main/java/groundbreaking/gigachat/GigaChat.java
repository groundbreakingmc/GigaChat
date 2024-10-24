package groundbreaking.gigachat;

import groundbreaking.gigachat.automessages.AutoMessages;
import groundbreaking.gigachat.collections.Cooldowns;
import groundbreaking.gigachat.collections.DisabledPrivateMessages;
import groundbreaking.gigachat.collections.PmSounds;
import groundbreaking.gigachat.commands.MainCommandHandler;
import groundbreaking.gigachat.commands.args.*;
import groundbreaking.gigachat.commands.other.BroadcastCommand;
import groundbreaking.gigachat.commands.other.DisableOwnChatExecutor;
import groundbreaking.gigachat.database.DatabaseHandler;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.exceptions.UnsupportedPrioritySpecified;
import groundbreaking.gigachat.listeners.CommandListener;
import groundbreaking.gigachat.listeners.DisconnectListener;
import groundbreaking.gigachat.listeners.NewListener;
import groundbreaking.gigachat.listeners.NewbieChatListener;
import groundbreaking.gigachat.utils.ServerInfo;
import groundbreaking.gigachat.utils.colorizer.basic.*;
import groundbreaking.gigachat.utils.config.values.*;
import groundbreaking.gigachat.utils.logging.BukkitLogger;
import groundbreaking.gigachat.utils.logging.ILogger;
import groundbreaking.gigachat.utils.logging.PaperLogger;
import groundbreaking.gigachat.utils.vanish.*;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

@Getter
public final class GigaChat extends JavaPlugin {

    private Chat chat;

    private Permission perms;

    private AutoMessages autoMessages;

    private AutoMessagesValues autoMessagesValues;
    private BroadcastValues broadcastValues;
    private ChatValues chatValues;
    private Messages messages;
    private NewbieChatValues newbieChatValues;
    private NewbieCommandsValues newbieCommandsValues;
    private PrivateMessagesValues pmValues;

    private Cooldowns cooldowns;
    private PmSounds pmSounds;

    private DisabledPrivateMessages disabled;

    private IVanishChecker vanishChecker;

    private ILogger myLogger;

    private NewListener chatListener;
    private CommandListener commandListener;
    private NewbieChatListener newbieChatListener;

    @Override
    public void onEnable() {
        final long startTime = System.currentTimeMillis();

        final ServerInfo serverInfo = new ServerInfo();
        if (!serverInfo.isPaperOrFork()) {
            this.logPaperWarning();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        final int subVersion = serverInfo.getSubVersion(this);
        this.setupLogger(subVersion);
        this.logLoggerType();

        super.saveDefaultConfig();
        this.setupVanishChecker();
        this.loadClasses();
        this.setupAll();

        new DatabaseHandler(this).createConnection();
        DatabaseQueries.createTables();

        this.autoMessages.run();

        final ServicesManager servicesManager = super.getServer().getServicesManager();
        this.setupChat(servicesManager);
        this.setupPerms(servicesManager);

        this.registerEvents();
        this.registerCommands();
        this.registerBroadcastCommand();

        final long endTime = System.currentTimeMillis();
        this.myLogger.info("Plugin successfully started in " + (endTime - startTime) + "ms.");
    }

    @Override
    public void onDisable() {
        new DatabaseHandler(this).closeConnection();
    }

    private void logPaperWarning() {
        final Logger logger = super.getLogger();
        logger.warning("\u001b[91m=============== \u001b[31mWARNING \u001b[91m===============\u001b[0m");
        logger.warning("\u001b[91mThe plugin dev is against using Bukkit, Spigot etc.!\u001b[0m");
        logger.warning("\u001b[91mSwitch to Paper or its fork. To download Paper visit:\u001b[0m");
        logger.warning("\u001b[91mhttps://papermc.io/downloads/all\u001b[0m");
        logger.warning("\u001b[91m=======================================\u001b[0m");
    }

    private void logLoggerType() {
        if (this.myLogger instanceof PaperLogger) {
            this.myLogger.info("Plugin will use new ComponentLogger for logging.");
        } else if (this.myLogger instanceof BukkitLogger) {
            this.myLogger.info("Plugin will use default old BukkitLogger for logging. Because your server version is under 19!");
        }
    }

    private void setupLogger(final int subVersion) {
        this.myLogger = subVersion >= 19
                ? new PaperLogger(this)
                : new BukkitLogger(this);
    }

    private void setupChat(final ServicesManager servicesManager) {
        final RegisteredServiceProvider<Chat> chatProvider = servicesManager.getRegistration(Chat.class);
        if (chatProvider != null) {
            this.chat = chatProvider.getProvider();
        }
    }

    private void setupPerms(final ServicesManager servicesManager) {
        final RegisteredServiceProvider<Permission> permissionProvider = servicesManager.getRegistration(Permission.class);
        if (permissionProvider != null) {
            this.perms = permissionProvider.getProvider();
        }
    }

    private void loadClasses() {
        this.messages = new Messages(this);
        this.autoMessagesValues = new AutoMessagesValues(this);
        this.broadcastValues = new BroadcastValues(this);
        this.chatValues = new ChatValues(this);
        this.newbieChatValues = new NewbieChatValues(this);
        this.pmValues = new PrivateMessagesValues(this);
        this.newbieCommandsValues = new NewbieCommandsValues(this);
        this.cooldowns = new Cooldowns(this);
        this.disabled = new DisabledPrivateMessages();
        this.chatListener = new NewListener(this);
        this.commandListener = new CommandListener(this);
        this.newbieChatListener = new NewbieChatListener(this);
        this.autoMessages = new AutoMessages(this);
        this.pmSounds = new PmSounds(this);
    }

    public void setupAll() {
        this.messages.setupMessages();
        this.autoMessagesValues.setValues();
        this.broadcastValues.setValues();
        this.chatValues.setValues();
        final PluginManager pluginManager = super.getServer().getPluginManager();
        if (pluginManager.getPlugin("NewbieGuard") == null) {
            this.newbieChatValues.setValues();
            this.newbieCommandsValues.setValues();
        }
        this.pmValues.setValues();
        this.cooldowns.setCooldowns();
    }

    public void setupVanishChecker() {
        final String checker = super.getConfig().getString("vanish-provider", "SUPER_VANISH").toUpperCase(Locale.ENGLISH);
        final PluginManager pluginManager = super.getServer().getPluginManager();
        switch (checker) {
            case "SUPER_VANISH":
                if (pluginManager.getPlugin("SuperVanish") != null) {
                    this.myLogger.warning("SuperVanish will be used as vanish provider.");
                    this.vanishChecker = new SuperVanishChecker();
                    break;
                }
            case "ESSENTIALS":
                final Plugin essentials = pluginManager.getPlugin("Essentials");
                if (essentials != null) {
                    this.myLogger.warning("Essentials will be used as vanish provider.");
                    this.vanishChecker = new EssentialsChecker(essentials);
                    break;
                }
            case "CMI":
                if (pluginManager.getPlugin("CMI") != null) {
                    this.myLogger.warning("CMI will be used as vanish provider.");
                    this.vanishChecker = new CMIChecker();
                    break;
                }
            default:
                this.myLogger.warning("No vanish provider were found! Plugin will not check if the player is vanished.");
                this.myLogger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                this.vanishChecker = new NoChecker();
        }
    }

    private void registerEvents() {
        final PluginManager pluginManager = super.getServer().getPluginManager();
        pluginManager.registerEvents(new DisconnectListener(this), this);
        this.registerReloadableEvents();
    }

    public void registerReloadableEvents() {
        this.chatListener.registerEvent();
        this.commandListener.unregisterEvent();
        this.newbieChatListener.unregisterEvent();
        final PluginManager pluginManager = super.getServer().getPluginManager();
        if (pluginManager.getPlugin("NewbieGuard") == null) {
            this.commandListener.registerEvent();
            this.newbieChatListener.registerEvent();
        } else {
            this.myLogger.info("Newbie protections will be disabled because NewbieGuard is detected.");
        }
    }

    private void registerCommands() {
        final MainCommandHandler mainCommandHandler = new MainCommandHandler(this);
        super.getCommand("gigachat").setExecutor(mainCommandHandler);

        final String disableOwnChatCommand = super.getConfig().getString("disable-own-chat.command");
        final List<String> disableOwnChatAliases = super.getConfig().getStringList("disable-own-chat.aliases");
        final DisableOwnChatExecutor disableOwnChat = new DisableOwnChatExecutor(this);

        this.registerCommand(disableOwnChatCommand, disableOwnChatAliases, disableOwnChat, disableOwnChat);

        final ClearChatArgument clearChat = new ClearChatArgument(this, "clearchat", "gigachat.command.clearchat");
        final DisableServerChatArgument disableServerChat = new DisableServerChatArgument(this, "disablechat", "gigachat.command.disablechat");
        final LocalSpyArgument localSpy = new LocalSpyArgument(this, "localspy", "gigachat.command.localspy");
        final ReloadArgument reload = new ReloadArgument(this, "reload", "gigachat.command.reload");
        final SetPmSoundArgument pmSoundSetter = new SetPmSoundArgument(this, "setpmsound", "gigachat.command.setpmsound");

        mainCommandHandler.registerArgument(clearChat);
        mainCommandHandler.registerArgument(disableServerChat);
        mainCommandHandler.registerArgument(localSpy);
        mainCommandHandler.registerArgument(reload);
        mainCommandHandler.registerArgument(pmSoundSetter);
    }

    private void registerBroadcastCommand() {
        final String command = super.getConfig().getString("broadcast.command");
        final List<String> aliases = super.getConfig().getStringList("broadcast.aliases");
        final BroadcastCommand broadcast = new BroadcastCommand(this);

        this.registerCommand(command, aliases, broadcast, broadcast);
    }

    public void registerCommand(final String command, final List<String> aliases, final CommandExecutor commandExecutor, final TabCompleter tabCompleter) {
        try {
            final CommandMap commandMap = super.getServer().getCommandMap();
            final Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            final PluginCommand pluginCommand = constructor.newInstance(command, this);
            pluginCommand.setAliases(aliases);
            pluginCommand.setExecutor(commandExecutor);
            pluginCommand.setTabCompleter(tabCompleter);
            commandMap.register(super.getDescription().getName(), pluginCommand);
        } catch (final Exception ex) {
            this.myLogger.warning("Unable to register" + command + " command! " + ex);
            super.getServer().getPluginManager().disablePlugin(this);
        }
    }

    public IColorizer getColorizer(final FileConfiguration config, final String configPath) {
        final String colorizerMode = config.getString(configPath).toUpperCase();

        return switch (colorizerMode) {
            case "MINIMESSAGE" -> new MiniMessageColorizer();
            case "LEGACY" -> new LegacyColorizer();
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer();
            default -> new VanillaColorizer();
        };
    }

    public IColorizer getColorizerByVersion() {
        final ServerInfo serverInfo = new ServerInfo();
        final boolean is16OrAbove = serverInfo.getSubVersion(this) >= 16;
        return is16OrAbove
                ? new LegacyColorizer()
                : new VanillaColorizer();
    }

    public EventPriority getEventPriority(final String priority, final String fileName) {
        return switch (priority) {
            case "LOWEST" -> EventPriority.LOWEST;
            case "LOW" -> EventPriority.LOW;
            case "NORMAL" -> EventPriority.NORMAL;
            case "HIGH" -> EventPriority.HIGH;
            case "HIGHEST" -> EventPriority.HIGHEST;
            default -> {
                this.myLogger.warning("Failed to parse value from \"settings.listener-priority\" from file \"" + fileName + "\". Please check your configuration file, or delete it and restart your server!");
                this.myLogger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                throw new UnsupportedPrioritySpecified("Failed to get event priority, please check your configuration files!");
            }
        };
    }
}
