package ua.nanit.limbo.protocol.packets.play.blocks;

import alix.libs.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import alix.libs.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import alix.libs.com.github.retrooper.packetevents.util.Vector3i;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PacketPlayOutBlockSectionUpdate extends OutRetrooperPacket<WrapperPlayServerMultiBlockChange> {

    public PacketPlayOutBlockSectionUpdate() {
        super(WrapperPlayServerMultiBlockChange.class);
    }

    public static List<PacketPlayOutBlockSectionUpdate> packetsToSpoof(Map<Vector3i, StateType> blocks, ClientVersion clientVersion) {
        //Chunk, Blocks on the given chunk
        Map<Long, List<SpoofedBlock>> blockMap = new HashMap<>();

        for (Map.Entry<Vector3i, StateType> entry : blocks.entrySet())
            blockMap.computeIfAbsent(chunkSection(entry.getKey()), l -> new ArrayList<>()).add(new SpoofedBlock(entry.getKey(), entry.getValue()));

        List<PacketPlayOutBlockSectionUpdate> packets = new ArrayList<>();

        for (Map.Entry<Long, List<SpoofedBlock>> entry : blockMap.entrySet()) {
            List<EncodedBlock> encodedBlocks = new ArrayList<>();
            for (SpoofedBlock block : entry.getValue()) {
                int blockId = block.type.getMapped().getId(clientVersion);

                int localX = block.loc.x & 0xF;
                int localY = block.loc.y & 0xF;
                int localZ = block.loc.z & 0xF;

                encodedBlocks.add(new EncodedBlock(blockId, localX, localY, localZ));
            }
            long chunkPos = entry.getKey();

            int sectionX = (int) (chunkPos >> 42);
            int sectionY = (int) (chunkPos << 44 >> 44);
            int sectionZ = (int) (chunkPos << 22 >> 42);

            Vector3i vec3i = new Vector3i(sectionX, sectionY, sectionZ);

            PacketPlayOutBlockSectionUpdate sectionUpdate = new PacketPlayOutBlockSectionUpdate();
            WrapperPlayServerMultiBlockChange wrapper = sectionUpdate.wrapper();

            wrapper.setChunkPosition(vec3i);
            wrapper.setTrustEdges(true);
            wrapper.setBlocks(encodedBlocks.toArray(new EncodedBlock[0]));

            packets.add(sectionUpdate);
        }
        return packets;
    }

    private static final class SpoofedBlock {

        private final Vector3i loc;
        private final StateType type;

        private SpoofedBlock(Vector3i loc, StateType type) {
            this.loc = loc;
            this.type = type;
        }
    }

    private static long chunkSection(Vector3i loc) {
        long sectionX = loc.x >> 4;
        long sectionY = loc.y >> 4;
        long sectionZ = loc.z >> 4;

        return ((sectionX & 0x3FFFFF) << 42) | (sectionY & 0xFFFFF) | ((sectionZ & 0x3FFFFF) << 20);
    }

    private static int floor(double num) {
        int floor = (int) num;
        return (double) floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }
}