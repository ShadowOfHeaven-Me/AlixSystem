package alix.common.utils.file;


import alix.common.AlixCommonMain;
import alix.loaders.bukkit.BukkitAlixMain;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class FileManager {

    private final File file;

    protected static final File INTERNAL_FOLDER;

    static {
        INTERNAL_FOLDER = new File(AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolderPath().toAbsolutePath() + File.separator + "internal");
        INTERNAL_FOLDER.mkdir();
    }

    protected FileManager(File file) {//existing file
        this.file = file;
        file.toPath();//buffer the path object
    }

    protected FileManager(String fileName) {
        this(fileName, true, true);
    }

    protected FileManager(String fileName, boolean internal) {
        this(fileName, true, internal);
    }

    protected FileManager(String fileName, boolean init, boolean internal) {
        this(init ? initializeFile(fileName, internal) : getPluginFile(fileName, internal));
    }

    public static void write(File file, Collection<?> values) throws IOException {
        FileWriter stream = new FileWriter(file);
        BufferedWriter writer = new BufferedWriter(stream);

        for (Object data : values) {
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

    public void read() {
        try {
            this.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException {
        try {
            DataInputStream stream = new DataInputStream(Files.newInputStream(file.toPath()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String line;

            while ((line = reader.readLine()) != null) if (!line.isEmpty()) loadLine(line);

            reader.close();
            stream.close();
        } catch (IOException e) {
            throw new IOException(file.getName(), e);
        }
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

    public <K, V> void saveKeyAndVal(Map<K, V> map, String separator, Function<V, String> valueFormatter) throws IOException {
        List<String> list = new ArrayList<>(map.size());
        for (Map.Entry<K, V> e : map.entrySet()) list.add(e.getKey() + separator + valueFormatter.apply(e.getValue()));
        save0(list);
    }

    public void saveKeyAndVal(Map<?, ?> map, String separator) throws IOException {
        List<String> list = new ArrayList<>(map.size());
        for (Map.Entry<?, ?> e : map.entrySet()) list.add(e.getKey() + separator + e.getValue());
        save0(list);
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