package com.github.groundbreakingmc.gigachat.utils;

import com.github.groundbreakingmc.gigachat.constructors.Hover;
import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

@UtilityClass
public class HoverUtils {

    public BaseComponent[] get(final Player sender, final String prefix, final String suffix,
                               final Hover hover, final String hoverText,
                               final String message,
                               final Colorizer colorizer) {
        final String replaced = colorizer.colorize(
                Utils.replacePlaceholders(sender, hoverText
                        .replace("{player}", sender.getName())
                        .replace("{prefix}", prefix)
                        .replace("{suffix}", suffix)
                )
        );

        final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(replaced)));

        final String hoverValue = hover.clickValue().replace("{player}", sender.getName());
        final ClickEvent clickEvent = new ClickEvent(hover.clickAction(), hoverValue);

        final BaseComponent[] components = TextComponent.fromLegacyText(message);

        for (int i = 0; i < components.length; i++) {
            components[i].setHoverEvent(hoverEvent);
            components[i].setClickEvent(clickEvent);
        }

        return components;
    }
}
