package alix.common.antibot.epoll.syn.analysis;

import lombok.Getter;

@Getter
public enum OS {
    // Unknown (Middle-ground: could be an exotic router or a poorly spoofed packet)
    UNKNOWN("Unknown", 50),

    // Windows Families - Primary Minecraft Playerbase
    WINDOWS_10_OR_11("Windows 10/11", 0),
    WINDOWS_MODERN_GENERIC("Modern Windows (Generic)", 5),
    WINDOWS_7_OR_NT_6("Windows 7/8 (or NT 6.x)", 15),
    WINDOWS_NT_6_GENERIC("Windows NT kernel 6.x (Generic)", 20),
    WINDOWS_XP("Windows XP", 40), // Highly unusual for modern MC, likely spoofed
    WINDOWS_NT_5_GENERIC("Windows NT kernel 5.x (Generic)", 45),
    WINDOWS_NT_GENERIC("Windows NT kernel (Generic)", 30),
    BLACKBERRY("Blackberry", 80), // Mobile, but irrelevant for modern MC

    // Apple / iOS / FreeBSD
    MAC_OS_OR_IOS_MODERN("macOS 11+ or modern iOS", 0),
    MAC_OS_X_10_9_OR_NEWER("macOS X 10.9 or newer (or iOS)", 5),
    MAC_OS_X_LEGACY("Mac OS X (Legacy)", 25),
    IOS_LEGACY("iOS (Legacy)", 20),
    APPLE_GENERIC("Apple (Generic)", 15),
    FREEBSD_9_OR_NEWER("FreeBSD 9.x or newer", 40), // Occasional console OS base (PS4/PS5), but unusual for PC
    FREEBSD_8("FreeBSD 8.x", 60),
    FREEBSD_GENERIC("FreeBSD (Generic)", 50),

    // Linux Families - Mixed Bag (Some real players, many datacenter bots)
    LINUX_5_X_OR_6_X("Linux 5.x or 6.x", 10), // Steam Deck / Linux Gamers
    LINUX_ANDROID("Linux (Android)", 0), // Geyser/Bedrock mobile players
    LINUX_MODERN_GENERIC("Modern Linux (Generic)", 40),
    LINUX_3_11_TO_4_X("Linux 3.11 to 4.x", 50),
    LINUX_3_1_TO_3_10("Linux 3.1-3.10", 60),
    LINUX_3_X_GENERIC("Linux 3.x (Generic)", 65),
    LINUX_2_6_X("Linux 2.6.x", 75),
    LINUX_2_4_X("Linux 2.4.x", 85),
    LINUX_2_4_TO_2_6_GENERIC("Linux 2.4.x-2.6.x (Generic)", 80),
    UNIX_LINUX_UNKNOWN("Unix/Linux (Unknown version)", 60),

    // Special Cases - Instant Drops
    NMAP_SYN_SCAN("NMap: SYN scan", 100),
    BAIDU_SPIDER("BaiduSpider", 100),
    SOLARIS_6("Solaris 6", 100),
    ROUTER_OR_NETWORK_INFRA("Router / Network Infrastructure (TTL 255)", 90),
    P0F_SENDSYN("p0f: sendsyn utility", 100);

    private final String readableName;
    private final int suspicionScore;

    OS(String readableName, int suspicionScore) {
        this.readableName = readableName;
        this.suspicionScore = suspicionScore;
    }

    public boolean isWindows() {
        return switch (this) {
            case WINDOWS_10_OR_11,
                 WINDOWS_MODERN_GENERIC,
                 WINDOWS_7_OR_NT_6,
                 WINDOWS_NT_6_GENERIC,
                 WINDOWS_XP,
                 WINDOWS_NT_5_GENERIC,
                 WINDOWS_NT_GENERIC -> true;
            default -> false;
        };
    }

    public boolean isLinux() {
        return switch (this) {
            case LINUX_5_X_OR_6_X,
                 LINUX_ANDROID,
                 LINUX_MODERN_GENERIC,
                 LINUX_3_11_TO_4_X,
                 LINUX_3_1_TO_3_10,
                 LINUX_3_X_GENERIC,
                 LINUX_2_6_X,
                 LINUX_2_4_X,
                 LINUX_2_4_TO_2_6_GENERIC,
                 UNIX_LINUX_UNKNOWN -> true;
            default -> false;
        };
    }

    public boolean isMacOS() {
        return switch (this) {
            case MAC_OS_OR_IOS_MODERN,
                 MAC_OS_X_10_9_OR_NEWER,
                 MAC_OS_X_LEGACY,
                 APPLE_GENERIC -> true;
            default -> false;
        };
    }
}