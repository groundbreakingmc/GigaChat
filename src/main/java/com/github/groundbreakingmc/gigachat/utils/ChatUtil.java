package com.github.groundbreakingmc.gigachat.utils;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.constructors.Chat;
import com.github.groundbreakingmc.gigachat.constructors.DefaultChat;
import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.gigachat.constructors.NoOneHead;
import com.github.groundbreakingmc.gigachat.exceptions.InvalidFormatException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class ChatUtil {

    private ChatUtil() {

    }

    public static Chat createChat(final GigaChat plugin, final ConfigurationSection chatSection, final String chatKey, final DefaultChat defaultChat) {
        final Chat.ChatBuilder chatBuilder = Chat.builder(plugin);

        final String format = chatSection.getString("format");
        if (format == null) {
            throw new InvalidFormatException("Chat format cannot be null! Path: \"chats." + chatKey + ".format\"");
        }

        chatBuilder.name(chatSection.getName());
        chatBuilder.format(format);

        chatBuilder.hover(Hover.get(chatSection.getConfigurationSection("hover"), defaultChat.hover()));
        chatBuilder.adminHover(Hover.get(chatSection.getConfigurationSection("admin-hover"), defaultChat.adminHover()));

        chatBuilder.distance(chatSection.getInt("distance"));
        chatBuilder.chatCooldown(chatSection.getInt("chat-cooldown", defaultChat.chatCooldown()));

        chatBuilder.spyFormat(chatSection.getString("spy-format"));
        chatBuilder.spyCommand(chatSection.getString("spy-command"));
        chatBuilder.spyCooldown(chatSection.getInt("spy-cooldown", defaultChat.spyCooldown()));

        chatBuilder.groupColors(getGroupColors(chatSection, defaultChat.groupColors()));

        chatBuilder.noOneHeard(getNoOneHeard(chatSection, defaultChat.noOneHead()));

        return chatBuilder.build();
    }

    public static DefaultChat createDefaultChat(final GigaChat plugin, final ConfigurationSection chatsSection) {
        final ConfigurationSection defaultChatSection = chatsSection.getConfigurationSection("default");
        if (defaultChatSection != null) {
            final int spyCooldown = defaultChatSection.getInt("spy-cooldown");
            final int chatCooldown = defaultChatSection.getInt("chat-cooldown");

            final NoOneHead noOneHeard = getNoOneHeard(defaultChatSection, null);

            final Hover hover = Hover.get(defaultChatSection.getConfigurationSection("hover"), null);
            final Hover adminHover = Hover.get(defaultChatSection.getConfigurationSection("admin-hover"), null);

            final Map<String, String> groupColors = getGroupColors(defaultChatSection, null);

            return new DefaultChat(spyCooldown, chatCooldown, groupColors, noOneHeard, hover, adminHover);
        } else {
            plugin.getCustomLogger().warn("Failed to load section \"chats.default\" from file \"chats.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getCustomLogger().warn("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
            return null;
        }
    }

    private static NoOneHead getNoOneHeard(final ConfigurationSection chatSection, final NoOneHead defaultNoOneHeard) {
        final ConfigurationSection noOneHeardSection = chatSection.getConfigurationSection("no-one-heard");
        if (noOneHeardSection != null) {
            final boolean isEnabled = noOneHeardSection.getBoolean("enable", defaultNoOneHeard != null && defaultNoOneHeard.isEnabled());
            final boolean hideHidden = noOneHeardSection.getBoolean("hide-hidden", defaultNoOneHeard != null && defaultNoOneHeard.hideHidden());
            final boolean hideVanished = noOneHeardSection.getBoolean("hide-vanished", defaultNoOneHeard != null && defaultNoOneHeard.hideVanished());
            final boolean hideSpectators = noOneHeardSection.getBoolean("hide-spectators", defaultNoOneHeard != null && defaultNoOneHeard.hideSpectators());

            return new NoOneHead(isEnabled, hideHidden, hideVanished, hideSpectators);
        }

        return defaultNoOneHeard;
    }

    private static Map<String, String> getGroupColors(final ConfigurationSection chatSection, final Map<String, String> defaultGroupColors) {
        final ConfigurationSection groupsColorsSection = chatSection.getConfigurationSection("group-colors");
        if (groupsColorsSection != null) {
            final Map<String, String> groupsColors = new Object2ObjectOpenHashMap<>();
            groupsColorsSection.getValues(false).forEach((key, value) ->
                    groupsColors.put(key, (String) value)
            );

            return groupsColors;
        }

        return defaultGroupColors;
    }
}
