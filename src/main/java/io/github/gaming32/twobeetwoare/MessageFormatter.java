package io.github.gaming32.twobeetwoare;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

// Thanks to https://github.com/Minecrell/TerminalConsoleAppender/blob/master/src/main/java/net/minecrell/terminalconsole/MinecraftFormattingConverter.java
// for the converter code
public final class MessageFormatter {
    private static final String ANSI_RESET = "\u001B[m";
    private static final char COLOR_CHAR = '§';
    private static final String LOOKUP = "0123456789abcdefklmnor";
    private static final String[] ANSI_CODES = new String[] {
            "\u001B[0;30m", // Black §0
            "\u001B[0;34m", // Dark Blue §1
            "\u001B[0;32m", // Dark Green §2
            "\u001B[0;36m", // Dark Aqua §3
            "\u001B[0;31m", // Dark Red §4
            "\u001B[0;35m", // Dark Purple §5
            "\u001B[0;33m", // Gold §6
            "\u001B[0;37m", // Gray §7
            "\u001B[0;30;1m",  // Dark Gray §8
            "\u001B[0;34;1m",  // Blue §9
            "\u001B[0;32;1m",  // Green §a
            "\u001B[0;36;1m",  // Aqua §b
            "\u001B[0;31;1m",  // Red §c
            "\u001B[0;35;1m",  // Light Purple §d
            "\u001B[0;33;1m",  // Yellow §e
            "\u001B[0;37;1m",  // White §f
            "\u001B[5m",       // Obfuscated §k
            "\u001B[21m",      // Bold §l
            "\u001B[9m",       // Strikethrough §m
            "\u001B[4m",       // Underline §n
            "\u001B[3m",       // Italic §o
            ANSI_RESET,        // Reset §r
    };

    public static String formatMessage(Component root) {
        return PlainTextComponentSerializer.plainText().serialize(root);
    }

    public static final String convertToAnsi(String text) {
        int next = text.indexOf(COLOR_CHAR);
        int last = text.length() - 1;
        if (next == -1 || next == last) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);
        result.setLength(next);

        int pos = next;
        do {
            int format = LOOKUP.indexOf(Character.toLowerCase(text.charAt(next + 1)));
            if (format != -1) {
                if (pos != next) {
                    result.append(text, pos, next);
                }
                result.append(ANSI_CODES[format]);
                pos = next += 2;
            } else {
                next++;
            }

            next = text.indexOf(COLOR_CHAR, next);
        } while (next != -1 && next < last);

        result.append(text, pos, text.length());
        result.append(ANSI_RESET);
        return result.toString();
    }

    public static final String stripFormatting(String text) {
        int next = text.indexOf(COLOR_CHAR);
        int last = text.length() - 1;
        if (next == -1 || next == last) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);
        result.setLength(next);

        int pos = next;
        do {
            int format = LOOKUP.indexOf(Character.toLowerCase(text.charAt(next + 1)));
            if (format != -1) {
                if (pos != next) {
                    result.append(text, pos, next);
                }
                pos = next += 2;
            } else {
                next++;
            }

            next = text.indexOf(COLOR_CHAR, next);
        } while (next != -1 && next < last);

        result.append(text, pos, text.length());
        return result.toString();
    }
}
