package alix.common.messages.file.extracted;

import alix.common.AlixCommonMain;
import alix.common.messages.Messages;
import alix.common.messages.file.MessagesFile;
import alix.common.utils.file.AlixFileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ExtractedMessages extends AlixFileManager {

    private final List<String> messages, syntaxes;

    public ExtractedMessages(MessagesFile file) {
        super("extracted-messages.txt", FileType.CONFIG, false);
        this.messages = new ArrayList<>();
        this.syntaxes = new ArrayList<>();
        this.syntaxes.addAll(file.getMap().keySet());
        Collection<String> values = file.getMap().values();
        List<String> list = new ArrayList<>(values.size());
        for (String s : values) list.add(s.replaceAll("§", "&"));
        try {
            super.save0(list);//writing the messages into the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void loadLine(String line) {
        messages.add(line);
    }

    public List<String> getFormattedMessages() {
        int size = messages.size();
        List<String> formatted = new ArrayList<>(size);
        //FUNCTIONALITY (audit, 2026-09-24): was a hardcoded ": " - MessagesFile#loadLine() splits on
        //messagesSeparator() (now '=', per this session's earlier delimiter revert), so a merge
        //(/as m-e then /as m-m) regenerated messages.txt with a delimiter the loader could no longer parse,
        //silently dropping virtually every message to the "<Message not found>" placeholder on next restart.
        char separator = AlixCommonMain.MAIN_CLASS_INSTANCE.getEngineParams().messagesSeparator();
        for (int i = 0; i < size; i++)
            formatted.add(syntaxes.get(i) + separator + " " + messages.get(i));
        Collections.sort(formatted);
        return formatted;
    }

    public static ExtractedMessages findFile() {
        File f = AlixFileManager.getPluginFile("extracted-messages.txt", FileType.CONFIG);
        if (f.exists()) return new ExtractedMessages(Messages.getFileInstance());
        else return null;
    }
}