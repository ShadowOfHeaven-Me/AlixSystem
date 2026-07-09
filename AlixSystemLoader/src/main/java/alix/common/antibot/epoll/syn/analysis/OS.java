package alix.common.antibot.epoll.syn.analysis;

import lombok.Getter;

@Getter
public enum OS {
    // Unknown
    UNKNOWN("Unknown"),

    // Windows Families
    WINDOWS_10_OR_11("Windows 10/11"),
    WINDOWS_MODERN_GENERIC("Modern Windows (Generic)"),
    WINDOWS_7_OR_NT_6("Windows 7/8 (or NT 6.x)"),
    WINDOWS_NT_6_GENERIC("Windows NT kernel 6.x (Generic)"),
    WINDOWS_XP("Windows XP"),
    WINDOWS_NT_5_GENERIC("Windows NT kernel 5.x (Generic)"),
    WINDOWS_NT_GENERIC("Windows NT kernel (Generic)"),
    BLACKBERRY("Blackberry"),

    // Apple / iOS / FreeBSD
    MAC_OS_OR_IOS_MODERN("macOS 11+ or modern iOS"),
    MAC_OS_X_10_9_OR_NEWER("macOS X 10.9 or newer (or iOS)"),
    MAC_OS_X_LEGACY("Mac OS X (Legacy)"),
    IOS_LEGACY("iOS (Legacy)"),
    APPLE_GENERIC("Apple (Generic)"),
    FREEBSD_9_OR_NEWER("FreeBSD 9.x or newer"),
    FREEBSD_8("FreeBSD 8.x"),
    FREEBSD_GENERIC("FreeBSD (Generic)"),

    // Linux Families
    LINUX_5_X_OR_6_X("Linux 5.x or 6.x"),
    LINUX_ANDROID("Linux (Android)"),
    LINUX_MODERN_GENERIC("Modern Linux (Generic)"),
    LINUX_3_11_TO_4_X("Linux 3.11 to 4.x"),
    LINUX_3_1_TO_3_10("Linux 3.1-3.10"),
    LINUX_3_X_GENERIC("Linux 3.x (Generic)"),
    LINUX_2_6_X("Linux 2.6.x"),
    LINUX_2_4_X("Linux 2.4.x"),
    LINUX_2_4_TO_2_6_GENERIC("Linux 2.4.x-2.6.x (Generic)"),

    // Special Cases
    UNIX_LINUX_UNKNOWN("Unix/Linux (Unknown version)"),
    NMAP_SYN_SCAN("NMap: SYN scan"),
    BAIDU_SPIDER("BaiduSpider"),
    SOLARIS_6("Solaris 6"),
    ROUTER_OR_NETWORK_INFRA("Router / Network Infrastructure (TTL 255)"),
    P0F_SENDSYN("p0f: sendsyn utility");

    private final String readableName;

    OS(String readableName) {
        this.readableName = readableName;
    }
}