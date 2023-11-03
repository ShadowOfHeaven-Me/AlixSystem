package shadow.utils.main.file.temp;

import shadow.utils.main.file.FileManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public abstract class TemporaryFile extends FileManager {

    protected TemporaryFile(String fileName) {
        super(fileName);
    }

    @Override
    public void load() {
        try {
            super.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveKeyAndVal(Map<?, ?> map, String separator) {
        try {
            super.saveKeyAndVal(map, separator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Map<?, ?> map) {
        try {
            super.save(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save0(Collection<?> values) {
        try {
            super.save0(values);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}