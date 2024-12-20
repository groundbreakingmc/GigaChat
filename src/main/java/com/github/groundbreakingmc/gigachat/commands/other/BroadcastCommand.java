package com.github.groundbreakingmc.gigachat.commands.other;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.CooldownCollections;
import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.gigachat.utils.HoverUtils;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.colorizer.basic.Colorizer;
import com.github.groundbreakingmc.gigachat.utils.colorizer.messages.PermissionColorizer;
import com.github.groundbreakingmc.gigachat.utils.config.values.BroadcastValues;
import com.github.groundbreakingmc.gigachat.utils.config.values.Messages;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BroadcastCommand implements CommandExecutor, TabCompleter {

    private final GigaChat plugin;
    private final BroadcastValues broadcastValues;
    private final Messages messages;
    private final CooldownCollections cooldownCollections;
    private final ConsoleCommandSender consoleCommandSender;

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
            if (this.hasCooldown(playerSender)) {
                this.sendMessageHasCooldown(playerSender);
                return true;
            }
        }

        final String message = this.getMessage(sender, args);

        if (isPlayerSender && this.broadcastValues.getHover().isEnabled()) {
            this.sendHover((Player) sender, message);
        } else {
            for (final Player recipient : Bukkit.getOnlinePlayers()) {
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

    private boolean hasCooldown(final Player sender) {
        return this.cooldownCollections.hasCooldown(sender, "gigachat.bypass.cooldown.broadcast", cooldownCollections.getBroadcastCooldowns());
    }

    private void sendMessageHasCooldown(final Player sender) {
        final long timeLeftInMillis = this.cooldownCollections.getBroadcastCooldowns()
                .get(sender.getUniqueId()) - System.currentTimeMillis();
        final int result = (int) (this.broadcastValues.getCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(result);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        sender.sendMessage(message);
    }

    private String getMessage(final CommandSender sender, final String[] args) {
        final String message;
        String prefix = "";
        String suffix = "";

        final PermissionColorizer colorizer = this.broadcastValues.getMessageColorizer();
        if (sender instanceof final Player player) {
            message = colorizer.colorize((Player) sender, String.join(" ", Arrays.copyOfRange(args, 0, args.length)).trim());
            prefix = this.plugin.getChat().getPlayerPrefix(player);
            suffix = this.plugin.getChat().getPlayerSuffix(player);
        } else {
            message = colorizer.colorize(String.join(" ", Arrays.copyOfRange(args, 0, args.length)).trim());
        }

        return Utils.replacePlaceholders(
                sender,
                this.broadcastValues.getFormat()
                        .replace("{player}", sender.getName())
                        .replace("{prefix}", prefix)
                        .replace("{suffix}", suffix)
                        .replace("{message}", message)
        );
    }

    private void sendHover(final Player sender, final String message) {
        final Hover hover = this.broadcastValues.getHover();
        final Chat chat = this.plugin.getChat();
        final String hoverText = hover.hoverText()
                .replace("{player}", sender.getName())
                .replace("{prefix}", chat.getPlayerPrefix(sender))
                .replace("{suffix}", chat.getPlayerSuffix(sender));
        final Colorizer colorizer = this.broadcastValues.getColorizer();
        final BaseComponent[] components = HoverUtils.get(sender, hover, hoverText, message, colorizer);
        for (final Player recipient : Bukkit.getOnlinePlayers()) {
            recipient.spigot().sendMessage(components);
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
