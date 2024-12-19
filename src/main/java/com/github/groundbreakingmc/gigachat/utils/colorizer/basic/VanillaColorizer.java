package com.github.groundbreakingmc.gigachat.utils.colorizer.basic;

public final class VanillaColorizer implements Colorizer {

    @Override
    public String colorize(final String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        return ColorCodesTranslator.translateAlternateColorCodes('&', message);
    }
}
