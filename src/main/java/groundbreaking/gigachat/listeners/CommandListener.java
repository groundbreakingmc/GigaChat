package groundbreaking.gigachat.listeners;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.config.values.NewbieCommandsValues;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class CommandListener implements Listener {

    private final GigaChat plugin;
    private final NewbieCommandsValues newbieValues;

    private boolean isRegistered = false;

    public CommandListener(final GigaChat plugin) {
        this.plugin = plugin;
        this.newbieValues = plugin.getNewbieCommandsValues();
    }

    @EventHandler
    public void onCommandUse(final PlayerCommandPreprocessEvent event) {
        this.processEvent(event);
    }

    public void registerEvent() {
        if (this.isRegistered || !this.newbieValues.isEnabled()) {
            this.unregisterEvent();
        }

        final Class<? extends Event> eventClass = PlayerCommandPreprocessEvent.class;
        final EventPriority eventPriority = this.plugin.getEventPriority(this.newbieValues.getPriority(), "newbie-commands.yml");

        this.plugin.getServer().getPluginManager().registerEvent(eventClass, this, eventPriority,
                (listener, event) -> this.onCommandUse((PlayerCommandPreprocessEvent) event),
                this.plugin
        );

        this.isRegistered = true;
    }

    public void unregisterEvent() {
        if (!this.isRegistered) {
            return;
        }

        HandlerList.unregisterAll(this);
        this.isRegistered = false;
    }

    private void processEvent(final PlayerCommandPreprocessEvent event) {
        if (!this.newbieValues.isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();

        if (player.hasPermission("gigachat.bypass.commandsnewbie")) {
            return;
        }

        final String enteredCommand = event.getMessage();

        long time = this.newbieValues.getCounter().count(player);

        if (this.newbieValues.isGiveBypassPermissionEnabled()) {
            if (time <= this.newbieValues.getRequiredTimeToGetBypassPerm()) {
                final String bypassPermission = "gigachat.bypass.commandsnewbie";
                this.plugin.getPerms().playerAdd(player, bypassPermission);
            }
        }

        if (time > this.newbieValues.getRequiredTime()) {
            return;
        }

        for (final String blockedCommand : this.newbieValues.getBlockedCommands()) {
            if (!this.isBlocked(enteredCommand, blockedCommand)) {
                return;
            }

            this.sendMessage(player, time);

            if (this.newbieValues.isDenySoundEnabled()) {
                this.playSound(player);
            }

            event.setCancelled(true);
            return;
        }
    }

    private void sendMessage(final Player player, final long time) {
        final String restTime = Utils.getTime((int) (this.newbieValues.getRequiredTime() - time));
        final String denyMessage = this.newbieValues.getDenyMessage().replace("{time}", restTime);
        player.sendMessage(denyMessage);
    }

    private void playSound(final Player player) {
        final Location playerLocation = player.getLocation();
        final Sound denySound = this.newbieValues.getDenySound();
        final float denySoundVolume = this.newbieValues.getDenySoundVolume();
        final float denySoundPitch = this.newbieValues.getDenySoundPitch();
        player.playSound(playerLocation, denySound, denySoundVolume, denySoundPitch);
    }

    private boolean isBlocked(final String command, final String cmd) {
        if (command.equalsIgnoreCase(cmd)) {
            return true;
        }

        final int length = Math.min(command.length(), cmd.length());

        for (int i = 0; i < length; i++) {
            final char currentChar = command.charAt(i);
            if (currentChar == ' ') {
                return true;
            }
            if (currentChar != cmd.charAt(i)) {
                return false;
            }
        }

        return true;
    }
}
