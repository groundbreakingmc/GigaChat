package groundbreaking.gigachat.utils;

import groundbreaking.gigachat.constructors.Hover;
import groundbreaking.gigachat.utils.colorizer.basic.Colorizer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

public final class HoverUtils {

    private HoverUtils() {

    }

    public static BaseComponent[] get(final Player sender, final Hover hover, final String hoverText, final String message, final Colorizer colorizer) {
        final String hoverString = colorizer.colorize(
                Utils.replacePlaceholders(sender, hoverText)
        );
        final String hoverValue = hover.clickValue().replace("{player}", sender.getName());

        final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverString)));
        final ClickEvent clickEvent = new ClickEvent(hover.clickAction(), hoverValue);
        final BaseComponent[] components = TextComponent.fromLegacyText(message);

        for (int i = 0; i < components.length; i++) {
            components[i].setHoverEvent(hoverEvent);
            components[i].setClickEvent(clickEvent);
        }

        return components;
    }
}
