//package alix.common.utils.file.temp.files;
//package shadow.utils.main.file.temp.files;
//
//import alix.common.messages.file.MessagesFile;
//import shadow.utils.main.file.temp.TemporaryFile;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class ExtractedCommands extends TemporaryFile {
//
//    private final List<String> messages, syntaxes;
//
//    public ExtractedCommands(MessagesFile file) {
//        super("extracted-commands.txt");
//        this.messages = new ArrayList<>();
//        this.syntaxes = new ArrayList<>();
//        syntaxes.addAll(file.getMap().keySet());
//        save(file.getMap().values());//writing the messages into the file
//    }
//
//    @Override
//    protected void loadLine(String line) {
//        messages.add(line);
//    }
//
//    public List<String> getFormattedMessages() {
//        int size = messages.size();
//        List<String> formatted = new ArrayList<>(size);
//        for (int i = 0; i < size; i++)
//            formatted.add(syntaxes.get(i) + ": " + messages.get(i));
//        Collections.sort(formatted);
//        return formatted;
//    }
//}
