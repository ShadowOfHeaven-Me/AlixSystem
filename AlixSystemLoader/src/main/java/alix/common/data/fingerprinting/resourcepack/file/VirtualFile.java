package alix.common.data.fingerprinting.resourcepack.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class VirtualFile {

    public static final char separator = File.separatorChar;
    private final String path;
    private final List<VirtualFile> parentOf = new ArrayList<>();
    private final boolean isDirectory;
    public byte[] contents;

    public VirtualFile() {
        this.path = "";
        this.isDirectory = true;
    }

    public VirtualFile(File file) {
        this.path = file.getAbsolutePath();
        this.isDirectory = file.isDirectory();
    }

    public VirtualFile(VirtualFile parent, String actualName) {
        var path = parent.path;
        this.path = path.isEmpty() ? actualName : path + separator + actualName;
        parent.parentOf.add(this);
        this.isDirectory = this.isDir0(actualName);
    }

    //not exactly true, should be ending with File.separator check,
    // but will suffice for now
    private boolean isDir0(String actualName) {
        return !actualName.contains(".");
    }

    public List<VirtualFile> parentOf() {
        return parentOf;
    }

    public String path() {
        return path;
    }
}