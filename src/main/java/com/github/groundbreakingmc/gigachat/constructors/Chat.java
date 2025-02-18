package com.github.groundbreakingmc.gigachat.constructors;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.collections.DisabledChatCollection;
import com.github.groundbreakingmc.gigachat.collections.IgnoreCollections;
import com.github.groundbreakingmc.gigachat.commands.other.ChatSpyExecutor;
import com.github.groundbreakingmc.gigachat.utils.Utils;
import com.github.groundbreakingmc.gigachat.utils.configvalues.Messages;
import com.github.groundbreakingmc.mylib.collections.expiring.ExpiringMap;
import com.github.groundbreakingmc.mylib.utils.command.CommandRuntimeUtils;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public final class Chat implements TabExecutor {

    private final GigaChat plugin;
    private final String name;
    private final String bypassCooldownPermission;
    private final String format;
    private final String spyFormat;
    private final String spyCommand;
    private final Hover hover;
    private final Hover adminHover;
    private final int distance;
    private final int chatCooldown;
    private final int spyCooldown;
    private final NoOneHead noOneHeardYou;
    private final Map<String, String> groupColors;
    private final ExpiringMap<UUID, Long> chatCooldowns;
    private final ExpiringMap<UUID, Long> spyCooldowns;
    private final Set<Player> spyListeners;
    private final ChatSpyExecutor chatSpyExecutor;

    public Chat(
            final GigaChat plugin,
            final String name,
            final String format,
            final String spyFormat,
            final String spyCommand,
            final Hover hover,
            final Hover adminHover,
            final int distance,
            final int chatCooldown,
            final int spyCooldown,
            final NoOneHead noOneHeardYou,
            final Map<String, String> groupColors
    ) {
        this.plugin = plugin;
        this.name = name;
        this.bypassCooldownPermission = "gigachat.bypass.cooldown.chat." + name;
        this.format = format;
        this.spyFormat = spyFormat;
        this.spyCommand = spyCommand;
        this.hover = hover;
        this.adminHover = adminHover;
        this.distance = distance;
        this.chatCooldown = chatCooldown;
        this.spyCooldown = spyCooldown;
        this.noOneHeardYou = noOneHeardYou;
        this.groupColors = groupColors;
        this.chatCooldowns = new ExpiringMap<>(chatCooldown, TimeUnit.MILLISECONDS);
        this.spyCooldowns = new ExpiringMap<>(chatCooldown, TimeUnit.MILLISECONDS);
        this.spyListeners = new HashSet<>();
        this.chatSpyExecutor = new ChatSpyExecutor(this.plugin);

        if (spyCommand != null && !spyCommand.isEmpty()) {
            CommandRuntimeUtils.register(plugin, spyCommand, List.of(), this);
        }
    }

    public static ChatBuilder builder(final GigaChat plugin) {
        return new ChatBuilder(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return this.chatSpyExecutor.execute(sender, this, this.spyCooldowns, this.spyCooldown);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }

    public boolean hasCooldown(final Player sender, final Messages messages) {
        final UUID senderUUID = sender.getUniqueId();
        if (!sender.hasPermission(this.bypassCooldownPermission) && this.chatCooldowns.containsKey(senderUUID)) {
            final int time = (int) (this.chatCooldown / 1000 + (this.chatCooldowns.get(senderUUID) - System.currentTimeMillis()) / 1000);
            final String restTime = Utils.getTime(time);
            final String cooldownMessage = messages.getChatCooldownMessage().replace("{time}", restTime);
            sender.sendMessage(cooldownMessage);
            return true;
        }

        return false;
    }

    public void addCooldown(final Player sender) {
        this.chatCooldowns.put(sender.getUniqueId(), System.currentTimeMillis());
    }

    public String getColor(final String group) {
        return this.groupColors.getOrDefault(group, "");
    }

    public Set<Player> getRecipients(final Player sender) {
        if (this.distance == -1) {
            return this.getSenderWorld(sender);
        }

        if (this.distance == -2) {
            return this.getNotIgnored(sender);
        }

        final Set<Player> playerList = new HashSet<>();
        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (this.distance == -3) {
            playerList.addAll(onlinePlayers);
            return playerList;
        }

        final UUID senderUUID = sender.getUniqueId();
        final World senderWorld = sender.getWorld();
        final Location senderLocation = sender.getLocation();

        final double maxDist = Math.pow(this.distance, 2.0D);
        for (final Player target : onlinePlayers) {
            final UUID targetUUID = target.getUniqueId();
            if (DisabledChatCollection.contains(targetUUID) || IgnoreCollections.isIgnoredChat(targetUUID, senderUUID)) {
                continue;
            }

            final World targetWorld = target.getWorld();
            if (targetWorld == senderWorld) {
                final Location targetLocation = target.getLocation();
                final double squaredDistance = senderLocation.distanceSquared(targetLocation);
                if (squaredDistance <= maxDist) {
                    playerList.add(target);
                }
            }
        }

        return playerList;
    }

    private Set<Player> getNotIgnored(final Player sender) {
        final Set<Player> playerList = new HashSet<>();
        final UUID senderUUID = sender.getUniqueId();
        for (final Player target : Bukkit.getOnlinePlayers()) {
            final UUID targetUUID = target.getUniqueId();
            if (DisabledChatCollection.contains(targetUUID) || !IgnoreCollections.isIgnoredChat(targetUUID, senderUUID)) {
                playerList.add(target);
            }
        }

        return playerList;
    }

    private Set<Player> getSenderWorld(final Player sender) {
        final Set<Player> playerList = new HashSet<>();
        final World senderWorld = sender.getWorld();
        for (final Player target : Bukkit.getOnlinePlayers()) {
            final World targetWorld = target.getWorld();
            if (senderWorld == targetWorld) {
                playerList.add(target);
            }
        }

        return playerList;
    }

    public boolean isNoOneHeard(final Player sender, final Set<Player> recipients) {
        if (this.noOneHeardYou.isEnabled()) {
            final Set<Player> validRecipients = new HashSet<>(recipients);
            validRecipients.removeIf(recipient ->
                    !this.isRecipientValid(sender, recipient)
            );

            return validRecipients.size() == 1;
        }

        return false;
    }

    private boolean isRecipientValid(final Player sender, final Player recipient) {
        return this.isRecipientVisible(sender, recipient)
                && this.isRecipientNotVanished(recipient)
                && this.isRecipientNotSpectator(recipient);
    }

    private boolean isRecipientVisible(final Player sender, final Player recipient) {
        return !this.noOneHeardYou.hideHidden() || sender.canSee(recipient);
    }

    private boolean isRecipientNotVanished(final Player recipient) {
        return !this.noOneHeardYou.hideVanished() || !PlayerUtils.isVanished(recipient);
    }

    private boolean isRecipientNotSpectator(final Player recipient) {
        return !this.noOneHeardYou.hideSpectators() || recipient.getGameMode() != GameMode.SPECTATOR;
    }

    @SuppressWarnings("unused")
    public static class ChatBuilder {

        private final GigaChat plugin;
        private String name;
        private String format;
        private String spyFormat = null;
        private String spyCommand = null;
        private Hover hover = null;
        private Hover adminHover = null;
        private int distance;
        private int chatCooldown = 3000;
        private int spyCooldown = 3000;
        private NoOneHead noOneHeardYou;
        private Map<String, String> groupColors;

        ChatBuilder(final GigaChat plugin) {
            this.plugin = plugin;
        }

        public ChatBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public String name() {
            return this.name;
        }

        public ChatBuilder format(final String format) {
            this.format = format;
            return this;
        }

        public String format() {
            return this.format;
        }

        public ChatBuilder spyFormat(final String spyFormat) {
            this.spyFormat = spyFormat;
            return this;
        }

        public String spyFormat() {
            return this.spyFormat;
        }

        public ChatBuilder spyCommand(final String spyCommand) {
            this.spyCommand = spyCommand;
            return this;
        }

        public String spyCommand() {
            return this.spyCommand;
        }

        public ChatBuilder hover(final Hover hover) {
            this.hover = hover;
            return this;
        }

        public Hover hover() {
            return this.hover;
        }

        public ChatBuilder adminHover(final Hover adminHover) {
            this.adminHover = adminHover;
            return this;
        }

        public Hover adminHover() {
            return this.adminHover;
        }

        public ChatBuilder distance(final int distance) {
            this.distance = distance;
            return this;
        }

        public int distance() {
            return this.distance;
        }

        public ChatBuilder chatCooldown(final int chatCooldown) {
            this.chatCooldown = chatCooldown;
            return this;
        }

        public int chatCooldown() {
            return this.chatCooldown;
        }

        public ChatBuilder spyCooldown(final int spyCooldown) {
            this.spyCooldown = spyCooldown;
            return this;
        }

        public int spyCooldown() {
            return this.spyCooldown;
        }

        public ChatBuilder noOneHeard(final NoOneHead noOneHeardYou) {
            this.noOneHeardYou = noOneHeardYou;
            return this;
        }

        public NoOneHead noOneHeard() {
            return this.noOneHeardYou;
        }

        public ChatBuilder groupColors(final Map<String, String> groupColors) {
            this.groupColors = ImmutableMap.copyOf(groupColors);
            return this;
        }

        public Map<String, String> groupColors() {
            return this.groupColors;
        }

        public Chat build() {
            return new Chat(
                    this.plugin,
                    this.name,
                    this.format,
                    this.spyFormat,
                    this.spyCommand,
                    this.hover,
                    this.adminHover,
                    this.distance,
                    this.chatCooldown,
                    this.spyCooldown,
                    this.noOneHeardYou,
                    this.groupColors
            );
        }
    }
}
