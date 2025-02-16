package com.github.groundbreakingmc.gigachat.commands.main.arguments;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.Argument;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class DisableServerChatArgument extends Argument {

    @Getter
    private static boolean chatDisabled = false;

    public DisableServerChatArgument(final GigaChat plugin) {
        super(plugin, "disablechat", "gigachat.command.disablechat");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (chatDisabled) {
            sender.sendMessage(super.getMessages().getServerChatEnabled());
            chatDisabled = false;
        } else {
            sender.sendMessage(super.getMessages().getServerChatDisabled());
            chatDisabled = true;
        }

        super.getPlugin().getPluginCommandLogger().log(() ->
                "[DISABLE-CHAT] [" + sender.getName() + "] " + (chatDisabled ? "disabled" : "enabled")
        );

        return true;
    }
}