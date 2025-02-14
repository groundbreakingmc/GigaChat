package com.github.groundbreakingmc.gigachat.commands.main.arguments;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.Argument;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ReloadArgument extends Argument {

    public ReloadArgument(final GigaChat plugin) {
        super(plugin, "reload", "gigachat.command.reload");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        final long startTime = System.currentTimeMillis();

        if (args.length == 1) {
            this.reloadAll();
        } else {
            this.reloadSpecified(args);
        }

        final String reloadTime = String.valueOf(System.currentTimeMillis() - startTime);
        final String message = super.getMessages().getReloadMessage().replace("{time}", reloadTime);
        if (sender instanceof Player) {
            super.getPlugin().getServer().getConsoleSender().sendMessage(message);
        }
        sender.sendMessage(message);
        return true;
    }

    private void reloadAll() {
        super.getPlugin().getAutoMessages().cancel();
        super.getPlugin().reloadConfig();
        super.getPlugin().setupConfigValues();
        super.getPlugin().getCooldownCollections().setCooldowns();
        super.getPlugin().getAutoMessages().run();
    }

    private void reloadSpecified(final String[] args) {
        for (final String arg : ImmutableSet.copyOf(args)) { // ImmutableSet needs to remove all duplicates
            if (arg.equalsIgnoreCase("auto-messages")) {
                super.getPlugin().getAutoMessages().cancel();
                super.getPlugin().getAutoMessagesValues().setValues();
                super.getPlugin().getAutoMessages().run();
            }
            if (arg.equalsIgnoreCase("broadcast")) {
                super.getPlugin().getBroadcastValues().setupValues();
            }
            if (arg.equalsIgnoreCase("chats")) {
                super.getPlugin().getChatValues().setupValues();
            }
            if (arg.equalsIgnoreCase("config")) {
                super.getPlugin().getConfigValues().setupValues();
            }
            if (arg.equalsIgnoreCase("messages")) {
                super.getPlugin().getMessages().setupMessages();
            }
            if (arg.equalsIgnoreCase("newbie-chat") && Bukkit.getPluginManager().getPlugin("NewbieGuard") != null) {
                super.getPlugin().getNewbieCommandsValues().setValues();
            }
            if (arg.equalsIgnoreCase("newbie-commands") && Bukkit.getPluginManager().getPlugin("NewbieGuard") != null) {
                super.getPlugin().getNewbieCommandsValues().setValues();
            }
            if (arg.equalsIgnoreCase("private-messages")) {
                super.getPlugin().getPmValues().setValues();
            }
        }
    }
}
