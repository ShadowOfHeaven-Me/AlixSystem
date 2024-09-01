package alix.common.utils;

import alix.common.AlixCommonMain;
import alix.common.data.LoginType;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static alix.common.utils.config.ConfigParams.defaultLoginType;

public final class AlixCommonUtils {

    public static final Consumer EMPTY_CONSUMER = e -> {};
    //public static final Predicate FALSE_PREDICATE = e -> false;
    public static final Random random = new Random();
    public static final boolean isGraphicEnvironmentHeadless;

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
        for(int c = 0; c < l; c++) b[c] = a[c];
        return b;
    }

    public static int[] toPrimitive(Integer... a) {
        int l = a.length;
        int[] b = new int[l];
        for(int c = 0; c < l; c++) b[c] = a[c];
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

    public static boolean isPrime(int n) {
        if (n == 2) return true;
        if (n <= 1 || (n & 1) == 0) return false;//n & 1 - same as n % 2, but faster, and works for negatives as well
        return isPrime0(n);
    }

    private static boolean isPrime0(int n) {
        for (int i = 3; i < n; i++) if (n % i == 0) return false;
        return true;
    }

    public static <T> void fillArray(T[] array, Supplier<T> supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get();
        }
    }

    public static boolean isValidClass(String clazzPath) {
        try {
            Class.forName(clazzPath);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void logException(Throwable e) {
        AlixCommonMain.logError("AlixSystem has thrown an exception - Report this immediately!");
        e.printStackTrace();
    }

    private AlixCommonUtils() {
    }
}