package alix.common.messages.file.extracted;

import alix.common.messages.file.MessagesFile;
import alix.common.messages.Messages;
import alix.common.utils.file.AlixFileManager;
import alix.common.utils.file.temp.TemporaryFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExtractedMessages extends TemporaryFile {

    private final List<String> messages, syntaxes;

    public ExtractedMessages(MessagesFile file) {
        super("extracted-messages.txt");
        this.messages = new ArrayList<>();
        this.syntaxes = new ArrayList<>();
        this.syntaxes.addAll(file.getMap().keySet());
        try {
            super.save0(file.getMap().values());//writing the messages into the file
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
        for (int i = 0; i < size; i++)
            formatted.add(syntaxes.get(i) + ": " + messages.get(i));
        Collections.sort(formatted);
        return formatted;
    }

    public static ExtractedMessages findFile() {
        File f = AlixFileManager.getPluginFile("extracted-messages.txt");
        if (f.exists()) return new ExtractedMessages(Messages.getFileInstance());
        else return null;
    }
}