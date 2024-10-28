package groundbreaking.gigachat.commands.args;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.LocalSpyMap;
import groundbreaking.gigachat.constructors.ArgsConstructor;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.config.values.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class LocalSpyArgument extends ArgsConstructor {

    private final Messages messages;

    public LocalSpyArgument(final GigaChat plugin, final String name, final String permission) {
        super(name, permission);
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {

        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(this.messages.getPlayerOnly());
            return true;
        }

        final String senderName = playerSender.getName();

        if (LocalSpyMap.contains(senderName)) {
            LocalSpyMap.remove(senderName);
            DatabaseQueries.removePlayerFromLocalSpy(senderName);
            sender.sendMessage(this.messages.getLocalSpyDisabled());
        } else {
            LocalSpyMap.add(senderName);
            sender.sendMessage(this.messages.getLocalSpyEnabled());
        }

        return true;
    }
}
