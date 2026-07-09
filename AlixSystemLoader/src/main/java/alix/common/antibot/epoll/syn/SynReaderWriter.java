package alix.common.antibot.epoll.syn;

import alix.common.antibot.epoll.syn.signature.SynSignature;
import alix.common.antibot.epoll.syn.signature.SynSignatureBuilder;

public interface SynReaderWriter {

    int enableSynSaving0(int serverFd);

    SynSignature parseSynSignature(int fd);

    static SynReaderWriter createImpl() {
        try {
            return new NettySynImpl();
        } catch (Throwable e) {
            return null;
        }
    }

    int IPPROTO_TCP = 6;//TCP options (level)
    int TCP_SAVE_SYN = 27;//server opt set
    int TCP_SAVED_SYN = 28;//client opt read

    static SynSignature parseSignature(byte[] packet) {
        if (packet == null || packet.length < 20) return null;

        SynSignatureBuilder sig = new SynSignatureBuilder();
        int version = (packet[0] >> 4) & 0x0F;
        sig.ipVersion = version;
        int tcpOffset;

        // --- 1. IP Layer Quirks Processing ---
        if (version == 4) {
            int ipHeaderLen = (packet[0] & 0x0F) * 4;
            sig.ipOptLen = ipHeaderLen - 20;
            sig.ttl = Byte.toUnsignedInt(packet[8]);

            // Check Don't Fragment (DF) bit (Byte 6, Mask 0x40)
            sig.df = (packet[6] & 0x40) != 0;

            // Parse IP Identification Field (Bytes 4-5)
            int ipId = (Byte.toUnsignedInt(packet[4]) << 8) | Byte.toUnsignedInt(packet[5]);
            sig.ipIdZero = (ipId == 0);

            tcpOffset = ipHeaderLen;
        } else if (version == 6) {
            if (packet.length < 40) return sig.build();
            sig.ttl = Byte.toUnsignedInt(packet[7]); // Hop Limit
            sig.ipOptLen = 0;                         // Fixed size base header
            sig.df = true;                            // IPv6 behaves like DF natively
            sig.ipIdZero = true;
            tcpOffset = 40;
        } else {
            return sig.build(); // Unknown Protocol
        }

        if (sig.ttl <= 64) {
            sig.initialTtl = 64;   // Linux / macOS / Mobile
        } else if (sig.ttl <= 128) {
            sig.initialTtl = 128;  // Windows
        } else if (sig.ttl <= 192) {
            sig.initialTtl = 192;  // Scanning
        } else {
            sig.initialTtl = 255;  // Cisco / Core / Solaris
        }
        sig.distance = sig.initialTtl - sig.ttl;

        if (tcpOffset + 20 <= packet.length) {
            int wSizeByte1 = Byte.toUnsignedInt(packet[tcpOffset + 14]);
            int wSizeByte2 = Byte.toUnsignedInt(packet[tcpOffset + 15]);
            sig.windowSize = (wSizeByte1 << 8) | wSizeByte2;

            int tcpHeaderLen = ((Byte.toUnsignedInt(packet[tcpOffset + 12]) >> 4) & 0x0F) * 4;
            int optionsLen = tcpHeaderLen - 20;

            if (optionsLen > 0 && (tcpOffset + 20 + optionsLen) <= packet.length) {
                int optIdx = 0;
                int baseOffset = tcpOffset + 20;
                StringBuilder layoutBuilder = new StringBuilder();

                while (optIdx < optionsLen) {
                    int kind = Byte.toUnsignedInt(packet[baseOffset + optIdx]);

                    if (kind == 0) {
                        if (layoutBuilder.length() > 0) layoutBuilder.append(",");
                        layoutBuilder.append("eol");
                        break;
                    }
                    if (kind == 1) {
                        if (layoutBuilder.length() > 0) layoutBuilder.append(",");
                        layoutBuilder.append("nop");
                        optIdx++;
                        continue;
                    }

                    if (optIdx + 1 >= optionsLen) break;
                    int optLen = Byte.toUnsignedInt(packet[baseOffset + optIdx + 1]);
                    if (optLen < 2) break;

                    if (layoutBuilder.length() > 0) layoutBuilder.append(",");

                    switch (kind) {
                        case 2: // MSS
                            layoutBuilder.append("mss");
                            if (optLen == 4 && optIdx + 3 < optionsLen) {
                                sig.mss = (Byte.toUnsignedInt(packet[baseOffset + optIdx + 2]) << 8) |
                                          Byte.toUnsignedInt(packet[baseOffset + optIdx + 3]);
                            }
                            break;
                        case 3: // Window Scale
                            layoutBuilder.append("ws");
                            if (optLen == 3 && optIdx + 2 < optionsLen) {
                                sig.windowScale = Byte.toUnsignedInt(packet[baseOffset + optIdx + 2]);
                            }
                            break;
                        case 4: // SACK Permitted
                            layoutBuilder.append("sok");
                            if (optLen == 2) {
                                sig.sackPermitted = true;
                            }
                            break;
                        case 8: // Timestamps
                            layoutBuilder.append("ts");
                            if (optLen == 10 && optIdx + 9 < optionsLen) {
                                sig.hasTimestamp = true;
                                sig.tsVal = ((long) Byte.toUnsignedInt(packet[baseOffset + optIdx + 2]) << 24) |
                                            ((long) Byte.toUnsignedInt(packet[baseOffset + optIdx + 3]) << 16) |
                                            ((long) Byte.toUnsignedInt(packet[baseOffset + optIdx + 4]) << 8) |
                                            (long) Byte.toUnsignedInt(packet[baseOffset + optIdx + 5]);
                            }
                            break;
                        default:
                            layoutBuilder.append("?");
                            break;
                    }
                    optIdx += optLen;
                }
                sig.optionsLayout = layoutBuilder.toString();
            } else {
                sig.optionsLayout = "none";
            }
        }

        // --- 3. Compile Standardized p0f Signature String ---
        String mssStr = (sig.mss == -1) ? "*" : String.valueOf(sig.mss);
        String wScaleStr = (sig.windowScale == -1) ? "*" : String.valueOf(sig.windowScale);

        StringBuilder quirks = new StringBuilder();
        if (sig.df) quirks.append("df");
        if (version == 4) {
            if (!sig.ipIdZero) {
                if (quirks.length() > 0) quirks.append(",");
                quirks.append("id+"); // Changing / Incremental ID
            } else {
                if (quirks.length() > 0) quirks.append(",");
                quirks.append("id-"); // Zeroed out ID
            }
        }
        String quirksStr = quirks.length() == 0 ? "none" : quirks.toString();

        sig.p0fSignature = String.format("%d:%d:%d:%s:%d,%s:%s:%s:0",
                sig.ipVersion, sig.initialTtl, sig.ipOptLen, mssStr,
                sig.windowSize, wScaleStr, sig.optionsLayout, quirksStr);

        return sig.build();
    }
}