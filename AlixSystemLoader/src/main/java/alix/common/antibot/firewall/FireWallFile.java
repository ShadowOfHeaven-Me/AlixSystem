package alix.common.antibot.firewall;


import alix.common.antibot.IPUtils;
import alix.common.utils.file.AlixFileManager;

public final class FireWallFile extends AlixFileManager {

    FireWallFile() {
        super("firewall.txt", FileType.INTERNAL);
    }

    @Override
    protected void loadLine(String line) {
        String[] s = line.split("\\|");
        FireWallManager.add0(IPUtils.fromAddress(s[0]), new FireWallEntry(s[1]));
    }
}