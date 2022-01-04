package io.github.gaming32.chatmonitor;

public final class Utils {
    private Utils() {
    }

    public static String lineBreaks(String input, int width) {
        int inLen = input.length();
        StringBuilder result = new StringBuilder(inLen);
        int i = 0;
        int lineStart = 0, lastSpace = 0, currentWidth = 0;
        while (i < inLen) {
            char c = input.charAt(i);
            if (c == ' ') {
                int wordLen = i - lastSpace;
                if (currentWidth + wordLen + 1 > width) {
                    result.append(input, lineStart, lastSpace);
                    result.append('\n');
                    result.append(input, lastSpace + 1, i + 1); // Include space
                    lineStart = i + 1; // Skip space
                    currentWidth = wordLen + 1; // Include space
                } else {
                    currentWidth += wordLen + 1; // Include space
                }
                lastSpace = i;
            } else if (c == '\n') {
                result.append(input, lineStart, i);
                result.append('\n');
                lineStart = i + 1; // Skip space
                currentWidth = 0;
                lastSpace = i;
            }
            i++;
        }
        result.append(input, lineStart, i);
        return result.toString();
    }

    public static void main(String[] args) {
        final String input = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        final String result = lineBreaks(input, 80);
        System.out.println(input.length() + " " + result.length() + " " + (input.length() == result.length()));
        System.out.println(result);
    }
}
