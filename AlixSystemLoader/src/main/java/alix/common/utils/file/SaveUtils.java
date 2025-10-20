package alix.common.utils.file;

import alix.common.utils.other.throwable.AlixException;

public final class SaveUtils {

    public static String asSavable(char separatorChar, Object... args) {
        StringBuilder sb = new StringBuilder(args.length << 4);//try to estimate the size
        String separator = String.valueOf(separatorChar);
        for (int i = 0; i < args.length; i++) {
            Object o = args[i];
            String savable = String.valueOf(o);

            if (savable.contains(separator)) new AlixException("STRING '" + savable + "' contains separator char '" + separator + "'! Report this immediately!").printStackTrace();

            sb.append(savable);
            if (i != args.length - 1) sb.append(separatorChar);
        }
        return sb.toString();
    }
}