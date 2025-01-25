package alix.common.utils.file;


import alix.common.AlixCommonMain;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AlixFileManager {

    private final File file;
    private static final File INTERNAL_FOLDER, SECRETS_FOLDER;

    static {
        INTERNAL_FOLDER = new File(AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolderPath().toAbsolutePath() + File.separator + "internal");
        INTERNAL_FOLDER.mkdir();
        SECRETS_FOLDER = new File(AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolderPath().toAbsolutePath() + File.separator + "secrets");
        SECRETS_FOLDER.mkdir();
    }

    protected AlixFileManager(File file) {//existing file
        this.file = file;
        file.toPath();//buffer the path object
    }

    protected AlixFileManager(String fileName, FileType type) {
        this(fileName, type, true);
    }

    protected AlixFileManager(String fileName, FileType type, boolean init) {
        this(init ? initializeFile(fileName, type) : getPluginFile(fileName, type));
    }

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    //private static final boolean isUtf32;
    public static final int HIGHEST_CHAR = 382;

    public enum FileType {

        CONFIG, INTERNAL, SECRET

    }

    /*static {
        Charset c;
        boolean utf32;
        try {
           c = Charset.forName("utf-32");
           utf32 = true;
        } catch (Throwable e) {
            c = StandardCharsets.UTF_16;
            utf32 = false;
        }
        CHARSET = c;
        isUtf32 = utf32;
        HIGHEST_CHAR = isUtf32 ? 1 << 31 : 1 << 15;
    }*/

    //private static final boolean isUTF8Default = Charset.defaultCharset() == StandardCharsets.UTF_8;

    /*public static String toUTF8(String s) {
        if (isUTF8Default) return s;
        return s.getBytes(StandardCharsets.ISO_8859_1)
    }*/

/*    public static void readNonEmptyLines(File file, Consumer<String> consumer) {
        try {
            readNonEmptyLines0(file, consumer);
        } catch (IOException e) {
            throw new AlixException(e);
        }
    }*/

/*    public static void readNonEmptyLines0(File file, Consumer<String> consumer) throws IOException {
        readLines(Files.newInputStream(file.toPath()), consumer, false);
    }*/

    public static void readLines(File file, Consumer<String> consumer, boolean acceptEmpty) throws IOException {
        readLines(Files.newInputStream(file.toPath()), consumer, acceptEmpty);
    }

    public static void readLines(InputStream is, Consumer<String> consumer, boolean acceptEmpty) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, CHARSET))) {
            //Files.readAllLines(

            String line;

            while ((line = reader.readLine()) != null) if (acceptEmpty || !line.isEmpty()) consumer.accept(line);
        }
    }

    public static <K, V> void writeKeyAndVal(File file, Map<K, V> map, String separator, Predicate<V> shouldSave, Function<K, String> keyFormatter, Function<V, String> valueFormatter) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), CHARSET))) {

            Function<V, String> valueFormatter0 = valueFormatter == null ? V::toString : valueFormatter;
            Function<K, String> keyFormatter0 = keyFormatter == null ? K::toString : keyFormatter;
            Predicate<V> shouldSave0 = shouldSave == null ? v -> true : shouldSave;//assume all entries should be saved if not specified

            for (Map.Entry<K, V> e : map.entrySet()) {
                if (!shouldSave0.test(e.getValue())) continue;
                writer.write(keyFormatter0.apply(e.getKey()) + separator + valueFormatter0.apply(e.getValue()));
                writer.newLine();
            }
        }
    }

    public static void write(File file, Collection<?> lines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), CHARSET))) {//new BufferedWriter(new FileWriter(file, CHARSET))
            for (Object data : lines) {
                writer.write(data.toString());
                writer.newLine();
            }
        }
    }

    public static File writeJarCompiledFileIntoDest(String s, FileType type) {
        return writeJarCompiledFileIntoDest(getPluginFile(s, type), s);
    }

    public static File writeJarCompiledFileIntoDest(File copyInto, String s) {
        Class<?> clazz = AlixCommonMain.MAIN_CLASS_INSTANCE.getClass();

        boolean written = writeJarCompiledFileIntoDest(copyInto, clazz.getResourceAsStream(s)) != null;
        if (!written)
            written = writeJarCompiledFileIntoDest(copyInto, clazz.getClassLoader().getResourceAsStream(s)) != null;

        if (!written)
            throw new AlixException("how");

        return copyInto;
    }

    public static File writeJarCompiledFileIntoDest(File copyInto, InputStream in) {
        if (in == null) return null;
        try (InputStream in0 = in) {
            copy(in0, copyInto);
            return copyInto;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File createPluginFile(String s, FileType type) {
        return createFileIfAbsent(writeJarCompiledFileIntoDest(s, type));
    }

    public static void createNewFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copy(InputStream in, File dest) {
        try (OutputStream out = Files.newOutputStream(dest.toPath())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) out.write(buffer, 0, bytesRead);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> getLines(File file) {
        try {
            List<String> lines = new ArrayList<>();
            readLines(file, lines::add, true);
            return lines;
        } catch (IOException e) {
            throw new AlixException(e);
        }
    }

    protected static File getPluginFile(String fileName, FileType type) {
        String path = AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolderPath().toAbsolutePath().toString();

        switch (type) {
            case SECRET:
                return getPluginFileInFolder(SECRETS_FOLDER, fileName);
            case INTERNAL:
                return getPluginFileInFolder(INTERNAL_FOLDER, fileName);
            case CONFIG:
                return new File(path + File.separator + fileName);//return the default file path if the file is not an internal one
            default:
                throw new AlixError("Invalid: " + type);
        }
    }

    private static File getPluginFileInFolder(File folder, String fileName) {
        String path = AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolderPath().toAbsolutePath().toString();
        File oldFile = new File(path + File.separator + fileName);
        File newFile = new File(folder.getAbsolutePath() + File.separator + fileName);
        if (oldFile.exists()) {
            try {
                Files.copy(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                oldFile.delete();
                return newFile;
            } catch (IOException e) {
                throw new ExceptionInInitializerError(e);
            }
        } else {
            return newFile;
        }
    }

    private static File initializeFile(String fileName, FileType type) {
        File file = getPluginFile(fileName, type);
        return createFileIfAbsent(file);
    }

    public static File createFileIfAbsent(File file) {
        //if (file.exists()) return file; <- Unnecessary
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void loadLine(String line);

    public final File getFile() {
        return file;
    }

    public void remove() {
        file.delete();
        //remove(this);
    }

/*    File f = File.createTempFile("plugin",".yml");
            Files.copy(in, f.toPath(), StandardCopyOption.REPLACE_EXISTING);*/

    /*        try (InputStream in = Main.instance.getResource(s)) {
            Files.copy(in, f.toPath());
            return f;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

    public void loadExceptionless() {
        try {
            this.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException {
        readLines(file, this::loadLine, false);
    }

    protected void loadSingularValue() {
        try {
            DataInputStream stream = new DataInputStream(Files.newInputStream(file.toPath()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            loadLine(reader.readLine());

            reader.close();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <K, V> void saveKeyAndVal(Map<K, V> map, String separator) throws IOException {
        this.saveKeyAndVal(map, separator, null);
    }

    public synchronized <K, V> void saveKeyAndVal(Map<K, V> map, String separator, Predicate<V> predicate) throws IOException {
        writeKeyAndVal(this.file, map, separator, predicate, null, null);
    }

    public synchronized <K, V> void saveKeyAndVal(Map<K, V> map, String separator, Predicate<V> predicate, Function<K, String> keyFormatter, Function<V, String> valueFormatter) throws IOException {
        writeKeyAndVal(this.file, map, separator, predicate, keyFormatter, valueFormatter);
    }

    public void save(Map<?, ?> map) throws IOException {
        save0(map.values());
    }

    public synchronized void save0(Collection<?> values) throws IOException {
        write(file, values);
    }

    protected void saveSingularValue(String value) throws IOException {
        FileWriter stream = new FileWriter(file);
        BufferedWriter writer = new BufferedWriter(stream);

        writer.write(value);

        writer.close();
        stream.close();
    }
}