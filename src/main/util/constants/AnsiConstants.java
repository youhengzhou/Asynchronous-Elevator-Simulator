package main.util.constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class for ANSI constants.
 * Pulled from the internet.
 */
public final class AnsiConstants {
    public static final String	SANE				= "\u001B[0m";

    public static final String	HIGH_INTENSITY		= "\u001B[1m";
    public static final String	LOW_INTENSITY		= "\u001B[2m";

    public static final String	ITALIC				= "\u001B[3m";
    public static final String	UNDERLINE			= "\u001B[4m";
    public static final String	BLINK				= "\u001B[5m";
    public static final String	RAPID_BLINK			= "\u001B[6m";
    public static final String	REVERSE_VIDEO		= "\u001B[7m";
    public static final String	INVISIBLE_TEXT		= "\u001B[8m";

    public static final String	BLACK				= "\u001B[30m";
    public static final String	RED					= "\u001B[31m";
    public static final String	GREEN				= "\u001B[32m";
    public static final String	YELLOW				= "\u001B[33m";
    public static final String	BLUE				= "\u001B[34m";
    public static final String	MAGENTA				= "\u001B[35m";
    public static final String	CYAN				= "\u001B[36m";
    public static final String	WHITE				= "\u001B[37m";

    public static final String	BACKGROUND_BLACK	= "\u001B[40m";
    public static final String	BACKGROUND_RED		= "\u001B[41m";
    public static final String	BACKGROUND_GREEN	= "\u001B[42m";
    public static final String	BACKGROUND_YELLOW	= "\u001B[43m";
    public static final String	BACKGROUND_BLUE		= "\u001B[44m";
    public static final String	BACKGROUND_MAGENTA	= "\u001B[45m";
    public static final String	BACKGROUND_CYAN		= "\u001B[46m";
    public static final String	BACKGROUND_WHITE	= "\u001B[47m";

    public static final AnsiConstants HighIntensity = new AnsiConstants(HIGH_INTENSITY);
    public static final AnsiConstants Bold = HighIntensity;
    public static final AnsiConstants LowIntensity = new AnsiConstants(LOW_INTENSITY);
    public static final AnsiConstants Normal = LowIntensity;

    public static final AnsiConstants Italic = new AnsiConstants(ITALIC);
    public static final AnsiConstants Underline = new AnsiConstants(UNDERLINE);
    public static final AnsiConstants Blink = new AnsiConstants(BLINK);
    public static final AnsiConstants RapidBlink = new AnsiConstants(RAPID_BLINK);

    public static final AnsiConstants Black = new AnsiConstants(BLACK);
    public static final AnsiConstants Red = new AnsiConstants(RED);
    public static final AnsiConstants Green = new AnsiConstants(GREEN);
    public static final AnsiConstants Yellow = new AnsiConstants(YELLOW);
    public static final AnsiConstants Blue = new AnsiConstants(BLUE);
    public static final AnsiConstants Magenta = new AnsiConstants(MAGENTA);
    public static final AnsiConstants Cyan = new AnsiConstants(CYAN);
    public static final AnsiConstants White = new AnsiConstants(WHITE);

    public static final AnsiConstants BgBlack = new AnsiConstants(BACKGROUND_BLACK);
    public static final AnsiConstants BgRed = new AnsiConstants(BACKGROUND_RED);
    public static final AnsiConstants BgGreen = new AnsiConstants(BACKGROUND_GREEN);
    public static final AnsiConstants BgYellow = new AnsiConstants(BACKGROUND_YELLOW);
    public static final AnsiConstants BgBlue = new AnsiConstants(BACKGROUND_BLUE);
    public static final AnsiConstants BgMagenta = new AnsiConstants(BACKGROUND_MAGENTA);
    public static final AnsiConstants BgCyan = new AnsiConstants(BACKGROUND_CYAN);
    public static final AnsiConstants BgWhite = new AnsiConstants(BACKGROUND_WHITE);

    final private String[] codes;
    final private String codes_str;
    public AnsiConstants(String... codes) {
        this.codes = codes;
        String _codes_str = "";
        for (String code : codes) {
            _codes_str += code;
        }
        codes_str = _codes_str;
    }

    public AnsiConstants and(AnsiConstants other) {
        List<String> both = new ArrayList<String>();
        Collections.addAll(both, codes);
        Collections.addAll(both, other.codes);
        return new AnsiConstants(both.toArray(new String[] {}));
    }

    public String colorize(String original) {
        return codes_str + original + SANE;
    }

    public String format(String template, Object... args) {
        return colorize(String.format(template, args));
    }
}
