package alix.common.utils;

import alix.common.AlixCommonMain;
import alix.common.data.LoginType;
import alix.common.data.security.password.Password;
import alix.common.messages.Messages;
import alix.common.utils.collections.RandomCharIterator;
import alix.common.utils.formatter.AlixFormatter;
import io.netty.channel.Channel;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static alix.common.utils.config.ConfigParams.defaultLoginType;

public final class AlixCommonUtils {

    public static final char[] generationChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-+=".toCharArray();
    private static final RandomCharIterator RANDOM_CHAR_ITERATOR = new RandomCharIterator(generationChars);
    public static final Consumer EMPTY_CONSUMER = e -> {
    };
    //public static final Predicate FALSE_PREDICATE = e -> false;
    public static final Random random = new Random();
    public static final boolean isGraphicEnvironmentHeadless;

    private static final class DoNotThrow {
        private static final String
                tooLongMessage = Messages.getWithPrefix("password-invalid-too-long"),
                tooShortMessage = Messages.getWithPrefix("password-invalid-too-short"),
                invalidCharacterMessage = Messages.getWithPrefix("password-invalid-character"),
                invalidCharacterBlankMessage = Messages.getWithPrefix("password-invalid-character-blank"),
                pinTypeInvalid = Messages.getWithPrefix("gui-pin-type-invalid");
    }

    static {
        boolean isGraphicEnvironmentHeadless0;
        try {
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
            isGraphicEnvironmentHeadless0 = false;
        } catch (Throwable e) {
            isGraphicEnvironmentHeadless0 = true;
        }
        isGraphicEnvironmentHeadless = isGraphicEnvironmentHeadless0;
    }

    //From PacketEvents, FakeChannelUtil.isFakeChannel
    public static boolean isFakeChannel(Channel channel) {
        //Main.logError("CHANNEL: " + channel);
        if (channel == null) return true;
        switch (channel.getClass().getSimpleName()) {
            case "FakeChannel":
            case "SpoofedChannel":
            case "EmbeddedChannel":
                return true;
            default:
                return false;
        }
    }

    public static String getNumbersOnly(String a) {
        char[] chars = a.toCharArray();
        StringBuilder numbersOnly = new StringBuilder();
        for (char d : chars)
            if (d >= 48 && d <= 57) numbersOnly.append(d);

        return numbersOnly.toString();
    }

    public static String getPasswordInvalidityReason(String password, LoginType type) {
        if (type == LoginType.PIN) //if the login type is pin, ensure the password is also a pin
            return isPIN(password) ? null : DoNotThrow.pinTypeInvalid;

        return getInvalidityReason(password, false);
    }

    public static boolean isPIN(String password) {
        if (password.length() != 4) return false;
        for (int i = 0; i < 4; i++) {
            char d = password.charAt(i);
            if (d < 48 || d > 57) return false;
        }
        return true;
    }

    public static String unslashify(String a) {
        return !a.isEmpty() && a.charAt(0) == '/' ? a.substring(1) : a;
    }

    public static String getInvalidityReason(String text, boolean canBeShort) {
        int l = text.length();
        if (l > Password.MAX_PASSWORD_LEN) return DoNotThrow.tooLongMessage;
        if (!canBeShort && l < 5) return DoNotThrow.tooShortMessage;
        for (int i = 0; i < l; i++) {
            char c = text.charAt(i);
            switch (c) {
                case '*':
                case ':':
                case ';':
                case '|':
                    return AlixFormatter.format(DoNotThrow.invalidCharacterMessage, invalidityFeedback(text, i));
                default:
                    if (c <= 32 || Character.isWhitespace(c))// || c > AlixFileManager.HIGHEST_CHAR)//c > 90 && !Character.isLetter(c)
                        return AlixFormatter.format(DoNotThrow.invalidCharacterBlankMessage, invalidityFeedback(text, i));
            }
        }
        return null; //The given text is valid
    }

    private static Object[] invalidityFeedback(String text, int invalidAt) {
        int mentionLen = 3;
        int startIdx = Math.max(invalidAt - mentionLen, 0);
        String start = (startIdx != 0 ? "..." : "") + text.substring(startIdx, invalidAt);

        int len = text.length();

        //+1 since it's exclusive
        int endIdx = Math.min(invalidAt + mentionLen + 1, len);
        String end = text.substring(invalidAt + 1, endIdx) + (endIdx != len ? "..." : "") ;

        return new String[]{start, String.valueOf(text.charAt(invalidAt)), end};
    }

    public static boolean isValidClass(String clazzPath) {
        try {
            Class.forName(clazzPath);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static char randomChar() {
        return RANDOM_CHAR_ITERATOR.next();
    }

    public static String randomStr(int len) {
        return new String(RANDOM_CHAR_ITERATOR.next(len));
    }

    public static LoginType readLoginType(String s, LoginType defaultForNull) {
        if (s.equals("null")) return defaultForNull;
        if (s.equals("0")) return defaultLoginType;
        return LoginType.from(s, false);
    }

    public static boolean startsWith(String a, String... b) {
        for (String c : b) if (a.startsWith(c)) return true;
        return false;
    }

    public static char[] shuffle(char[] chars) {
        List<Character> list = Arrays.asList(toObject(chars));
        Collections.shuffle(list);
        return toPrimitive(list.toArray(new Character[0]));
    }

    public static Character[] toObject(char... a) {
        Character[] b = new Character[a.length];
        for (int c = 0; c < a.length; c++) b[c] = a[c];
        return b;
    }

    public static char[] toPrimitive(Character... a) {
        char[] b = new char[a.length];
        for (int c = 0; c < a.length; c++) b[c] = a[c];
        return b;
    }

    public static Integer[] toObject(int... a) {
        int l = a.length;
        Integer[] b = new Integer[l];
        for (int c = 0; c < l; c++) b[c] = a[c];
        return b;
    }

    public static int[] toPrimitive(Integer... a) {
        int l = a.length;
        int[] b = new int[l];
        for (int c = 0; c < l; c++) b[c] = a[c];
        return b;
    }

/*    public static int getGcd(int i) {
        for (int j = 0; j < ; j++) {

        }
    }*/

    public static int nextPrimeEqualOrGreaterThan(int n) {
        if (isPrime0(n)) return n;
        if ((n & 1) == 0) n++;

        while (!isPrime0(n)) n += 2;

        return n;
    }

    public static <T> T getRandom(T[] array) {
        return array[random.nextInt(array.length)];
    }

    public static <T> T getRandom(T[] array, int len) {
        return array[random.nextInt(len)];
    }

    public static boolean isPrime(int n) {
        if (n == 2) return true;
        if (n <= 1 || (n & 1) == 0) return false;//n & 1 - same as n % 2, but faster, and works for negatives as well
        return isPrime0(n);
    }

    private static boolean isPrime0(int n) {
        for (int i = 3; i < n; i += 2) if (n % i == 0) return false;
        return true;
    }

    public static <T> void fillArray(T[] array, Supplier<T> supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get();
        }
    }

    public static void logException(Throwable e) {
        AlixCommonMain.logError("AlixSystem has thrown an exception - Report this immediately!");
        e.printStackTrace();
    }

    private AlixCommonUtils() {
    }
}