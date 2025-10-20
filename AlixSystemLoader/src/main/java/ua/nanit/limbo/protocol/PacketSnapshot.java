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

import alix.common.utils.collections.queue.AlixQueue;
import alix.common.utils.collections.queue.ConcurrentAlixDeque;
import alix.common.utils.netty.BufUtils;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.compression.CompressionHandler;
import ua.nanit.limbo.connection.pipeline.compression.CompressionSupplier;
import ua.nanit.limbo.connection.pipeline.compression.GlobalCompressionHandler;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.Log;
import ua.nanit.limbo.util.map.VersionMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * PacketSnapshot encodes a packet to byte array for each MC version.
 * Some versions have the same snapshot, so there are mappings to avoid data copying
 */
public final class PacketSnapshot implements PacketOut {

    //private static final CompressionHandler COMPRESSION = GlobalCompressionHandler.INSTANCE;
    static final AlixQueue<PacketSnapshot> snapshots = new ConcurrentAlixDeque<>();
    public static final boolean floodgateNoCompression = NanoLimbo.INTEGRATION.isFloodgateNoCompressionPresent();
    private final PacketOut packet;
    public final State state;
    //private final Map<Version, ByteBuf> cache = new ConcurrentHashMap<>();
    private final VersionMap<ByteBuf> encodings;

    //Floodgate no compression support
    //private final AbstractVersionMap<ByteBuf> noCompressionEncodings;
    //private final Map<ByteBuf, Version> noCompressionAlreadyCached;

    //private final Map<Version, byte[]> versionMessages = new EnumMap<>(Version.class);
    //private final Map<Version, Version> mappings = new EnumMap<>(Version.class);

    private PacketSnapshot(PacketOut packet) {
        this.packet = packet;
        this.state = State.getState(packet);
        this.encodings = NanoLimbo.usePacketSnapshots ? encode0(packet, state, CompressionSupplier.GLOBAL) : null;
        //encode(packet, CompressionSupplier.NULL_SUPPLIER)
        /*if (CompressionHandler.COMPRESSION_ENABLED)
            this.noCompressionEncodings = floodgateNoCompression ? new ConcurrentVersionMap<>() : null;
        else this.noCompressionEncodings = this.encodings;

        this.noCompressionAlreadyCached = floodgateNoCompression ? new ConcurrentHashMap<>(4, 1, 4) : null;*/
        snapshots.offerLast(this);
    }

    void release() {
        //this.encodings.forEach(ByteBuf::release);
        if (!NanoLimbo.usePacketSnapshots)
            return;

        this.encodings.forEach(buf -> {
            int refCnt = buf.refCnt();
            if (refCnt != 0) buf.unwrap().release();
        });
    }

    private ByteBuf getEnsureNotNull(Version version) {
        //terrible performance, but this should only be used in debugging anyway
        if (!NanoLimbo.usePacketSnapshots)
            return this.encodePacket(version, GlobalCompressionHandler.INSTANCE, true);

        var buf = this.encodings.get(version);
        if (buf == null)
            throw new AlixError("NULL ENCODING: VER: " + version + " PACKET: " + packet.getClass().getSimpleName());
        return buf;
    }

    public ByteBuf getEncoded(Version version) {
        if (NanoLimbo.debugSnapshots) Log.error("PACKET: " + this.packet + " version=" + version);

        return this.getEnsureNotNull(version.getEncodingSafe());
    }

    private ByteBuf encodePacket(Version version, CompressionHandler handler, boolean pooled) {
        State.PacketRegistry mappings = this.state.clientBound.getRegistry(version);
        return PacketDuplexHandler.encodeToRaw0(packet, mappings, version, handler, pooled);
    }

    public ByteBuf getEncodedNoCompression(Version version) {
        return CompressionHandler.COMPRESSION_ENABLED ? this.encodePacket(version, null, true) : this.getEncoded(version);
        //ByteBuf encoding = this.noCompressionEncodings.computeIfAbsent(version, this::createEncodedNoCompression);
        //return this.ensureNotNull(encoding, version);
    }

    /*private ByteBuf createEncodedNoCompression(Version version) {
        State.PacketRegistry mappings = this.state.clientBound.getRegistry(version);
        ByteBuf buf = PacketDuplexHandler.encodeToRaw0(packet, mappings, version, null, false);//todo: consider making this pooled

        Version alreadyCached = this.noCompressionAlreadyCached.get(buf);
        if (alreadyCached != null) {
            buf.release();
            return this.noCompressionEncodings.get(alreadyCached);
        }

        this.noCompressionAlreadyCached.put(buf, version);
        return BufUtils.constBuffer(buf);
    }*/

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
    private static VersionMap<ByteBuf> encode0(PacketOut packet, State state, CompressionSupplier compressionSupplier) {
        VersionMap<ByteBuf> encodings = new VersionMap<>();
        Map<ByteBuf, Version> encoded = new HashMap<>();

        for (Version version : Version.values()) {
            if (version.isUndefined()) continue;

            State.PacketRegistry mappings = state.clientBound.getRegistry(version);
                /*if ((mappings == null || !mappings.hasPacket(packet.getClass())) && packet.getClass() == PacketPlayOutTransaction.class) {
                    Log.error("State: %s mappings %s hasPacket %s version %s", state, mappings, mappings.hasPacket(packet.getClass()), version);
                }*/
            if (mappings == null || !mappings.hasPacket(packet.getClass())) continue;

            CompressionHandler compression = compressionSupplier.getHandlerFor(packet, version, state);

            //Consider: Use a pooled buf for faster allocation, and later copy into an unpooled
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
        return encodings;
    }

    public Collection<ByteBuf> buffers() {
        if (!NanoLimbo.usePacketSnapshots)
            return Collections.EMPTY_LIST;

        return this.encodings.valuesSnapshot();
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

    public PacketOut getPacket() {
        return packet;
    }

    @Override
    public PacketSnapshot toSnapshot() {
        return this;
    }

    public static PacketSnapshot of(PacketOut packet) {
        return packet instanceof PacketSnapshot snapshot ? snapshot : new PacketSnapshot(packet);
    }
}
