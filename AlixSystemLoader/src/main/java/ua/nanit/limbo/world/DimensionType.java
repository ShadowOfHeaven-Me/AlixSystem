package ua.nanit.limbo.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.data.NamespacedKey;
import ua.nanit.limbo.util.map.DefaultVersionMap;
import ua.nanit.limbo.util.map.VersionMap;

@RequiredArgsConstructor
@Getter
public enum DimensionType {

    OVERWORLD(NamespacedKey.minecraft("overworld"), 0),
    THE_END(NamespacedKey.minecraft("the_end"), 1),
    THE_NETHER(NamespacedKey.minecraft("the_nether"), -1);

    private final NamespacedKey key;
    private final int legacyDimensionId;

    public VersionedDimension createVersionedDimension(DimensionRegistry dimensionRegistry) {
        VersionMap<Dimension> perVersionDimension = new DefaultVersionMap<>();
        for (Version version : Version.values()) {
            Dimension dimension = dimensionRegistry.findDimension(version, this.key);
            if (dimension == null)
                continue;

            perVersionDimension.put(version, dimension);
        }
        return new VersionedDimension(this.key, this.legacyDimensionId, perVersionDimension);
    }
}