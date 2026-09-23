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
        return appendPrefix
                && !m.isEmpty()//a blank spacer line should stay a genuinely blank line, not "<prefix> " with
                //nothing after it - matches how e.g. LuckPerms' own console/chat output never tags its blank
                //separator lines either
                && !m.startsWith(messagePrefix)//quick-fix for my own mistakes
                ? messagePrefix + m : m;
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
        replacement = escapeMiniMessage(replacement);
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
                    sb.append(escapeMiniMessage(String.valueOf(args[index])));
                    i += 2;//skipping '<digit>}' in the text, as the first '{' is already skipped by the default for(i) iterator
                    continue;//continue to the next loop and stop this
                }//continue, the index was invalid
            }
            sb.append(c);
        }
        return sb.toString();
    }

    //Escapes MiniMessage's own special characters ('\' and '<' - '>' alone can never start a tag, so leaving
    //it as-is avoids a spurious visible backslash for the rare value that happens to contain one, without any
    //loss of safety) in a value about to be substituted into a message template via {0}-style formatting - a
    //nickname, an IP, an email, or any other data pulled in at runtime - so it can never be interpreted as
    //MiniMessage tag syntax once the finished string reaches MessageWrapper's MiniMessage parsing step. A
    //player naming themselves e.g. "<click:run_command:'/x'>" (or any other real, recognized tag) must never
    //become a live tag in someone else's client just because that name got substituted into a message. The
    //STATIC template text itself (e.g. a literal "<player>" placeholder already baked into
    //messages.properties/messages.txt) is deliberately left unescaped instead - see MessageWrapper's own
    //comment for why that's handled safely a different way (non-strict parsing).
    private static String escapeMiniMessage(String s) {
        if (s.indexOf('\\') < 0 && s.indexOf('<') < 0)
            return s;//fast path - the overwhelming majority of substituted values (numbers, statuses, IPs...) contain neither
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '<') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }

    @AlixIntrinsified(method = "ChatColor.translateAlternateColorCodes")
    public static String translateColors(String text) {//Faster than ChatColor.translateAlternateColorCodes
        if (text == null) return null;
        char[] c = text.toCharArray();
        //Hex codes ("&#RRGGBB") expand into a longer, multi-character raw sequence ("§x§R§R§G§G§B§B"), unlike
        //every other code here which is a straight 1-for-1 '&'->'§' swap done in place on the same char[] -
        //so they need a separate, StringBuilder-based pass. Pre-scanning for one first (cheap, no allocation)
        //keeps every message that doesn't use hex - still the overwhelming majority - on the exact same
        //fast, allocation-free path as before hex was supported at all.
        if (containsHexCode(c)) return translateColorsWithHex(c);

        int lM1 = c.length - 1;
        for (int i = 0; i < lM1; i++)
            if (c[i] == '&') {
                char d = c[++i];                                   //k, l, m, n, o
                if (d >= 'a' && d <= 'f' || d >= '0' && d <= '9' || d >= 'k' && d <= 'o' || d == 'r') c[i - 1] = '§';
            }
        return new String(c);
    }

    private static boolean containsHexCode(char[] c) {
        int limit = c.length - 8;//last valid start index for an 8-character "&#RRGGBB" sequence
        for (int i = 0; i <= limit; i++)
            if (c[i] == '&' && c[i + 1] == '#' && isHex(c, i + 2)) return true;
        return false;
    }

    private static boolean isHex(char[] c, int from) {
        for (int i = from; i < from + 6; i++) {
            char h = c[i];
            if (!(h >= '0' && h <= '9' || h >= 'a' && h <= 'f' || h >= 'A' && h <= 'F')) return false;
        }
        return true;
    }

    //Handles both "&#RRGGBB" hex codes (expanded into the raw "§x§R§R§G§G§B§B" sequence Minecraft's legacy
    //chat component format expects for a hex color) and the regular single-character codes translateColors()
    //handles above - only reached once containsHexCode() has confirmed at least one hex code is present.
    private static String translateColorsWithHex(char[] c) {
        StringBuilder sb = new StringBuilder(c.length + 16);
        int lM1 = c.length - 1;
        int i = 0;
        while (i < c.length) {
            if (i < lM1 && c[i] == '&') {
                char d = c[i + 1];
                if (d == '#' && i <= lM1 - 7 && isHex(c, i + 2)) {
                    sb.append('§').append('x');
                    for (int j = i + 2; j < i + 8; j++) sb.append('§').append(c[j]);
                    i += 8;
                    continue;
                }
                if (d >= 'a' && d <= 'f' || d >= '0' && d <= '9' || d >= 'k' && d <= 'o' || d == 'r') {
                    sb.append('§').append(d);
                    i += 2;
                    continue;
                }
            }
            sb.append(c[i]);
            i++;
        }
        return sb.toString();
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