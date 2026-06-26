package alix.common.antibot.firewall;


import alix.common.antibot.firewall.entry.FireWallEntry;
import alix.common.antibot.ip.IPUtils;
import alix.common.utils.file.AlixFileManager;

public final class FireWallFile extends AlixFileManager {

    FireWallFile() {
        super("firewall.txt", FileType.INTERNAL);
    }

    @Override
    protected void loadLine(String line) {
        String[] s = line.split("\\|");
        var reason = s[1];
        long timeoutAt;

        if (s.length >= 3) {
            timeoutAt = Long.parseLong(s[2]);
        } else {
            if (reason.startsWith(FireWallManager.EXCEPTION_CAUGHT_KEY))
                timeoutAt = System.currentTimeMillis() + FireWallManager.EXCEPTION_TIMEOUT.toMillis();
            else
                timeoutAt = FireWallManager.NO_TIMEOUT;
        }

        FireWallManager.add0(IPUtils.fromAddress(s[0]), FireWallEntry.from(reason, timeoutAt));
    }
}