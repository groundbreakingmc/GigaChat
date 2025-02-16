package com.github.groundbreakingmc.gigachat.commands.main.arguments;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.Argument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ClearChatArgument extends Argument {

    private final String clearMessage;

    public ClearChatArgument(final GigaChat plugin) {
        super(plugin, "clearchat", "gigachat.command.clearchat");
        this.clearMessage = "\n ".repeat(100);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(sender)) {
                continue;
            }

            if (!player.hasPermission("gigachat.bypass.clearchat")) {
                player.sendMessage(this.clearMessage);
            }
            player.sendMessage(super.getMessages().getChatHasBeenClearedByAdministrator());
        }

        sender.sendMessage(super.getMessages().getChatHasBeenCleared());
        super.getPlugin().getPluginCommandLogger().log(() ->
                "[CLEAR-CHAT] [" + sender.getName() + "]"
        );
        return true;
    }
}