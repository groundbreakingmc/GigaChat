package com.github.groundbreakingmc.gigachat.utils.colorizer;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ColorizerUtils {

    public static String getClear(final String message) {
        final StringBuilder result = new StringBuilder(message.length());

        for (int i = 0; i < message.length() - 1; i++) {
            final char current = message.charAt(i);
            if (current == '§') {
                final char next = message.charAt(i + 1);
                if (isColorCharacter(next) || isStyleCharacter(next)) {
                    i++;
                    continue;
                }
            }

            result.append(current);
        }

        return result.toString();
    }

    public static boolean isColorCharacter(char c) {
        return c >= '0' && c <= '9'
                || c >= 'a' && c <= 'f'
                || c >= 'A' && c <= 'F';
    }

    public static boolean isStyleCharacter(char c) {
        return c == 'r'
                || c >= 'k' && c <= 'o'
                || c == 'x'
                || c == 'R'
                || c >= 'K' && c <= 'O'
                || c == 'X';
    }
}
