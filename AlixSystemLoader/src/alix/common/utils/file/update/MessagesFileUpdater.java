package alix.common.utils.file.update;

import alix.common.utils.file.AlixFileManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class MessagesFileUpdater {

    //Replaces the old formatting (%s<number>) with the universally known one ({<number>})

    public static void updateFormatting(File messagesFile) {
        List<String> lines = AlixFileManager.getLines(messagesFile);
        for (int i = 0; i < lines.size(); i++) {
            String line = reformat(lines.get(i));
            lines.set(i, line);
        }
        try {
            AlixFileManager.write(messagesFile, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String reformat(String s) {
        return s.replaceAll("%s0", "{0}").replaceAll("%s1", "{1}").replaceAll("%s2", "{2}").replaceAll("%s3", "{3}").replaceAll("%s4", "{4}");
    }

    private MessagesFileUpdater() {
    }
}