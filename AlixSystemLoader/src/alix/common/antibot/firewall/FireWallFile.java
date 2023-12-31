package alix.common.antibot.firewall;


import alix.common.utils.file.FileManager;

public final class FireWallFile extends FileManager {

    protected FireWallFile() {
        super("firewall.txt");
    }

    @Override
    protected void loadLine(String line) {
        String[] s = line.split("\\|");
        FireWallManager.add0(s[0], new FireWallEntry(s[1]));
    }
}