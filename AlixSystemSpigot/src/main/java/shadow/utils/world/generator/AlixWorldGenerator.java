package shadow.utils.world.generator;

import org.bukkit.*;
import org.jetbrains.annotations.Nullable;
import shadow.Main;
import shadow.systems.executors.captcha.CaptchaRespawnExecutors;
import shadow.utils.misc.ReflectionUtils;
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
        World world = super.createWorld();
        init(world);
        return world;
    }

    private static void init(World world) {
        world.setTime(20_000);
        //world.setFullTime(18000);
        world.setAmbientSpawnLimit(-1);
        world.setAnimalSpawnLimit(-1);
        world.setWaterAnimalSpawnLimit(-1);
        world.setMonsterSpawnLimit(-1);
        world.setTicksPerAnimalSpawns(-1);
        world.setTicksPerMonsterSpawns(-1);
        world.setPVP(false);
        world.setAutoSave(false);
        //world.getViewDistance()
        world.setSpawnFlags(false, false);
        world.setDifficulty(Difficulty.EASY);

        if (ReflectionUtils.invokeIfPresent(ReflectionUtils.getMethodOrNull(World.class, "setViewDistance", int.class), world, 2)) {
            Main.logInfo("Optimizing chunk rendering in the verification world, thanks to the Paper environment");
            ReflectionUtils.invokeIfPresent(ReflectionUtils.getMethodOrNull(World.class, "setSimulationDistance", int.class), world, 2);
            ReflectionUtils.invokeIfPresent(ReflectionUtils.getMethodOrNull(World.class, "setSendViewDistance", int.class), world, 2);
        }
        //setSendViewDistance

        //Main.logError("METADATA " + world.getMetadata());
        try {
            Class.forName("org.bukkit.GameRule");
            initGameRulesModern(world);
            initCorrectRespawnModern(world);
        } catch (ClassNotFoundException ex) {
            initGameRulesOld(world);
            initCorrectRespawnOld();
        }
    }

    private static void initGameRulesModern(World world) {
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, -1);
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, -1);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, -1);
        world.setGameRule(GameRule.DO_TILE_DROPS, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }

    private static void initGameRulesOld(World world) {
        world.setGameRuleValue("randomTickSpeed", "-1");
        world.setGameRuleValue("maxEntityCramming", "-1");
        world.setGameRuleValue("spawnRadius", "-1");
        world.setGameRuleValue("doTileDrops", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("disableRaids", "true");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("spectatorsGenerateChunks", "false");
        world.setGameRuleValue("disableElytraMovementCheck", "true");
        world.setGameRuleValue("doEntityDrops", "false");
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setGameRuleValue("mobGriefing", "false");
        world.setGameRuleValue("showDeathMessages", "false");
        world.setGameRuleValue("doMobLoot", "false");
        world.setGameRuleValue("announceAdvancements", "false");
    }

    private static void initCorrectRespawnModern(World world) {
        GameRule rule = GameRule.getByName("doImmediateRespawn");
        if (rule != null) world.setGameRule((GameRule<Boolean>) rule, false);
        Main.pm.registerEvents(new CaptchaRespawnExecutors(rule == null), Main.plugin);
    }

    private static void initCorrectRespawnOld() {
        Main.pm.registerEvents(new CaptchaRespawnExecutors(true), Main.plugin);
    }
}
