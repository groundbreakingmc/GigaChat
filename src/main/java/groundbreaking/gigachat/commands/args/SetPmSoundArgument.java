package groundbreaking.gigachat.commands.args;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.PmSoundsMap;
import groundbreaking.gigachat.constructors.ArgsConstructor;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.config.values.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SetPmSoundArgument extends ArgsConstructor {

    private final Messages messages;
    private final PmSoundsMap pmSoundsMap;

    public SetPmSoundArgument(final GigaChat plugin, String name, String permission) {
        super(name, permission);
        this.messages = plugin.getMessages();
        this.pmSoundsMap = plugin.getPmSoundsMap();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length != 3) {
            sender.sendMessage(this.messages.getSetpmsoundUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(this.messages.getPlayerNotFound());
            return true;
        }

        if (args[2].equalsIgnoreCase("none")) {
            this.pmSoundsMap.remove(target.getName());
            DatabaseQueries.removePlayerFromPmSounds(target.getName());

            final boolean messageForTargetIsEmpty = !this.messages.getPmSoundRemoved().isEmpty();
            if (sender != target || messageForTargetIsEmpty) {
                sender.sendMessage(this.messages.getTargetPmSoundRemoved()
                        .replace("{player}", target.getName())
                );
            }

            if (!messageForTargetIsEmpty) {
                target.sendMessage(this.messages.getPmSoundRemoved());
            }

            return true;
        }

        final Sound sound;
        try {
            sound = Sound.valueOf(args[2]);
        } catch (IllegalArgumentException ignore) {
            sender.sendMessage(this.messages.getSoundNotFound());
            return true;
        }

        this.pmSoundsMap.setSound(target.getName(), sound.name());

        final boolean messageForTargetIsEmpty = !this.messages.getPmSoundRemoved().isEmpty();
        if (sender != target || messageForTargetIsEmpty) {
            sender.sendMessage(this.messages.getTargetPmSoundSet()
                    .replace("{player}", target.getName())
                    .replace("{sound}", sound.name())
            );
        }

        if (!messageForTargetIsEmpty) {
            target.sendMessage(this.messages.getPmSoundSet()
                    .replace("{sound}", sound.name())
            );
        }

        return true;
    }
}
