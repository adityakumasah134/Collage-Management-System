package utility;

import java.util.Scanner;

/** Central place for reading and validating console input. */
public class InputHelper {

    /** Thrown when the input stream ends (e.g. Ctrl+D), so the program can save and exit cleanly. */
    public static class InputClosedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InputClosedException() {
            super("Input stream closed");
        }
    }

    private static final Scanner scanner = new Scanner(System.in);

    private static final String NAME_PATTERN = "[A-Za-z][A-Za-z .'-]*";
    private static final String EMAIL_PATTERN = "[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+";
    private static final String PHONE_PATTERN = "\\d{10}";

    private InputHelper() {
    }

    /** Reads one line. The '|' character is removed because it is used as the file separator. */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().replace('|', ' ').replaceAll("\\s+", " ").trim();
    }

    public static String readNonEmpty(String prompt) {
        return readMatching(prompt, ".+", "Invalid input.", false);
    }

    public static String readName(String prompt) {
        return readMatching(prompt, NAME_PATTERN, "Name must contain letters only.", false);
    }

    public static String readEmail(String prompt) {
        return readMatching(prompt, EMAIL_PATTERN, "Enter a valid email address (example: name@mail.com).", false);
    }

    public static String readPhone(String prompt) {
        return readMatching(prompt, PHONE_PATTERN, "Phone number must be exactly 10 digits.", false);
    }

    /** Optional versions return an empty string when the user just presses Enter. */
    public static String readOptional(String prompt) {
        return readLine(prompt);
    }

    public static String readOptionalName(String prompt) {
        return readMatching(prompt, NAME_PATTERN, "Name must contain letters only.", true);
    }

    public static String readOptionalEmail(String prompt) {
        return readMatching(prompt, EMAIL_PATTERN, "Enter a valid email address (example: name@mail.com).", true);
    }

    public static String readOptionalPhone(String prompt) {
        return readMatching(prompt, PHONE_PATTERN, "Phone number must be exactly 10 digits.", true);
    }

    /** Reads an ID and converts it to upper case. */
    public static String readId(String prompt) {
        return readNonEmpty(prompt).toUpperCase().replace(" ", "");
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message
            }
            System.out.println("Error: Please enter a whole number between " + min + " and " + max + ".");
        }
    }

    /** Returns null when the user presses Enter without typing anything. */
    public static Integer readOptionalInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) {
                return null;
            }
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message
            }
            System.out.println("Error: Please enter a whole number between " + min + " and " + max + ".");
        }
    }

    public static double readAmount(String prompt, double min, double max) {
        while (true) {
            String value = readLine(prompt);
            try {
                double amount = Double.parseDouble(value);
                if (!Double.isNaN(amount) && !Double.isInfinite(amount) && amount >= min && amount <= max) {
                    return Math.round(amount * 100.0) / 100.0;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message
            }
            System.out.println("Error: Please enter an amount between " + String.format("%.2f", min)
                    + " and " + String.format("%.2f", max) + ".");
        }
    }

    /** Reads a menu choice. Returns -1 if the input is not a number. */
    public static int readChoice(String prompt) {
        String value = readLine(prompt);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean confirm(String prompt) {
        while (true) {
            String value = readLine(prompt + " (Y/N): ");
            if (value.equalsIgnoreCase("Y") || value.equalsIgnoreCase("YES")) {
                return true;
            }
            if (value.equalsIgnoreCase("N") || value.equalsIgnoreCase("NO")) {
                return false;
            }
            System.out.println("Error: Please type Y or N.");
        }
    }

    /** Extracts the number from an ID such as "STU1005" (prefix "STU"). Returns 0 if not possible. */
    public static int extractNumber(String id, String prefix) {
        if (id == null || !id.startsWith(prefix)) {
            return 0;
        }
        try {
            return Integer.parseInt(id.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String readMatching(String prompt, String regex, String error, boolean allowBlank) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) {
                if (allowBlank) {
                    return "";
                }
                System.out.println("Error: This field cannot be empty.");
                continue;
            }
            if (value.matches(regex)) {
                return value;
            }
            System.out.println("Error: " + error);
        }
    }
}
