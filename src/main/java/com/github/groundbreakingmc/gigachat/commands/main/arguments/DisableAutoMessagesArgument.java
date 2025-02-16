package com.github.groundbreakingmc.gigachat.commands.main.arguments;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.AutoMessagesCollection;
import com.github.groundbreakingmc.gigachat.constructors.Argument;
import com.github.groundbreakingmc.gigachat.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

public final class DisableAutoMessagesArgument extends Argument {

    public DisableAutoMessagesArgument(final GigaChat plugin) {
        super(plugin, "disableam", "gigachat.command.disableam.other");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(super.getMessages().getDisableAutoMessagesUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(super.getMessages().getPlayerNotFound());
            return true;
        }

        return this.process(sender, target);
    }

    private boolean process(final CommandSender sender, final Player target) {
        final UUID targetUUID = target.getUniqueId();
        if (AutoMessagesCollection.contains(targetUUID)) {
            return this.process(
                    AutoMessagesCollection::remove,
                    sender,
                    target,
                    Database.REMOVE_PLAYER_FROM_AUTO_MESSAGES,
                    super.getMessages().getAutoMessagesEnabledOther(),
                    super.getMessages().getAutoMessagesEnabledByOther(),
                    "enabled"
            );
        }

        return this.process(
                AutoMessagesCollection::add,
                sender,
                target,
                Database.ADD_PLAYER_TO_AUTO_MESSAGES,
                super.getMessages().getAutoMessagesDisabledOther(),
                super.getMessages().getAutoMessagesDisabledByOther(),
                "disabled"
        );
    }

    private boolean process(final Consumer<UUID> consumer,
                            final CommandSender sender,
                            final Player target,
                            final String query,
                            final String senderMessage,
                            final String targetMessage,
                            final String mode) {
        consumer.accept(target.getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
            try (final Connection connection = super.getDatabase().getConnection()) {
                super.getDatabase().executeUpdateQuery(query, connection, target.getUniqueId().toString());
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        });

        sender.sendMessage(senderMessage.replace("{player}", target.getName()));
        target.sendMessage(targetMessage.replace("{player}", sender.getName()));

        super.getPlugin().getPluginCommandLogger().log(() ->
                "[DISABLE-AUTO-MESSAGES] [" + sender.getName() + "] " + mode + "for " + target.getName()
        );

        return true;
    }
}
