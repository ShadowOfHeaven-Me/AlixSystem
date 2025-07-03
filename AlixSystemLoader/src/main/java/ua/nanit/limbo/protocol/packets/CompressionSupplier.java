package ua.nanit.limbo.protocol.packets;

import ua.nanit.limbo.connection.pipeline.compression.CompressionHandler;
import ua.nanit.limbo.connection.pipeline.compression.GlobalCompressionHandler;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;

@FunctionalInterface
public interface CompressionSupplier {

    CompressionHandler getHandlerFor(Packet packet, Version version, State state);

    CompressionSupplier GLOBAL = GlobalCompressionHandler::getCompressionFor;
    CompressionSupplier NULL_SUPPLIER = supply0(null);

    static CompressionSupplier supply(CompressionHandler handler) {
        return handler == null ? NULL_SUPPLIER : supply0(handler);
    }

    private static CompressionSupplier supply0(CompressionHandler handler) {
        return (p, v, s) -> handler;
    }
}