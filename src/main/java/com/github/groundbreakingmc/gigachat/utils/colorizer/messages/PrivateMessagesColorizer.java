package com.github.groundbreakingmc.gigachat.utils.colorizer.messages;

import com.github.groundbreakingmc.gigachat.GigaChat;
import com.github.groundbreakingmc.gigachat.utils.colorizer.ColorizerUtils;
import org.bukkit.entity.Player;

public final class PrivateMessagesColorizer extends PermissionColorizer {

    public PrivateMessagesColorizer(final GigaChat plugin) {
        super(plugin);
    }

    @Override
    public String colorize(final Player player, final String message) {
        if (player.hasPermission("gigachat.color.private.hex")) {
            return super.messagesColorizer.colorize(message);
        }

        final char[] letters = message.toCharArray();
        for (int i = 0; i < letters.length; i++) {
            if (letters[i] == COLOR_CHAR) {
                final char code = letters[i + 1];
                if (ColorizerUtils.isColorCharacter(code) && player.hasPermission("gigachat.color.private." + code)
                        || ColorizerUtils.isStyleCharacter(code) && player.hasPermission("gigachat.style.private." + code)) {
                    letters[i++] = MINECRAFT_COLOR_CHAR;
                    letters[i] = Character.toLowerCase(letters[i]);
                }
            }
        }

        return new String(letters);
    }

    @Override
    public String colorize(final String message) {
        return super.messagesColorizer.colorize(message);
    }
}
