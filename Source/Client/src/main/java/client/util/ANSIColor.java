package client.util;

public enum ANSIColor {
    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7),
    BRIGHT_BLACK(60),
    BRIGHT_RED(61),
    BRIGHT_GREEN(62),
    BRIGHT_YELLOW(63),
    BRIGHT_BLUE(64),
    BRIGHT_MAGENTA(65),
    BRIGHT_CYAN(66),
    BRIGHT_WHITE(67);

    private final int index;

    ANSIColor(int index) {
        this.index = index;
    }

    private static final String RESET = "\u001B[0m";

    private static final int FOREGROUND_START = 30;
    private static final int BACKGROUND_START = 40;

    private static int getForegroundCode(ANSIColor color) {
        return FOREGROUND_START + color.index;
    }

    private static int getBackgroundCode(ANSIColor color) {
        return BACKGROUND_START + color.index;
    }

    public static String format(String str, ANSIColor fgColor) {
        return String.format("\u001B[%sm%s%s", getForegroundCode(fgColor), str, RESET);
    }

    public static String format(String str, ANSIColor fgColor, ANSIColor bgColor) {
        return format(String.format("\u001B[%sm%s", getBackgroundCode(bgColor), str), fgColor);
    }
}
