package nanolimbo.alix.protocol.packets.configuration;

import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.MetadataWriter;
import nanolimbo.alix.protocol.PacketOut;
import nanolimbo.alix.protocol.registry.Version;
import nanolimbo.alix.world.DimensionRegistry;

public class PacketRegistryData implements PacketOut {

    private DimensionRegistry dimensionRegistry;
    private MetadataWriter metadataWriter;

    public void setDimensionRegistry(DimensionRegistry dimensionRegistry) {
        this.dimensionRegistry = dimensionRegistry;
    }

    public void setMetadataWriter(MetadataWriter metadataWriter) {
        this.metadataWriter = metadataWriter;
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        if (metadataWriter != null) {
            if (version.moreOrEqual(Version.V1_20_5)) {
                metadataWriter.writeData(msg, version);
                return;
            }
        }
        msg.writeNamelessCompoundTag(dimensionRegistry.getCodec_1_20());
    }
}
