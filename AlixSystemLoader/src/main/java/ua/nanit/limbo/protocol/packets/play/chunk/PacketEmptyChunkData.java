package ua.nanit.limbo.protocol.packets.play.chunk;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public final class PacketEmptyChunkData implements PacketOut {

    //Additional sources: https://github.com/BoomEaro/NanoLimbo/blob/1.21.5_1.9_1/src/main/java/ua/nanit/limbo/protocol/packets/play/PacketEmptyChunk.java
    //Source code: https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/fallback/protocol/packets/play/ChunkDataPacket.java

    private int x;
    private int z;

    public PacketEmptyChunkData setX(int x) {
        this.x = x;
        return this;
    }

    public PacketEmptyChunkData setZ(int z) {
        this.z = z;
        return this;
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        msg.writeInt(this.x);
        msg.writeInt(this.z);

        if (version.moreOrEqual(Version.V1_17)) {
            if (version.lessOrEqual(Version.V1_17_1)) {
                msg.writeVarInt( 0); // mask
            }
        } else {
            msg.writeBoolean(true); // full chunk

            if (version.inBetween(Version.V1_16, Version.V1_16_1)) {
                msg.writeBoolean(true); // ignore old data
            }

            if (version.more(Version.V1_8)) {
                msg.writeVarInt( 0);
            } else {
                msg.writeShort(1); // fix void chunk
            }
        }

        if (version.moreOrEqual(Version.V1_14)) {
            if (version.moreOrEqual(Version.V1_21_5)) {
                // In 1.21.5. They changed to List<EnumMap<Heightmap.Type, long[]>>
                msg.writeVarInt( 1); // List size
                msg.writeVarInt( 4); // Ordinal of MOTION_BLOCKING
                // Write long array
                msg.writeVarInt( 37);
                for (int i = 0; i < 37; i++) {
                    msg.writeLong(0);
                }
            } else { // Nbt for older version
                final long[] motionBlockingData = new long[version.less(Version.V1_18) ? 36 : 37];
                final CompoundBinaryTag motionBlockingTag = CompoundBinaryTag.builder()
                        .put("MOTION_BLOCKING", LongArrayBinaryTag.longArrayBinaryTag(motionBlockingData))
                        .build();
                final CompoundBinaryTag rootTag = CompoundBinaryTag.builder()
                        .put("root", motionBlockingTag)
                        .build();

                msg.writeBinaryTag(version, rootTag);
            }

            if (version.inBetween(Version.V1_15, Version.V1_17_1)) {
                if (version.moreOrEqual(Version.V1_16_2)) {
                    msg.writeVarInt( 1024);

                    for (int i = 0; i < 1024; i++) {
                        msg.writeVarInt( 1);
                    }
                } else {
                    for (int i = 0; i < 1024; i++) {
                        msg.writeInt(0);
                    }
                }
            }
        }

        if (version.less(Version.V1_8)) {
            msg.writeInt(0);
            msg.writeBytes(new byte[2]);
        } else if (version.less(Version.V1_13)) {
            msg.writeVarInt( 0);
        } else if (version.less(Version.V1_15)) {
            msg.writeBytes(new byte[256 * 4]);
        } else if (version.less(Version.V1_18)) {
            msg.writeVarInt( 0);
        } else {
            // nonEmptyBlockCount. short occupies 2 bytes.
            // SingularPalette (byte) + id (varint) + long array with size (older)
            // Since 1.21.5. The length of the long array of PaletteStorage no longer depends on the VarInt in the packet
            // SingularPalette doesn't need to read any additional long array.
            // So we'll remove suffix 0 as array length here. The cost of writing a palette has been reduced from 3 to 2 bytes.
            final byte[] sectionData = version.less(Version.V1_21_5) ? new byte[]{0, 0, 0, 0, 0, 0, 1, 0} : new byte[]{0, 0, 0, 0, 0, 1};
            int count = 24;
            msg.writeVarInt( sectionData.length * count);

            for (int i = 0; i < count; i++) {
                msg.writeBytes(sectionData);
            }
        }

        if (version.moreOrEqual(Version.V1_9_4)) {
            msg.writeVarInt( 0);
        }

        if (version.moreOrEqual(Version.V1_21_2)) {
            for (int i = 0; i < 6; i++) {
                msg.writeVarInt( 0);
            }
        } else if (version.moreOrEqual(Version.V1_18)) {
            final byte[] lightData = new byte[]{1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0};

            msg.ensureWritable(lightData.length);

            if (version.moreOrEqual(Version.V1_20)) {
                msg.writeBytes(lightData, 1, lightData.length - 1);
            } else {
                msg.writeBytes(lightData);
            }
        }
    }

//    @Override
//    public void encode(ByteMessage msg, Version version) {
//        msg.writeInt(x);
//        msg.writeInt(z);
//
//        if (version.moreOrEqual(V1_17)) {
//            if (version.lessOrEqual(V1_17_1)) {
//                msg.writeVarInt( 0); // mask
//            }
//        } else {
//            msg.writeBoolean(true); // full chunk
//
//            if (version.fromTo(V1_16, V1_16_1)) {
//                msg.writeBoolean(true); // ignore old data
//            }
//
//            if (version.more(V1_8)) {
//                msg.writeVarInt( 0);
//            } else {
//                msg.writeShort(1); // fix void chunk
//            }
//        }
//
//        if (version.moreOrEqual(V1_14)) {
//            final long[] motionBlockingData = new long[version.less(V1_18) ? 36 : 37];
//            final CompoundBinaryTag motionBlockingTag = CompoundBinaryTag.builder()
//                    .put("MOTION_BLOCKING", LongArrayBinaryTag.longArrayBinaryTag(motionBlockingData))
//                    .build();
//            final CompoundBinaryTag rootTag = CompoundBinaryTag.builder()
//                    .put("root", motionBlockingTag)
//                    .build();
//
//            if (version.less(V1_20_2)) msg.writeCompoundTag(rootTag);
//            else msg.writeNamelessCompoundTag(rootTag);
//
//            if (version.fromTo(V1_15, V1_17_1)) {
//                 if (version.moreOrEqual(V1_16_2)) {
//                     msg.writeVarInt(1024);
//
//                    for (int i = 0; i < 1024; i++) {
//                        msg.writeVarInt( 1);
//                    }
//                } else {
//                    for (int i = 0; i < 1024; i++) {
//                        msg.writeInt(0);
//                    }
//                }
//            }
//        }
//
//        if (version.less(V1_8)) {
//            msg.writeInt(0);
//            msg.writeBytes(new byte[2]);
//        } else if (version.less(V1_13)) {
//            msg.writeVarInt( 0);
//        } else if (version.less(V1_15)) {
//            msg.writeBytesArray(new byte[256 * 4]);
//        } else if (version.less(V1_18)) {
//            msg.writeVarInt( 0);
//        } else {
//            final byte[] sectionData = new byte[]{0, 0, 0, 0, 0, 0, 1, 0};
//            int count = version.moreOrEqual(V1_21_2) ? 24 : 16;
//            msg.writeVarInt( sectionData.length * count);
//
//            for (int i = 0; i < count; i++) {
//                msg.writeBytes(sectionData);
//            }
//        }
//
//        if (version.moreOrEqual(V1_9_4)) {
//            msg.writeVarInt( 0);
//        }
//
//        if (version.moreOrEqual(V1_21_2)) {
//            for (int i = 0; i < 6; i++) {
//                msg.writeVarInt( 0);
//            }
//        } else if (version.moreOrEqual(V1_18)) {
//            final byte[] lightData = new byte[]{1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0};
//
//            msg.ensureWritable(lightData.length);
//
//            if (version.moreOrEqual(V1_20)) {
//                msg.writeBytes(lightData, 1, lightData.length - 1);
//            } else {
//                msg.writeBytes(lightData);
//            }
//        }
//    }
}
