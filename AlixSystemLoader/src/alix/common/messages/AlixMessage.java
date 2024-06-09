package alix.common.messages;

import alix.common.utils.formatter.AlixFormatter;

public final class AlixMessage {

    /*private final String[] messageParts;
    private final int[] formattingIndexes;
    private final int primaryLength;*/
    private final String message;

    AlixMessage(String message) {
        this.message = message;
        /*List<String> messageParts = new ArrayList<>();
        this.formattingIndexes = equivalentIndexes(message, messageParts);
        this.messageParts = messageParts.toArray(new String[0]);
        this.primaryLength = message.length();*/
    }

    public String format(Object... args) {
        return AlixFormatter.format(this.message, args);//format0(this.messageParts, this.formattingIndexes, this.primaryLength, args);
    }

    /*private static String format0(String[] a, int[] indexes, int primaryLength, Object[] args) {
        AlixCommonMain.logError("A: " + Arrays.toString(a) + " indexes: " + Arrays.toString(indexes) + " primaryLength: " + primaryLength);
        StringBuilder sb = new StringBuilder(primaryLength + (args.length << 2)); //assume each arg is about 7 characters long (3 in {<digit>}, 4 in argLength * 4)
        int lM1 = a.length - 1;
        for (int i = 0; i < lM1; i++) {
            sb.append(a[i]);
            int index = indexes[i];
            if (index >= args.length) sb.append("{").append(index).append("}");
            else sb.append(args[index].toString());
        }
        sb.append(a[lM1]);
        return sb.toString();
    }

    private static int[] equivalentIndexes(String s, List<String> list) {
        char[] a = s.toCharArray();
        int lM2 = a.length - 2;
        List<Integer> indexList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            char c = a[i];
            if (i == lM2) {
                list.add(sb.append(a[lM2]).append(a[a.length - 1]).toString());//the remaining text was not skipped and is 2 chars long, so we can skip the character test and simply return the current text + the 2 remaining characters, since the regex is 3 characters long
                return AlixCommonUtils.toPrimitive(indexList.toArray(new Integer[0]));
            }
            if (c == '{' && a[i + 2] == '}') {
                int index = a[i + 1] - 48;//48 is '0' in ascii
                if (index >= 0) {//the given index is valid
                    list.add(sb.toString());
                    sb = new StringBuilder();
                    indexList.add(index);
                    i += 2;//skipping '<digit>}' in the text, as the first '{' is already skipped by the default for(i) iterator
                    continue;//continue to the next loop and stop this
                }//continue, the index was invalid
            }
            sb.append(c);
        }
        return AlixCommonUtils.toPrimitive(indexList.toArray(new Integer[0]));
    }*/
}