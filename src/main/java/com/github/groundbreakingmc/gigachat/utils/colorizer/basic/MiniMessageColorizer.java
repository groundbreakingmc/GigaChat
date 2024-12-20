package com.github.groundbreakingmc.gigachat.utils.colorizer.basic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class MiniMessageColorizer implements Colorizer {

    @Override
    public String colorize(final String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final Component component = MiniMessage.miniMessage().deserialize(message);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
