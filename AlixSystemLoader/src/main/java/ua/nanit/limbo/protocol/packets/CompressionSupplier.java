package ua.nanit.limbo.protocol.packets;

import ua.nanit.limbo.connection.pipeline.compression.CompressionHandler;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;

@FunctionalInterface
public interface CompressionSupplier {

    CompressionHandler getHandlerFor(Packet packet, Version version, State state);

    CompressionSupplier NULL_SUPPLIER = supply(null);

    static CompressionSupplier supply(CompressionHandler handler) {
        return (p, v, s) -> handler;
    }
}