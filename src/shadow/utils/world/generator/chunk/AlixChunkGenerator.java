package shadow.utils.world.generator.chunk;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import shadow.utils.main.file.managers.SpawnFileManager;
import shadow.utils.world.AlixWorld;

import java.util.Random;

public final class AlixChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        return createEmptyChunkData(world);
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return AlixWorld.TELEPORT_LOCATION; //SpawnFileManager.file.getSpawn().getLocation();
    }

    private static ChunkData createEmptyChunkData(World world) {
        ChunkData chunkData = Bukkit.createChunkData(world);
        // Set all block types to air (empty)
        chunkData.setRegion(0, 0, 0, 16, 256, 16, Material.AIR);
        return chunkData;
    }
}