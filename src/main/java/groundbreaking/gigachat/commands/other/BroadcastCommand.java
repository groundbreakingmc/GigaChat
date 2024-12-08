package groundbreaking.gigachat.commands.other;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.CooldownCollections;
import groundbreaking.gigachat.utils.Utils;
import groundbreaking.gigachat.utils.colorizer.messages.PermissionsColorizer;
import groundbreaking.gigachat.utils.config.values.BroadcastValues;
import groundbreaking.gigachat.utils.config.values.Messages;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class BroadcastCommand implements CommandExecutor, TabCompleter {

    private final GigaChat plugin;
    private final BroadcastValues broadcastValues;
    private final Messages messages;
    private final CooldownCollections cooldownCollections;
    private final ConsoleCommandSender consoleCommandSender;

    private final String[] placeholders = {"{player}", "{prefix}", "{suffix}", "{message}"};

    public BroadcastCommand(final GigaChat plugin) {
        this.plugin = plugin;
        this.broadcastValues = plugin.getBroadcastValues();
        this.messages = plugin.getMessages();
        this.cooldownCollections = plugin.getCooldownCollections();
        this.consoleCommandSender = plugin.getServer().getConsoleSender();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("gigachat.command.broadcast")) {
            sender.sendMessage(this.messages.getNoPermission());
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(this.messages.getBroadcastUsageError());
            return true;
        }

        final boolean isPlayerSender = sender instanceof Player;
        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            final UUID senderUUID = playerSender.getUniqueId();
            if (this.hasCooldown(playerSender, senderUUID)) {
                this.sendMessageHasCooldown(playerSender, senderUUID);
                return true;
            }
        }

        final List<Player> recipients = new ArrayList<>(Bukkit.getOnlinePlayers());
        final String[] replacementList = this.getPlaceholders(sender, args, isPlayerSender);
        final String message = this.getMessage(replacementList);

        if (isPlayerSender && this.broadcastValues.isHoverEnabled()) {
            this.sendHover((Player) sender, message, recipients, replacementList);
        } else {
            for (int i = 0; i < recipients.size(); i++) {
                final Player recipient = recipients.get(i);
                recipient.sendMessage(message);
                this.playerSound(recipient);
            }
        }

        if (isPlayerSender) {
            this.cooldownCollections.addCooldown(((Player) sender).getUniqueId(), this.cooldownCollections.getBroadcastCooldowns());
        }

        this.consoleCommandSender.sendMessage(message);

        return true;
    }

    private boolean hasCooldown(final Player playerSender, final UUID playerUUID) {
        return this.cooldownCollections.hasCooldown(playerSender, playerUUID, "gigachat.bypass.cooldown.broadcast", cooldownCollections.getBroadcastCooldowns());
    }

    private void sendMessageHasCooldown(final Player playerSender, final UUID playerUUID) {
        final long timeLeftInMillis = this.cooldownCollections.getBroadcastCooldowns().get(playerUUID) - System.currentTimeMillis();
        final int result = (int) (this.broadcastValues.getCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        playerSender.sendMessage(message);
    }

    private String[] getPlaceholders(final CommandSender sender, final String[] args, final boolean isPlayerSender) {
        final String name = sender.getName();
        String prefix = "";
        String suffix = "";
        final String message;

        final PermissionsColorizer colorizer = this.broadcastValues.getMessageColorizer();
        if (isPlayerSender) {
            final Player playerSender = (Player) sender;
            prefix = colorizer.colorize(this.plugin.getChat().getPlayerPrefix(playerSender));
            suffix = colorizer.colorize(this.plugin.getChat().getPlayerSuffix(playerSender));
            message = colorizer.colorize((Player) sender, String.join(" ", Arrays.copyOfRange(args, 0, args.length)).trim());
        } else {
            message = colorizer.colorize(String.join(" ", Arrays.copyOfRange(args, 0, args.length)).trim());
        }

        return new String[]{name, prefix, suffix, message};
    }

    private String getMessage(final String[] replacementList) {
        return Utils.replaceEach(this.broadcastValues.getFormat(), this.placeholders, replacementList);
    }

    private void sendHover(final Player sender, final String formattedMessage, final List<Player> recipients, final String[] replacementList) {
        final String hoverString = this.broadcastValues.getColorizer().colorize(
                Utils.replacePlaceholders(
                        sender,
                        Utils.replaceEach(this.broadcastValues.getHoverText(), this.placeholders, replacementList)
                )
        );
        final ClickEvent.Action hoverAction = ClickEvent.Action.valueOf(this.broadcastValues.getHoverAction());
        final String hoverValue = this.broadcastValues.getHoverValue().replace("{player}", sender.getName());

        final HoverEvent hoverText = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverString)));
        final ClickEvent clickEvent = new ClickEvent(hoverAction, hoverValue);
        final BaseComponent[] comp = TextComponent.fromLegacyText(formattedMessage);

        for (int i = 0; i < comp.length; i++) {
            comp[i].setHoverEvent(hoverText);
            comp[i].setClickEvent(clickEvent);
        }
        for (int i = 0; i < recipients.size(); i++) {
            final Player recipient = recipients.get(i);
            recipient.spigot().sendMessage(comp);
            this.playerSound(recipient);
        }
    }

    private void playerSound(final Player recipient) {
        final Location location = recipient.getLocation();
        final Sound sound = this.broadcastValues.getSound();
        final float soundVolume = this.broadcastValues.getSoundVolume();
        final float soundPitch = this.broadcastValues.getSoundPitch();

        recipient.playSound(location, sound, soundVolume, soundPitch);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
