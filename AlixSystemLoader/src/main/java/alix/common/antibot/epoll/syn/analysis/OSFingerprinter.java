package alix.common.antibot.epoll.syn.analysis;

import alix.common.antibot.epoll.syn.signature.SynSignatureBuilder;

import java.util.Locale;

public final class OSFingerprinter {

    public static OS identifyOS(SynSignatureBuilder sig) {
        if (sig == null) {
            return OS.UNKNOWN;
        }

        String layout = normalizeLayout(sig.optionsLayout);

        if (sig.initialTtl == 128) {
            return classifyWindows(sig, layout);
        }

        if (sig.initialTtl == 64) {
            OS appleOrBsd = classifyAppleAndBSD(sig, layout);
            if (appleOrBsd != null) {
                return appleOrBsd;
            }

            OS linux = classifyLinux(sig, layout);
            if (linux != null) {
                return linux;
            }

            if (sig.mss == 1460 && sig.windowSize == (sig.mss * 4)) {
                if (layoutStartsWith(layout, "mss,sok,nop,nop,nop") && layoutEndsWith(layout, ",ws")) {
                    return OS.BAIDU_SPIDER;
                }
            }

            return OS.UNIX_LINUX_UNKNOWN;
        }

        if (sig.initialTtl == 255) {
            if (sig.df && sig.mss > 0 && sig.windowSize == (sig.mss * 7)) {
                return OS.SOLARIS_6;
            }
            return OS.ROUTER_OR_NETWORK_INFRA;
        }

        if (sig.initialTtl == 192) {
            return OS.P0F_SENDSYN;
        }

        return OS.UNKNOWN;
    }

    private static OS classifyWindows(SynSignatureBuilder sig, String layout) {
        int score = 0;

        if (sig.df) score += 2;
        if (sig.mss == 1460) score += 2;
        if (sig.sackPermitted) score += 1;
        if (sig.hasTimestamp) score += 1;

        if (sig.windowSize == 64240 || sig.windowSize == 65535) score += 3;
        if (sig.windowSize == 8192) score += 2;
        if (sig.windowSize == 16384) score += 2;

        if (sig.windowScale == 8) score += 3;
        if (sig.windowScale == 7) score += 2;
        if (sig.windowScale == 1 || sig.windowScale == 3) score += 1;

        if (layoutEqualsAny(layout,
                "mss,nop,ws,sok,ts",
                "mss,nop,ws,nop,nop,sok",
                "mss,nop,ws,nop,nop,sok,ts",
                "mss,nop,ws,sok")) {
            score += 4;
        }

        if (layoutEqualsAny(layout,
                "mss,nop,nop,sok",
                "mss,nop,ws,nop,nop,sok")) {
            if (sig.windowSize == 8192) {
                return OS.WINDOWS_7_OR_NT_6;
            }
        }

        if (sig.windowScale != 8 && (sig.windowSize == 16384 || sig.windowSize == 65535)) {
            if (layoutEqualsAny(layout, "mss,nop,nop,sok") && sig.df) {
                return OS.WINDOWS_XP;
            }
            return OS.WINDOWS_NT_5_GENERIC;
        }

        if (score >= 11 && (sig.windowSize == 64240 || sig.windowSize == 65535)) {
            if (sig.windowScale == 8) {
                return OS.WINDOWS_10_OR_11;
            }
            return OS.WINDOWS_MODERN_GENERIC;
        }

        if (score >= 8) {
            return OS.WINDOWS_MODERN_GENERIC;
        }

        if (sig.windowSize == 8192) {
            return OS.WINDOWS_NT_6_GENERIC;
        }

        return OS.WINDOWS_NT_GENERIC;
    }

    private static OS classifyAppleAndBSD(SynSignatureBuilder sig, String layout) {
        if (sig.windowSize != 65535) {
            return null;
        }

        if (layoutEqualsAny(layout, "mss,nop,ws,nop,nop,ts,sok,eol+1") && sig.df) {
            if (sig.windowScale >= 5 && sig.windowScale <= 7) return OS.MAC_OS_OR_IOS_MODERN;
            if (sig.windowScale == 4) return OS.MAC_OS_X_10_9_OR_NEWER;
            if (sig.windowScale == 1 || sig.windowScale == 3) return OS.MAC_OS_X_LEGACY;
            if (sig.windowScale == 2) return OS.IOS_LEGACY;
            return OS.APPLE_GENERIC;
        }

        if (layoutEqualsAny(layout, "mss,nop,ws,sok,ts") && sig.df) {
            if (sig.windowScale == 6) return OS.FREEBSD_9_OR_NEWER;
            if (sig.windowScale == 3) return OS.FREEBSD_8;
            return OS.FREEBSD_GENERIC;
        }

        return null;
    }

    private static OS classifyLinux(SynSignatureBuilder sig, String layout) {
        if (!sig.df || sig.mss <= 0) {
            return null;
        }

        int w = sig.windowSize;
        int mss = sig.mss;

        if (layoutEqualsAny(layout, "mss,sok,ts,nop,ws", "mss,sok,ts,ws,nop")) {
            if (w == 64240 || w == (mss * 44)) {
                if (sig.windowScale == 7 || sig.windowScale == 8) return OS.LINUX_5_X_OR_6_X;
                if (sig.windowScale == 1 || sig.windowScale == 3) return OS.LINUX_ANDROID;
                return OS.LINUX_MODERN_GENERIC;
            }

            if (w == (mss * 20)) {
                if (sig.windowScale == 10 || sig.windowScale == 7) return OS.LINUX_3_11_TO_4_X;
                return OS.LINUX_MODERN_GENERIC;
            }

            if (w == (mss * 10)) {
                if (sig.windowScale >= 4 && sig.windowScale <= 7) return OS.LINUX_3_1_TO_3_10;
                return OS.LINUX_3_X_GENERIC;
            }

            if (w == (mss * 4)) {
                if (sig.windowScale >= 6 && sig.windowScale <= 8) return OS.LINUX_2_6_X;
                if (sig.windowScale >= 0 && sig.windowScale <= 2) return OS.LINUX_2_4_X;
                return OS.LINUX_2_4_TO_2_6_GENERIC;
            }
        }

        return null;
    }

    private static boolean layoutEqualsAny(String layout, String candidate) {
        return layout.equals(candidate);
    }

    private static boolean layoutEqualsAny(String layout, String... candidates) {
        for (String candidate : candidates) {
            if (candidate.equals(layout)) {
                return true;
            }
        }
        return false;
    }

    private static boolean layoutStartsWith(String actual, String prefix) {
        return normalizeLayout(actual).startsWith(normalizeLayout(prefix));
    }

    private static boolean layoutEndsWith(String actual, String suffix) {
        return normalizeLayout(actual).endsWith(normalizeLayout(suffix));
    }

    private static String normalizeLayout(String layout) {
        if (layout == null) return "";
        return layout.trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replaceAll(",+", ",")
                .replaceAll("^,|,$", "");
    }
}