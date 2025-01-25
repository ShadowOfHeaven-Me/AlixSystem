/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo.protocol;

import alix.common.utils.netty.BufUtils;
import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.compression.CompressionHandler;
import ua.nanit.limbo.connection.pipeline.compression.GlobalCompressionHandler;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.util.map.VersionMap;

import java.util.HashMap;
import java.util.Map;

import static ua.nanit.limbo.protocol.PacketSnapshots.snapshots;

/**
 * PacketSnapshot encodes a packet to byte array for each MC version.
 * Some versions have the same snapshot, so there are mappings to avoid data copying
 */
public final class PacketSnapshot implements PacketOut {

    //private static final CompressionHandler COMPRESSION = GlobalCompressionHandler.INSTANCE;
    private final PacketOut packet;
    //private final Map<Version, ByteBuf> cache = new ConcurrentHashMap<>();
    private final VersionMap<ByteBuf> encodings;

    //private final Map<Version, byte[]> versionMessages = new EnumMap<>(Version.class);
    //private final Map<Version, Version> mappings = new EnumMap<>(Version.class);

    private PacketSnapshot(PacketOut packet) {
        this.packet = packet;
        this.encodings = encode(packet);
        snapshots.offerLast(this);
    }

    void release() {
        this.encodings.forEach(buf -> {
            int refCnt = buf.refCnt();
            if (refCnt != 0) buf.release(refCnt);
        });
    }

    public ByteBuf getEncoded(Version version) {
        ByteBuf encoding = this.encodings.get(version);
        if (encoding == null)
            throw new AlixException("NULL ENCODING: VER: " + version + " PACKET: " + this.packet.getClass().getSimpleName());
        return encoding;
    }

/*    public ByteBuf getOrCreateRaw(State.PacketRegistry encoderMappings, Version version, CompressionHandler handler) throws Exception {
        ByteBuf cache = this.cache.get(version);
        if (cache != null) {
            ByteBuf n = BufUtils.constBuffer(PacketDuplexHandler.encodeToRaw0(this.packet, encoderMappings, version, handler));
            if (n.equals(cache)) Log.error("CACHE IS THE SAME");
            else
                Log.error("COMPARE: NEW: " + Arrays.toString(new ByteMessage(n).toByteArray()) + " CACHE: " + Arrays.toString(new ByteMessage(cache).toByteArray()));
            return cache;
        }

        ByteBuf newBuf = BufUtils.constBuffer(PacketDuplexHandler.encodeToRaw0(this.packet, encoderMappings, version, handler));
        this.cache.put(version, newBuf);
        return newBuf;
    }*/

    /*public PacketOut getWrappedPacket() {
        return packet;
    }*/

    @SneakyThrows
    private static VersionMap<ByteBuf> encode(PacketOut packet) {
        VersionMap<ByteBuf> encodings = new VersionMap<>();
        Map<ByteBuf, Version> encoded = new HashMap<>();

        for (Version version : Version.values()) {
            if (version.equals(Version.UNDEFINED)) continue;

            for (State state : State.values()) {
                State.PacketRegistry mappings = state.clientBound.getRegistry(version);
                /*if ((mappings == null || !mappings.hasPacket(packet.getClass())) && packet.getClass() == PacketPlayOutTransaction.class) {
                    Log.error("State: %s mappings %s hasPacket %s version %s", state, mappings, mappings.hasPacket(packet.getClass()), version);
                }*/
                if (mappings == null || !mappings.hasPacket(packet.getClass())) continue;

                CompressionHandler compression = GlobalCompressionHandler.getCompressionFor(packet, version, state);

                ByteBuf buf = PacketDuplexHandler.encodeToRaw0(packet, mappings, version, compression, false);
                Version alreadyCached = encoded.get(buf);

                if (alreadyCached != null) {
                    encodings.put(version, encodings.get(alreadyCached));
                    buf.release();
                } else {
                    encoded.put(buf, version);
                    encodings.put(version, BufUtils.constBuffer(buf));
                }
            }
        }
        return encodings;
    }

    /*public void encode() {
        Map<Integer, Version> hashes = new HashMap<>();

        for (Version version : Version.values()) {
            if (version.equals(Version.UNDEFINED)) continue;

            ByteMessage encodedMessage = ByteMessage.create();
            packet.encode(encodedMessage, version);

            int hash = encodedMessage.hashCode();
            Version hashed = hashes.get(hash);

            if (hashed != null) {
                //mappings.put(version, hashed);
            } else {
                hashes.put(hash, version);
                //mappings.put(version, version);
                versionMessages.put(version, encodedMessage.toByteArray());
            }

            encodedMessage.release();
        }
    }*/

    @Override
    public void encode(ByteMessage msg, Version version) {
        throw new AlixException("PacketSnapshot should use it's raw cache, tried encoding instead");
        //this.packet.encode(msg, version);

        /*Version mapped = mappings.get(version);
        byte[] message = versionMessages.get(mapped);

        if (message != null)
            msg.writeBytes(message);
        else
            throw new IllegalArgumentException("No mappings for version " + version);*/
    }

    @Override
    public String toString() {
        return packet.getClass().getSimpleName();
    }

    @Override
    public PacketSnapshot toSnapshot() {
        return this;
    }

    public static PacketSnapshot of(PacketOut packet) {
        if (packet instanceof PacketSnapshot) return (PacketSnapshot) packet;
        return new PacketSnapshot(packet);
    }
}
