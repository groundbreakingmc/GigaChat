package groundbreaking.gigachat.commands.args;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.commands.ArgsConstructor;
import groundbreaking.gigachat.utils.config.values.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ClearChatArgument extends ArgsConstructor {

    private final Messages messages;
    private final String clearMessage;

    public ClearChatArgument(final GigaChat plugin, String name, String permission) {
        super(name, permission);
        this.messages = plugin.getMessages();
        clearMessage = "\n ".repeat(100);
    }

    // todo проверить player != sender если отправитель игрок, ибо не факт, что жаба сама умеет кастовать
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {;

        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("gigachat.bypass.clearchat")) {
                player.sendMessage(clearMessage);
            }
            if (player != sender) {
                player.sendMessage(messages.getChatHasBeenClearedByAdministrator());
            }
        }

        sender.sendMessage(messages.getChatHasBeenClearedByAdministrator());
        return true;
    }
}