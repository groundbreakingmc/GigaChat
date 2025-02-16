package com.github.groundbreakingmc.gigachat.commands.privatemessages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.CooldownCollections;
import com.github.groundbreakingmc.gigachat.collections.SocialSpyCollection;
import com.github.groundbreakingmc.gigachat.database.Database;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import com.github.groundbreakingmc.gigachat.utils.configvalues.PrivateMessagesValues;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class SocialSpyExecutor implements TabExecutor {

    private final GigaChat plugin;
    private final Messages messages;
    private final Database database;
    private final PrivateMessagesValues pmValues;
    private final CooldownCollections cooldownCollections;

    public SocialSpyExecutor(final GigaChat plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.database = plugin.getDatabase();
        this.pmValues = plugin.getPmValues();
        this.cooldownCollections = plugin.getCooldownCollections();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player playerSender)) {
            sendMessage(sender, this.messages.getPlayerOnly());
            return true;
        }

        if (!playerSender.hasPermission("gigachat.command.socialspy")) {
            sendMessage(playerSender, this.messages.getNoPermission());
            return true;
        }

        if (this.hasCooldown(playerSender)) {
            this.sendCooldownMessage(playerSender);
            return true;
        }

        this.handleSocialSpyToggle(playerSender);
        this.cooldownCollections.addCooldown(playerSender.getUniqueId(), this.cooldownCollections.getSpyCooldowns());

        return true;
    }

    private void handleSocialSpyToggle(final Player sender) {
        final UUID senderUUID = sender.getUniqueId();
        if (SocialSpyCollection.contains(senderUUID)) {
            this.process(
                    SocialSpyCollection::remove,
                    sender, senderUUID,
                    Database.REMOVE_PLAYER_FROM_SOCIAL_SPY,
                    this.messages.getSpyDisabled(),
                    "disabled"
            );
            return;
        }

        this.process(
                SocialSpyCollection::add,
                sender, senderUUID,
                Database.ADD_PLAYER_TO_SOCIAL_SPY,
                this.messages.getSpyEnabled(),
                "enabled"
        );
    }

    private boolean hasCooldown(final Player sender) {
        return this.cooldownCollections.hasCooldown(sender, "gigachat.bypass.cooldown.socialspy", this.cooldownCollections.getSpyCooldowns());
    }

    private void sendCooldownMessage(final Player sender) {
        final long timeLeftInMillis = this.cooldownCollections.getSpyCooldowns().get(sender.getUniqueId()) - System.currentTimeMillis();
        final int remainingTime = (int) (this.pmValues.getSpyCooldown() / 1000 + timeLeftInMillis / 1000);
        final String restTime = Utils.getTime(remainingTime);
        final String message = this.messages.getCommandCooldownMessage().replace("{time}", restTime);
        this.sendMessage(sender, message);
    }

    private boolean process(
            final Consumer<UUID> consumer,
            final Player sender, final UUID senderUUID,
            final String query, final String message,
            final String mode) {
        consumer.accept(senderUUID);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (final Connection connection = this.database.getConnection()) {
                this.database.executeUpdateQuery(query, connection, senderUUID.toString());
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        });

        this.sendMessage(sender, message);

        this.plugin.getCommandLogger().log(() ->
                "[SOCIAL-SPY] [" + sender.getName() + "] " + mode
        );

        return true;
    }

    private void sendMessage(final CommandSender sender, final String message) {
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
