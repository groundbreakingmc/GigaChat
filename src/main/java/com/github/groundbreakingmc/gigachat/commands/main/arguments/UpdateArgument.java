package com.github.groundbreakingmc.gigachat.commands.main.arguments;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.Argument;
import com.github.groundbreakingmc.mylib.updateschecker.UpdatesChecker;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public final class UpdateArgument extends Argument {

    public UpdateArgument(final GigaChat plugin) {
        super(plugin, "update", "gigachat.command.spy.other");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("§4[GigaChat] §cThis command can only be executed only by the console!");
            return true;
        }

        final UpdatesChecker updatesChecker = super.getPlugin().getUpdatesChecker();

        super.getPlugin().getPluginCommandLogger().log(() ->
                "[UPDATE] [CONSOLE] has update: " + updatesChecker.hasUpdate()
        );

        if (!updatesChecker.hasUpdate()) {
            sender.sendMessage("§4[GigaChat] §cNothing to update!");
            return true;
        }

        super.getPlugin().getServer().getScheduler().runTaskAsynchronously(super.getPlugin(), () ->
                updatesChecker.downloadJar(true)
        );

        return true;
    }
}
