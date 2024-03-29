package alix.common.utils.file;


import alix.common.AlixCommonMain;
import alix.common.utils.other.throwable.AlixException;
import alix.loaders.bukkit.BukkitAlixMain;

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

    protected final File file;
    protected static final File INTERNAL_FOLDER;

    static {
        INTERNAL_FOLDER = new File(AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolderPath().toAbsolutePath() + File.separator + "internal");
        INTERNAL_FOLDER.mkdir();
    }

    protected AlixFileManager(File file) {//existing file
        this.file = file;
        file.toPath();//buffer the path object
    }

    protected AlixFileManager(String fileName) {
        this(fileName, true, true);
    }

    protected AlixFileManager(String fileName, boolean internal) {
        this(fileName, true, internal);
    }

    protected AlixFileManager(String fileName, boolean init, boolean internal) {
        this(init ? initializeFile(fileName, internal) : getPluginFile(fileName, internal));
    }

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    //private static final boolean isUtf32;
    public static int HIGHEST_CHAR = 382;

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

    public static void readLines(InputStream is, Consumer<String> consumer) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, CHARSET));
            //Files.readAllLines

            String line;

            while ((line = reader.readLine()) != null) if (!line.isEmpty()) consumer.accept(line);

            reader.close();
        } catch (Exception e) {
            throw new AlixException(e);
        }
    }

    public static <K, V> void writeKeyAndVal(File file, Map<K, V> map, String separator, Predicate<V> shouldSave, Function<K, String> keyFormatter, Function<V, String> valueFormatter) throws IOException {
        FileWriter stream = new FileWriter(file);
        BufferedWriter writer = new BufferedWriter(stream);

        Function<V, String> valueFormatter0 = valueFormatter == null ? V::toString : valueFormatter;
        Function<K, String> keyFormatter0 = keyFormatter == null ? K::toString : keyFormatter;
        Predicate<V> shouldSave0 = shouldSave == null ? v -> true : shouldSave;//assume all entries should be saved if not specified

        for (Map.Entry<K, V> e : map.entrySet()) {
            if (!shouldSave0.test(e.getValue())) continue;
            writer.write(keyFormatter0.apply(e.getKey()) + separator + valueFormatter0.apply(e.getValue()));
            writer.newLine();
        }

        writer.close();
        stream.close();
    }

    public static void write(File file, Collection<?> lines) throws IOException {
        FileWriter stream = new FileWriter(file);
        BufferedWriter writer = new BufferedWriter(stream);

        for (Object data : lines) {
            writer.write(data.toString());
            writer.newLine();
        }

        writer.close();
        stream.close();
    }

    public static File getWithJarCompiledFile(String s, boolean internal) {
        return getWithJarCompiledFile(getPluginFile(s, internal), s);
    }

    public static File getWithJarCompiledFile(File copyInto, String s) {
        //tmpdir - tf is this
        try (InputStream in = BukkitAlixMain.class.getClassLoader().getResourceAsStream(s)) {
            Files.copy(in, copyInto.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return copyInto;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File createPluginFile(String s) {
        return createPluginFile(s, false);
    }

    public static File createPluginFile(String s, boolean internal) {
        return createFileIfAbsent(getWithJarCompiledFile(s, internal));
    }

    public static void createNewFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getLines(File file) {
        try {
            DataInputStream stream = new DataInputStream(Files.newInputStream(file.toPath()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            List<String> lines = new ArrayList<>();

            String line;

            while ((line = reader.readLine()) != null) lines.add(line);

            reader.close();
            stream.close();

            return lines;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getPluginFile(String fileName) {
        return getPluginFile(fileName, false);
    }

    public static File getPluginFile(String fileName, boolean internal) {
        String path = AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolderPath().toAbsolutePath().toString();

        if (internal) {
            File oldFile = new File(path + File.separator + fileName);
            File newFile = new File(INTERNAL_FOLDER.getAbsolutePath() + File.separator + fileName);
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

        //new File(path).mkdirs(); //Creating the folder if absent

        return new File(path + File.separator + fileName);//return the default file path if the file is not an internal one
    }

    private static File initializeFile(String fileName, boolean internal) {
        File file = getPluginFile(fileName, internal);
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
        readLines(Files.newInputStream(file.toPath()), this::loadLine);
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