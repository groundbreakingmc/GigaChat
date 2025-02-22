package com.github.groundbreakingmc.gigachat.commands.main;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.commands.main.arguments.*;
import com.github.groundbreakingmc.gigachat.constructors.Argument;
import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import com.google.common.collect.ImmutableMap;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MainCommandExecutor implements TabExecutor {

    @Setter
    public static List<String> chats;

    private final Messages messages;
    private final Map<String, Argument> arguments;

    public MainCommandExecutor(final GigaChat plugin) {
        this.messages = plugin.getMessages();
        this.arguments = this.getArguments(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            if (this.hasAnyPluginPermission(sender)) {
                sender.sendMessage(this.messages.getNonExistArgument());
            } else {
                sender.sendMessage(this.messages.getNoPermission());
            }
            return true;
        }

        final Argument argument = this.arguments.get(args[0]);

        if (argument == null) {
            sender.sendMessage(this.messages.getNonExistArgument());
            return true;
        }

        if (!sender.hasPermission(argument.getPermission())) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        return argument.execute(sender, args);
    }

    private boolean hasAnyPluginPermission(final CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }

        return sender.hasPermission("gigachat.command.clearchat")
                || sender.hasPermission("gigachat.command.disableam.other")
                || sender.hasPermission("gigachat.command.disablechat")
                || sender.hasPermission("gigachat.command.reload")
                || sender.hasPermission("gigachat.command.setpmsound")
                || sender.hasPermission("gigachat.command.spy.other");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> completions = new ArrayList<>();
        final String input = args[args.length - 1].toUpperCase();

        switch (args.length) {
            case 1 -> {
                if (sender.hasPermission("gigachat.command.clearchat") && StringUtil.startsWithIgnoreCase("clearchat", input)) {
                    completions.add("clearchat");
                }
                if (sender.hasPermission("gigachat.command.disableam.other") && StringUtil.startsWithIgnoreCase("disableam", input)) {
                    completions.add("disableam");
                }
                if (sender.hasPermission("gigachat.command.disablechat") && StringUtil.startsWithIgnoreCase("disablechat", input)) {
                    completions.add("disablechat");
                }
                if (sender.hasPermission("gigachat.command.reload") && StringUtil.startsWithIgnoreCase("reload", input)) {
                    completions.add("reload");
                }
                if (sender.hasPermission("gigachat.command.setpmsound") && StringUtil.startsWithIgnoreCase("setpmsound", input)) {
                    completions.add("setpmsound");
                }
                if (sender.hasPermission("gigachat.command.spy.other") && StringUtil.startsWithIgnoreCase("spy", input)) {
                    completions.add("spy");
                }
                if (sender instanceof ConsoleCommandSender && StringUtil.startsWithIgnoreCase("update", input)) {
                    completions.add("update");
                }
            }
            case 2 -> {
                if (sender.hasPermission("gigachat.command.disableam.other") && args[0].equalsIgnoreCase("disableam")
                        || sender.hasPermission("gigachat.command.setpmsound") && args[0].equalsIgnoreCase("setpmsound")
                        || sender.hasPermission("gigachat.command.spy.other") && args[0].equalsIgnoreCase("spy")) {
                    this.addPlayerNames(input, completions);
                }
            }
            case 3 -> {
                if (sender.hasPermission("gigachat.command.spy.other") && args[0].equalsIgnoreCase("spy")) {
                    return chats;
                } else if (sender.hasPermission("gigachat.command.setpmsound") && args[0].equalsIgnoreCase("setpmsound")) {
                    if (StringUtil.startsWithIgnoreCase("none", input)) {
                        completions.add("none");
                    }

                    for (final Sound sound : Sound.values()) {
                        final String soundName = sound.name();
                        if (StringUtil.startsWithIgnoreCase(soundName, input)) {
                            completions.add(soundName);
                        }
                    }
                }
            }
        }

        if (args.length > 1
                && completions.isEmpty()
                && sender.hasPermission("gigachat.command.reload")
                && args[0].equalsIgnoreCase("reload")) {
            this.reloadCompletion(input, completions);
        }

        return completions;
    }

    private void reloadCompletion(final String input, final List<String> completions) {
        if (StringUtil.startsWithIgnoreCase("auto-messages", input)) {
            completions.add("auto-messages");
        }
        if (StringUtil.startsWithIgnoreCase("broadcast", input)) {
            completions.add("broadcast");
        }
        if (StringUtil.startsWithIgnoreCase("chats", input)) {
            completions.add("chats");
        }
        if (StringUtil.startsWithIgnoreCase("config", input)) {
            completions.add("config");
        }
        if (StringUtil.startsWithIgnoreCase("messages", input)) {
            completions.add("messages");
        }
        final boolean newbieGuard = Bukkit.getPluginManager().isPluginEnabled("NewbieGuard");
        if (!newbieGuard && StringUtil.startsWithIgnoreCase("newbie-chat", input)) {
            completions.add("newbie-chat");
        }
        if (!newbieGuard && StringUtil.startsWithIgnoreCase("newbie-private", input)) {
            completions.add("newbie-private");
        }
        if (StringUtil.startsWithIgnoreCase("private-messages", input)) {
            completions.add("private-messages");
        }
    }

    private void addPlayerNames(final String input, final List<String> completions) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String playerName = player.getName();
            if (StringUtil.startsWithIgnoreCase(playerName, input)) {
                completions.add(playerName);
            }
        }
    }

    private Map<String, Argument> getArguments(final GigaChat plugin) {
        final Map<String, Argument> temp = new HashMap<>();
        temp.put("clearchat", new ClearChatArgument(plugin));
        temp.put("disableam", new DisableAutoMessagesArgument(plugin));
        temp.put("disablechat", new DisableServerChatArgument(plugin));
        temp.put("reload", new ReloadArgument(plugin));
        temp.put("setpmsound", new SetPmSoundArgument(plugin));
        temp.put("spy", new SpyArgument(plugin));
        temp.put("update", new UpdateArgument(plugin));

        return ImmutableMap.copyOf(temp);
    }
}
