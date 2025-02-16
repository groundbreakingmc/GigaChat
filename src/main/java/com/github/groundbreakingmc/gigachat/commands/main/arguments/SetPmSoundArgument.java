package com.github.groundbreakingmc.gigachat.commands.main.arguments;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.PmSoundsCollection;
import com.github.groundbreakingmc.gigachat.constructors.Argument;
import com.github.groundbreakingmc.gigachat.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public final class SetPmSoundArgument extends Argument {

    private final PmSoundsCollection pmSoundsCollection;

    public SetPmSoundArgument(final GigaChat plugin) {
        super(plugin, "setpmsound", "gigachat.command.setpmsound");
        this.pmSoundsCollection = plugin.getPmSoundsCollection();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 3) {
            sender.sendMessage(super.getMessages().getSetpmsoundUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(super.getMessages().getPlayerNotFound());
            return true;
        }

        final UUID targetUUID = target.getUniqueId();
        final boolean isSenderTarget = sender.equals(target);
        final boolean messageForTargetIsEmpty = super.getMessages().getPmSoundRemoved().isEmpty();

        if (args[2].equalsIgnoreCase("none")) {
            this.handlePmSoundRemoval(sender, target, targetUUID, isSenderTarget, messageForTargetIsEmpty);
            return true;
        }

        final Sound sound = this.getSoundOrNotify(sender, args[2]);
        if (sound == null) {
            return true;
        }

        this.pmSoundsCollection.setSound(targetUUID, sound);
        this.sendPmSoundMessage(sender, target, sound, isSenderTarget, messageForTargetIsEmpty);
        this.updateDatabase(targetUUID, sound.name(), Database.ADD_PLAYER_PM_SOUND_TO_PRIVATE_MESSAGES_SOUNDS);

        super.getPlugin().getPluginCommandLogger().log(() ->
                "[SET-PM-SOUND] [" + sender.getName() + "] set " + args[2] + " for " + target.getName()
        );

        return true;
    }

    private void handlePmSoundRemoval(final CommandSender sender, final Player target,
                                      final UUID targetUUID,
                                      final boolean isSenderTarget,
                                      final boolean messageForTargetIsEmpty) {
        this.pmSoundsCollection.remove(targetUUID);
        this.updateDatabase(targetUUID, null, Database.REMOVE_PLAYER_FROM_PRIVATE_MESSAGES_SOUNDS);

        if (!isSenderTarget || !messageForTargetIsEmpty) {
            sender.sendMessage(super.getMessages().getTargetPmSoundRemoved().replace("{player}", target.getName()));
        }

        if (!messageForTargetIsEmpty) {
            target.sendMessage(super.getMessages().getPmSoundRemoved());
        }

        super.getPlugin().getPluginCommandLogger().log(() ->
                "[SET-PM-SOUND] [" + sender.getName() + "] removed for " + target.getName()
        );
    }

    private Sound getSoundOrNotify(final CommandSender sender, final String soundName) {
        try {
            return Sound.valueOf(soundName);
        } catch (final IllegalArgumentException ex) {
            sender.sendMessage(super.getMessages().getSoundNotFound());
            return null;
        }
    }

    private void sendPmSoundMessage(final CommandSender sender, final Player target,
                                    final Sound sound, final boolean isSenderTarget,
                                    final boolean messageForTargetIsEmpty) {
        if (!isSenderTarget || !messageForTargetIsEmpty) {
            sender.sendMessage(super.getMessages().getTargetPmSoundSet()
                    .replace("{player}", target.getName())
                    .replace("{sound}", sound.name()));
        }

        if (!messageForTargetIsEmpty) {
            target.sendMessage(super.getMessages().getPmSoundSet()
                    .replace("{sound}", sound.name()));
        }
    }

    private void updateDatabase(final UUID targetUUID, final String soundName, final String query) {
        Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
            try (final Connection connection = super.getDatabase().getConnection()) {
                if (soundName == null) {
                    super.getDatabase().executeUpdateQuery(query, connection, targetUUID.toString());
                } else {
                    super.getDatabase().executeUpdateQuery(query, connection, targetUUID.toString(), soundName);
                }
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

}
