package shadow.utils.world;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import shadow.utils.world.generator.AlixWorldGenerator;
import shadow.utils.world.location.ConstLocation;

public final class AlixWorld {

    public static final String worldName = "world_alix_captcha";
    private static final AlixWorld instance = new AlixWorld();
    public static final ConstLocation TELEPORT_LOCATION = instance.teleportLocation;
    public static final ConstLocation TELEPORT_FALL_LOCATION = instance.teleportLocation.asModifiableCopy().add(0, 100, 0).toConst();
    public static final Vector3d TELEPORT_VEC3D = instance.vec3d;
    public static final Vector3i TELEPORT_VEC3I = instance.vec3d.toVector3i();
    public static final World CAPTCHA_WORLD = instance.world;

    //private final List<Integer> playerIds = new ArrayList<>();
    private final World world;
    private final ConstLocation teleportLocation;
    private final Vector3d vec3d;

    private AlixWorld() {
        this.world = new AlixWorldGenerator(worldName).createWorld();

        this.teleportLocation = new ConstLocation(this.world, 0.5, 2, 0.5, 180, 45);
        this.vec3d = new Vector3d(this.teleportLocation.getX(), this.teleportLocation.getY(), this.teleportLocation.getZ());
        this.prepareSpawnCube();
    }

    private void prepareSpawnCube() {
        for (int x = -1; x <= 1; x++) {
            for (int y = 1; y <= 4; y++) {
                for (int z = -1; z <= 1; z++) {
                    boolean air = (y == 2 || y == 3) && x == 0 && z == 0;

                    this.world.getBlockAt(x, y, z).setType(air ? Material.AIR : Material.BARRIER);
                    //Main.logInfo("Block " + x + " " + y + " " + z + " was set to: " + this.world.getBlockAt(x, y, z).getType());
                }
            }
        }
        this.world.getBlockAt(0, 2, 0).setType(Material.COBWEB);
    }

    private void forceloadSpawnChunks() {
        try {
            World.class.getMethod("setChunkForceLoaded", int.class, int.class, boolean.class);
        } catch (Exception ignored) {//the method doesn't exist on lower versions
            return;
        }

        int min = Math.max(Bukkit.getViewDistance(), 2);

        for (int i = -min; i <= min; i++)
            for (int j = -min; j <= min; j++)
                this.world.setChunkForceLoaded(i, j, true);
    }

    public static boolean preload() {
        boolean nn = instance != null;
        if (nn) instance.forceloadSpawnChunks();
        return nn;
    }

/*    public static Location getOriginalLocation(UnverifiedUser user, Location joinedWithLoc) {
        if (instance == null) throw new RuntimeException("World instance is null!");

        Player player = user.getPlayer();

        if (!joinedWithLoc.getWorld().equals(instance.world)) {
            return TELEPORT_LOCATION; //player.teleport(instance.teleportLocation);
        }

        return OriginalLocationsManager.findOrCreateAndAdd(player, user.getOriginalLocation());
    }*/

    //instance.playerIds.add(player.getEntityId());

    //JavaScheduler.runLaterAsync(() -> hideEntities(user), 250, TimeUnit.MILLISECONDS);

/*    public static void teleportBack(UnverifiedUser user) {
        Location originalLocation = user.getOriginalLocation();

        Player player = user.getPlayer();

        if (instance == null) {
            player.teleport(originalLocation);
            return;
        }

        if (originalLocation.getWorld().equals(instance.world)) SpawnFileManager.teleport(player);
        else player.teleport(originalLocation);

        //instance.playerIds.remove(Integer.valueOf(player.getEntityId()));
    }*/

/*    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    private static void hideEntities(UnverifiedUser user) {//as packets about entities are already blocked we just need to remove the already existing entities from the player's view
        List<Integer> list = instance.playerIds;

        int[] ids = new int[list.size() - 1];

        int userId = user.getPlayer().getEntityId();

        for (int i = 0; i < list.size(); i++) {
            int entityId = list.get(i);
            if (userId != entityId) ids[i] = entityId;
        }
        try {
            Object destroyEntityPacket = ReflectionUtils.outDestroyEntityConstructor.newInstance(ids);
            user.getBlocker().getChannel().writeAndFlush(destroyEntityPacket);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }*/

/*    private static void hideFromOthers(Player player) {
        for(Player p : instance.world.getPlayers()) {
            p.hidePlayer(Main.plugin, player);
            player.hidePlayer(Main.plugin, p);
        }
    }*/

/*    public static World teleport(UnverifiedUser user) {
        if (loginWorldInstance == null) throw new RuntimeException("Alix World instance is null!");

        Player p = user.getPlayer();
        PersistentUserData data = user.getData();
        World originalWorld = p.getWorld();

        UUID originalWorldUUID = originalWorld.getUID();

        if (originalWorldUUID.equals(loginWorldInstance.worldUUID)) {//Player is already in the login world

            if (data == null || data.getOriginalWorldUUID() == null) {
                return SpawnFileManager.file.getSpawn().getLocation().getWorld();//Spawn logic disallows it's world to be null
                // - the world would have to removed in the runtime in order for this to be null
            }

            return Bukkit.getWorld(UUID.fromString(data.getOriginalWorldUUID()));
        }

        if (data != null) {
            data.setOriginalWorldUUID(originalWorldUUID.toString());
        }

        Location loc = user.getOriginalLocation();

        loc.setWorld(loginWorldInstance.world);

        p.teleport(loc);
        return originalWorld;
    }

    public static void returnToOriginalWorld(UnverifiedUser user) {
        Player p = user.getPlayer();
        Location loc = user.getOriginalLocation();
        loc.setWorld(user.getOriginalWorld());
        p.teleport(loc);
    }*/
}
