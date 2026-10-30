import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

enum LogLevel {
    TRACE, INFO, WARN, ERROR
}

class Logger {
    static LogLevel level = LogLevel.INFO;
    static String format = "[%t] [%l] %n: %m";
    private String name;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Random random = new Random();

    public Logger(String name) {
        this.name = name;
    }

    public static void setLevel(LogLevel newLevel) {
        level = newLevel;
    }

    public static void setFormat(String newFormat) {
        format = newFormat;
    }

    public void log(LogLevel msgLevel, String message) {
        if (msgLevel.ordinal() < level.ordinal()) {
            return;
        }
        System.out.println(buildLine(msgLevel, message));
    }

    public void trace(String message) { log(LogLevel.TRACE, message); }
    public void info(String message)  { log(LogLevel.INFO, message); }
    public void warn(String message)  { log(LogLevel.WARN, message); }
    public void error(String message) { log(LogLevel.ERROR, message); }

    private String buildLine(LogLevel msgLevel, String message) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);

            if (c == '%' && i + 1 < format.length()) {
                String value = placeholderValue(format.charAt(i + 1), msgLevel, message);
                if (value != null) {
                    result.append(value);
                    i++;
                    continue;
                }
            }
            result.append(c);
        }
        return result.toString();
    }

    private String placeholderValue(char letter, LogLevel msgLevel, String message) {
        switch (letter) {
            case 't': return LocalTime.now().format(TIME_FORMAT);
            case 'm': return message;
            case 'n': return name;
            case 'l': return msgLevel.name();
            case 'r': return randomMessage(msgLevel);
            default:  return null;
        }
    }

    private static String randomMessage(LogLevel msgLevel) {
        String[] options;
        switch (msgLevel) {
            case TRACE:
                options = new String[] {"Just a small step", "Going deeper", "Still working on it"};
                break;
            case INFO:
                options = new String[] {"Operation completed", "Everything looks good", "Here is your result"};
                break;
            case WARN:
                options = new String[] {"Hmm, this looks strange", "Be careful here", "Something is off"};
                break;
            default:
                options = new String[] {"Oops, something broke", "This is not right", "Operation failed"};
        }
        return options[random.nextInt(options.length)];
    }
}

class ParenthesesValidator {
    private Logger logger = new Logger("Parentheses");

    public boolean isValid(String input) {
        int open = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '(') {
                open++;
                logger.trace("Found '(' at position " + i);
            } else if (c == ')') {
                if (open == 0) {
                    logger.warn("Found ')' at position " + i + " but there is no '(' to match it");
                    return finish(input, false, "the ')' at position " + i + " has no matching '('");
                }
                open--;
                logger.info("Found a matching pair, closed at position " + i);
            }
        }

        if (open > 0) {
            return finish(input, false, open + " '(' never got closed");
        }
        return finish(input, true, null);
    }

    private boolean finish(String input, boolean valid, String reason) {
        if (!valid) {
            logger.error("Input \"" + input + "\" is invalid: " + reason);
        }
        logger.info("Result for \"" + input + "\": " + valid);
        return valid;
    }
}


public class Main {
    public static void main(String[] args) {
        ParenthesesValidator validator = new ParenthesesValidator();

        String[] inputs   = {"()", ")((()))", "(", "(())((()())())"};
        boolean[] expected = {true, false, false, true};

        System.out.println("Level TRACE:");
        Logger.setLevel(LogLevel.TRACE);
        validator.isValid("(())");

        System.out.println("\nLevel INFO: ");
        Logger.setLevel(LogLevel.INFO);
        for (int i = 0; i < inputs.length; i++) {
            boolean got = validator.isValid(inputs[i]);
        }

        System.out.println("Level ERROR:");
        Logger.setLevel(LogLevel.ERROR);
        validator.isValid("())");
        validator.isValid("()");

        System.out.println("\nDifferent formats: ");
        Logger.setLevel(LogLevel.TRACE);
        Logger demo = new Logger("Demo");
        String[] formats = {"[%l] %m", "%n: %m", "[%t] %m", "%l - %n - %m", "[%l] %r -> %m"};
        for (String f : formats) {
            Logger.setFormat(f);
            demo.info("Valid input");
        }
        Logger.setFormat("[%l] %r");
        demo.error("x");
    }
}
