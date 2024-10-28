package groundbreaking.gigachat.commands.args;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.AutoMessagesMap;
import groundbreaking.gigachat.constructors.ArgsConstructor;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.config.values.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class DisableAutoMessagesArgument extends ArgsConstructor {

    private final Messages messages;

    public DisableAutoMessagesArgument(final GigaChat plugin, final String name, final String permission) {
        super(name, permission);
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length < 1) {
            sender.sendMessage(this.messages.getDisableAutoMessagesUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        return this.process(sender, target);
    }

    private boolean process(final CommandSender sender, final Player target) {
        final String senderName = sender.getName();
        final String targetName = target.getName();
        if (AutoMessagesMap.contains(targetName)) {
            sender.sendMessage(this.messages.getAutoMessagesEnabledOther().replace("{player}", targetName));
            target.sendMessage(this.messages.getAutoMessagesEnabledByOther().replace("{player}", senderName));
            AutoMessagesMap.remove(targetName);
            DatabaseQueries.removePlayerFromAutoMessages(targetName);
        } else {
            sender.sendMessage(this.messages.getAutoMessagesDisabledOther().replace("{player}", targetName));
            target.sendMessage(this.messages.getAutoMessagesDisabledByOther().replace("{player}", senderName));
            AutoMessagesMap.add(targetName);
        }

        return true;
    }
}