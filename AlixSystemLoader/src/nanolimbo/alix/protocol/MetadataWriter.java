package nanolimbo.alix.protocol;

import nanolimbo.alix.protocol.registry.Version;

@FunctionalInterface
public interface MetadataWriter {

    void writeData(ByteMessage message, Version version);

}
