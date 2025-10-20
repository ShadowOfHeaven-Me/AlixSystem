package ua.nanit.limbo.connection.pipeline.compression;

import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;

import static ua.nanit.limbo.connection.pipeline.compression.CompressionHandler.COMPRESSION_ENABLED;

@FunctionalInterface
public interface CompressionSupplier {

    CompressionHandler getHandlerFor(Packet packet, Version version, State state);

    CompressionSupplier GLOBAL = GlobalCompressionHandler::getCompressionFor;
    CompressionSupplier NULL_SUPPLIER = supply0(null);

    static CompressionSupplier supply(CompressionHandler handler) {
        return handler == null ? NULL_SUPPLIER : supplyAccountDisabled(handler);
    }

    private static CompressionSupplier supplyAccountDisabled(CompressionHandler handler) {
        return (p, v, s) -> compressDisabled(p, v, s) ? null : handler;
    }

    static boolean compressDisabled(Packet packet, Version version, State state) {
        return !COMPRESSION_ENABLED || version.lessOrEqual(Version.V1_7_6) || !State.isCompressible(state, packet.getClass());
    }

    private static CompressionSupplier supply0(CompressionHandler handler) {
        return (p, v, s) -> handler;
    }
}