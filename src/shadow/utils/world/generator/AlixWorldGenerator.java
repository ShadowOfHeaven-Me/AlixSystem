package shadow.utils.world.generator;

import org.bukkit.*;
import org.jetbrains.annotations.Nullable;
import shadow.Main;
import shadow.systems.executors.captcha.CaptchaRespawnExecutors;
import shadow.utils.world.generator.chunk.AlixChunkGenerator;

public final class AlixWorldGenerator extends WorldCreator {

    public AlixWorldGenerator(String name) {
        super(name);
        this.type(WorldType.FLAT);
        //this.environment(World.Environment.NORMAL);
        this.generateStructures(false);

        //this.generatorSettings("2;0;1");
        this.generator(new AlixChunkGenerator());
    }

    @Nullable
    @Override
    public World createWorld() {
        World w = Bukkit.getWorld(this.name());
        World world = w != null ? w : super.createWorld();
        init(world);
        return world;
    }

    private static void init(World world) {
        world.setTime(18000);
        world.setAmbientSpawnLimit(0);
        world.setAnimalSpawnLimit(0);
        world.setWaterAnimalSpawnLimit(0);
        world.setMonsterSpawnLimit(0);
        world.setPVP(false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, -1);
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, -1);
        world.setGameRule(GameRule.SPAWN_RADIUS, 1);
        world.setGameRule(GameRule.DO_TILE_DROPS, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, true);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        initCorrectRespawn(world);
    }

    private static void initCorrectRespawn(World world) {
        GameRule rule = GameRule.getByName("doImmediateRespawn");
        if (rule != null) world.setGameRule((GameRule<Boolean>) rule, true);
        Main.pm.registerEvents(new CaptchaRespawnExecutors(rule == null), Main.plugin);
    }
}
