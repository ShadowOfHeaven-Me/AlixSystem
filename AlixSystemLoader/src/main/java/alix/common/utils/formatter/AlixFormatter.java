package alix.common.utils.formatter;

import alix.common.utils.config.ConfigProvider;
import alix.common.utils.other.annotation.AlixIntrinsified;

import java.util.Arrays;

public final class AlixFormatter {

    //public static final String pluginPrefix = translateColors(ConfigProvider.config.getString("prefix"));
    private static final String messagePrefix;
    private static final boolean appendPrefix;

    static {
        messagePrefix = translateColors(ConfigProvider.config.getString("prefix"));
        appendPrefix = ConfigProvider.config.getBoolean("append-prefix");
        //AlixCommonMain.logError("messagePrefix='" + messagePrefix + "' appendPrefix=" + appendPrefix);
    }

    public static String appendPrefix(String m) {
        return appendPrefix ? messagePrefix + m : m;
    }

    public static String colorize(String m) {
        return appendPrefix(translateColors(m));
    }

    /**
     * Returns a formatted String with the syntax {<digit>}, where
     * the digit is the array index of the "args" argument used for formatting.
     * Different formatting methods are invoked depending on the args' length.
     * It should be noted that if only one formatting argument is provided
     * (args.length == 1), the method will end after reformatting only one regex.
     * If you wish to reformat the same regex of "{0}" that has multiple
     * occurrences, then use the formatMultiple method.
     *
     * @param s    The String that should be formatted
     * @param args The arguments used for formatting, as explained above
     * @return A String with regexes of the syntax {<digit>} replaced with
     * the formatting "args" of the same array index as the <digit>
     * @author ShadowOfHeaven
     */


    @AlixIntrinsified(method = "String#replaceAll")
    public static String format(String s, Object... args) {
        switch (args.length) {
            case 0:
                return s;//no formatting is needed, as there is none to format with
            case 1:
                return formatSingle(s, args[0].toString());//format one regex with one replacement
            default:
                return formatMultiple(s, args);//format multiple regexes with their according replacements
        }
    }

    /**
     * Returns a formatted String with the syntax "{0}", where the regex
     * is replaced with the "replacement" argument. Assumes there's
     * only one formatting regex.
     *
     * @param s           The String that should be formatted
     * @param replacement The replacement used for the {0} regex
     * @return A String where the "{0}" regex was replaced
     * with the "replacement" argument
     * @author ShadowOfHeaven
     */

    public static String formatSingle(String s, String replacement) {//For {<digit>} = 1, specifically for {0} = 1
        char[] a = s.toCharArray();
        int l = a.length;
        int lM2 = l - 2;
        StringBuilder sb = new StringBuilder(l - 3 + replacement.length());//minus 3 because '{<digit>}' is 3 characters long
        for (int i = 0; i < lM2; i++) {
            char c = a[i];
            if (c == '{' && a[i + 1] == '0' && a[i + 2] == '}') {//the regex is "{0}"
                int ip3 = i + 3;//the regex is 3 letters long
                return ip3 >= l ? sb.append(replacement).toString() : sb.append(replacement).append(Arrays.copyOfRange(a, ip3, l)).toString();
            }
            sb.append(c);
        }
        return s;
    }

    /**
     * Returns a formatted String with regexes {<digit>}, where
     * the digit is the array index of the "args" argument used for formatting
     *
     * @param s    The String that should be formatted
     * @param args The arguments used for formatting, as explained above
     * @return A String with regexes of the syntax {<digit>} replaced with
     * the formatting "args" of the same array index as the <digit>
     * @author ShadowOfHeaven
     */

    public static String formatMultiple(String s, Object... args) {//For {<digit>} > 1
        //n << 2 is n * 4                                                     \/
        StringBuilder sb = new StringBuilder(s.length() + (args.length << 2)); //assume each arg is about 7 characters long (3 in {<digit>}, 4 in argLength * 4)
        char[] a = s.toCharArray();
        int lM2 = a.length - 2;
        for (int i = 0; i < a.length; i++) {
            char c = a[i];
            if (i == lM2)
                return sb.append(a[lM2]).append(a[a.length - 1]).toString();//the remaining text was not skipped and is 2 chars long, so we can skip the character test and simply return the current text + the 2 remaining characters, since the regex is 3 characters long
            if (c == '{' && a[i + 2] == '}') {
                int index = a[i + 1] - 48;//48 is '0' in ascii
                if (index < args.length && index >= 0) {//the given index is valid
                    sb.append(args[index]);
                    i += 2;//skipping '<digit>}' in the text, as the first '{' is already skipped by the default for(i) iterator
                    continue;//continue to the next loop and stop this
                }//continue, the index was invalid
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @AlixIntrinsified(method = "ChatColor.translateAlternateColorCodes")
    public static String translateColors(String text) {//Faster than ChatColor.translateAlternateColorCodes
        if (text == null) return null;
        char[] c = text.toCharArray();
        int lM1 = c.length - 1;
        for (int i = 0; i < lM1; i++)
            if (c[i] == '&') {
                char d = c[++i];                                   //k, l, m, n, o
                if (d >= 'a' && d <= 'f' || d >= '0' && d <= '9' || d >= 'k' && d <= 'o' || d == 'r') c[i - 1] = '§';
            }
        return new String(c);
    }

    private AlixFormatter() {
    }

/*    public static char[] translateColors(char[] c) {
        for (int i = 0; i < c.length; i++)
            if (c[i] == '&') c[i] = '§';
        return c;
    }*/

/*    private static String formatMultiple(String s, Object[] args) {//For %s > 1
        char[] a = s.toCharArray();
        int l = a.length;
        int lM1 = l - 1;
        int argsLength = args.length;
        int totalLength = l - argsLength * 2;
        int replacementsIndex = 0;
        String[] replacements = new String[argsLength];
        for (byte i = 0; i < argsLength; i++) {
            String t = args[i].toString();
            replacements[i] = t;
            totalLength += t.length();
        }
        StringBuilder sb = new StringBuilder(totalLength);
        for (int i = 0; i < lM1; i++) {
            char c = a[i];
            if (c == '%' && a[i + 1] == 's') {
                sb.append(replacements[replacementsIndex++]);
                if (replacementsIndex == argsLength) {
                    int ip2 = i + 2;
                    if (ip2 == l) return sb.toString();
                    return sb.append(Arrays.copyOfRange(a, ip2, l)).toString();
                }
            } else sb.append(c);
        }
        return sb.toString();
    }*/

/*    private static String formatComplex(String s, Object[] args) {
        char[] a = s.toCharArray();
        int l = a.length;
        int lM1 = l - 1;
        //char[] newString = new char[newLength];
        StringBuilder sb = new StringBuilder(l + args.length);
        for (int i = 0; i < lM1; i++) {
            char c = a[i];
            if (c == '%') {
                switch (a[i + 1]) {
                    case 'p':
                        break;
                    case 's':
                        break;
                    case 'r':
                        break;
                    case 'n':
                        break;
                }
            }
            sb.append(c);
        }
        return s;
    }*/

/*    public static String formatSingle(String s, String replacement) {//For %s = 1
//Faster than String.format by about 40 000% (~16ms vs ~0.04ms)
        char[] a = s.toCharArray();
        int l = a.length;
        int lM1 = l - 1;
        //char[] newString = new char[newLength];
        StringBuilder sb = new StringBuilder(lM1 - 1 + replacement.length());
        for (int i = 0; i < lM1; i++) {
            char c = a[i];
            if (c == '%' && a[i + 1] == 's') {
                int ip2 = i + 2;
                if (ip2 == l) return sb.append(replacement).toString();
                return sb.append(replacement).append(Arrays.copyOfRange(a, ip2, l)).toString();
            }
            sb.append(c);
        }
        return s;
    }*/

/*    public static String formatSingle(String s, String replacement) {//todo: Deprecated - For %s0 = 1
        char[] a = s.toCharArray();
        int l = a.length;
        int lM2 = l - 2;
        //char[] newString = new char[newLength];
        StringBuilder sb = new StringBuilder(l - 3 + replacement.length());
        for (int i = 0; i < lM2; i++) {
            char c = a[i];
            if (c == '%' && a[i + 1] == 's' && a[i + 2] == '0') {
                int ip3 = i + 3;
                if (ip3 >= l) return sb.append(replacement).toString();
                return sb.append(replacement).append(Arrays.copyOfRange(a, ip3, l)).toString();
            }
            sb.append(c);
        }
        return s;
    }*/

    /*    public static String formatMultiple(String s, Object... args) {//todo: Deprecated - For %s<number> > 1
        final int argsLength = args.length; //n << 2 is n * 4
        StringBuilder sb = new StringBuilder(s.length() + (argsLength << 2)); //assume each arg is about 7 characters long (3 in %sn, 4 in argLength * 4)
        char[] a = s.toCharArray();
        int l = a.length;
        int lM2 = l - 2;
        for (int i = 0; i < l; i++) {
            char c = a[i];
            if (i >= lM2) {
                sb.append(c);
                continue;
            }
            if (c == '%' && a[i + 1] == 's') {
                int index = a[i + 2] - 48;//48 is '0' in ascii
                if (index < argsLength && index >= 0) {//the given index is valid
                    sb.append(args[index].toString());
                    i += 2;//skipping 's<number>' in the text
                } else {
                    sb.append(c);
                    //continue, the index was invalid
                }
            } else sb.append(c);
        }
        return sb.toString();
    }*/

    /*    public static StringBuilder defaultBuilder(int capacity) {
        return appendPrefix ? new StringBuilder(capacity + messagePrefix.length()).append(messagePrefix) : new StringBuilder();
    }*/

/*    public static UserDataFormatter formatPersistentData(PersistentUserData data) {
        return new UserDataFormatter(data);
    }*/
}