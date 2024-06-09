/*
package shadow.systems.virtualization;

import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.mojang.authlib.GameProfile;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import net.kyori.adventure.nbt.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import shadow.Main;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.main.AlixUnsafe;
import shadow.utils.world.AlixWorld;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class UserVirtualization {

    private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
    private static final Method placeNewPlayer = ReflectionUtils.getMethodByReturnTypeByFirstParams(ReflectionUtils.nms2("server.dedicated.DedicatedPlayerList"), void.class, ReflectionUtils.networkManagerClass, ReflectionUtils.entityPlayerClass);
    //private static final Object PLAYER_LIST_ORIGINAL;
    private static final Map<UUID, Runnable> MAP = new ConcurrentHashMap<>();
    private static final String virtualName = "--alix-virtual-user";
    private static final List<PacketWrapper<?>> joinPackets = new ArrayList<>();
    public static final Runnable RETURN_ORIGINAL_PLAYER_LIST;
    private static final Vector3i SPAWN_POS = SpigotConversionUtil.fromBukkitLocation(AlixWorld.TELEPORT_LOCATION).getPosition().toVector3i();

    //Thanks for the tip on ByteBuddy, zlataovce!
    static {
        try {
            Class<?> mcServerClass = ReflectionUtils.nms2("server.MinecraftServer");
            Object mcServer = mcServerClass.getMethod("getServer").invoke(null);

            Class<?> playerListClazz = ReflectionUtils.nms2("server.dedicated.DedicatedPlayerList");
            Field playerListField = ReflectionUtils.getFieldFromTypeAssignable(mcServerClass, playerListClazz.getSuperclass());//superclass call here as well

            Object playerList = playerListField.get(mcServer);
            //PLAYER_LIST_ORIGINAL = playerList;

            Main.logInfo("Started User Virtualization set-up...");

            Class<?> playerListClazzCopy =
                    new ByteBuddy().subclass(playerListClazz)
                            //.name("shadow.")
                            .method(ElementMatchers.is(placeNewPlayer)).intercept(MethodDelegation.to(PlayerListInterceptor.class))
                            .make().load(UserVirtualization.class.getClassLoader())//use our own class loader, since it won't find the class otherwise
                            .getLoaded();

            Object copyInstance = UNSAFE.allocateInstance(playerListClazzCopy);//do not invoke the constructor, since that creates the server xD

            Class<?> currentClazz = playerListClazz;


            //copy the fields, since the constructor was not invoked
            do {
                for (Field f : currentClazz.getDeclaredFields()) {
                    //Field copyField = f; //currentClazz.getDeclaredField(f.getName());
                    //override(f, copyField, playerList, copyInstance, UNSAFE);
                    if (Modifier.isStatic(f.getModifiers())) continue;
                    f.setAccessible(true);
                    f.set(copyInstance, f.get(playerList));
                    //Main.logError("FIELD NAME: " + f.getName() + " TYPE: " + f.getType() + " COPY: " + f.get(copyInstance));
                }
            } while ((currentClazz = currentClazz.getSuperclass()) != Object.class);

            Class<?> packetSendListenerClazz = ReflectionUtils.nms2("network.PacketSendListener");


            Class<?> networkManagerClazzCopy =
                    new ByteBuddy().subclass(ReflectionUtils.networkManagerClass)
                            //.name("shadow.")
                            .method(ElementMatchers.takesArguments(ReflectionUtils.packetClazz, packetSendListenerClazz))
                            .intercept(MethodDelegation.to(NetworkInterceptor.class))
                            .make().load(UserVirtualization.class.getClassLoader())//use our own class loader, since it won't find the class otherwise
                            .getLoaded();

            Object virtualNetworkManager = networkManagerClazzCopy.getConstructors()[0].newInstance(ReflectionUtils.clientboundProtocolDirection);

            */
/*Field craftServerField = ReflectionUtils.getFieldFromTypeAssignable(mcServerClass, Server.class);
            craftServerField.setAccessible(true);
            Bukkit.getServer()*//*



            World world = Bukkit.getServer().getWorld(AlixWorld.worldName);
            Object serverLevel = world.getClass().getMethod("getHandle").invoke(world);//nms version of World

            Object virtualEntityPlayer = ReflectionUtils.entityPlayerClass.getConstructors()[0].newInstance(mcServer, serverLevel, new GameProfile(new UUID(2137, 420), virtualName));

            //Server server = Bukkit.getServer();

            */
/*Class<?> pluginManagerClazzCopy =
                    new ByteBuddy().rebase(SimplePluginManager.class)
                            //.name("shadow.me.Yes")
                            .method(ElementMatchers.named("callEvent").and(ElementMatchers.takesArguments(Event.class)))
                            .intercept(MethodDelegation.to(EventInterceptor.class))
                            .make().load(UserVirtualization.class.getClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST)//use our own class loader, since it won't find the class otherwise
                            .getLoaded();



            Field pmField = server.getClass().getDeclaredField("pluginManager");

            pmField.setAccessible(true);
            PluginManager originalPM = (PluginManager) pmField.get(server);

            Main.logError("pluginManagerClazzCopy: " + pluginManagerClazzCopy + " loader: " + pluginManagerClazzCopy.getClassLoader() + " originalPM: " + originalPM.getClass() + " loader: " + originalPM.getClass().getClassLoader());

            pmField.set(server, pluginManagerClazzCopy.getConstructors()[0].newInstance(server, ReflectionUtils.commandMap));*//*
//new PluginManagerInterceptor(originalPM)

            //HandlerList.getHandlerLists();

            //Make sure no events are invoked by the virtual user
            Field f1 = PlayerJoinEvent.class.getDeclaredField("handlers");
            f1.setAccessible(true);
            HandlerList original1 = (HandlerList) f1.get(null);
            UNSAFE.putObject(UNSAFE.staticFieldBase(f1), UNSAFE.staticFieldOffset(f1), new EmptyStoringHandlerList(original1));

            Field f2 = PlayerSpawnLocationEvent.class.getDeclaredField("handlers");
            f2.setAccessible(true);
            HandlerList original2 = (HandlerList) f2.get(null);
            UNSAFE.putObject(UNSAFE.staticFieldBase(f2), UNSAFE.staticFieldOffset(f2), new EmptyStoringHandlerList(original2));

            Field f3 = PlayerQuitEvent.class.getDeclaredField("handlers");
            f3.setAccessible(true);
            HandlerList original3 = (HandlerList) f3.get(null);
            UNSAFE.putObject(UNSAFE.staticFieldBase(f3), UNSAFE.staticFieldOffset(f3), new EmptyStoringHandlerList(original3));

            Class<?> playerConnectionClazz = ReflectionUtils.nms2("server.network.PlayerConnection");
            Object virtualPlayerConnection = playerConnectionClazz.getConstructors()[0].newInstance(mcServer, virtualNetworkManager, virtualEntityPlayer);

            ReflectionUtils.getFieldFromTypeDirect(ReflectionUtils.entityPlayerClass, playerConnectionClazz).set(virtualEntityPlayer, virtualPlayerConnection);

            //invoke add to let NetworkInterceptor collect the packets
            placeNewPlayer.invoke(playerList, virtualNetworkManager, virtualEntityPlayer);

*/
/*            for (Field f : playerListClazzCopy.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == Map.class) ((Map) f.get(copyInstance)).clear();
                if (f.getType() == List.class) ((List) f.get(copyInstance)).clear();
            }*//*

            //invoke remove to not introduce any invalid data
            playerListClazz.getMethod("remove", ReflectionUtils.entityPlayerClass).invoke(playerList, virtualEntityPlayer);


            UNSAFE.putObject(UNSAFE.staticFieldBase(f1), UNSAFE.staticFieldOffset(f1), original1);
            UNSAFE.putObject(UNSAFE.staticFieldBase(f2), UNSAFE.staticFieldOffset(f2), original2);
            UNSAFE.putObject(UNSAFE.staticFieldBase(f3), UNSAFE.staticFieldOffset(f3), original3);
            f1.setAccessible(false);
            f2.setAccessible(false);
            f3.setAccessible(false);
            //pmField.set(server, originalPM);

            Main.logInfo("Finished User Virtualization set-up.");
            //Object playerListProxy = Proxy.newProxyInstance(AlixEventInterceptor.class.getClassLoader(), new Class[]{playerListClazz}, INTERCEPTOR);
            //playerListField.set(mcServer, playerListProxy);

            playerListField.set(mcServer, copyInstance);//it's important to set it at the very end, since we use the original instance above
            RETURN_ORIGINAL_PLAYER_LIST = () -> {
                try {
                    playerListField.set(mcServer, playerList);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            };
        } catch (Exception e) {
            throw new AlixException(e);
        }
    }

    private static final class EmptyStoringHandlerList extends HandlerList {

        private final HandlerList original;

        private EmptyStoringHandlerList(HandlerList original) {
            this.original = original;
        }

        @Override
        public synchronized void register(@NotNull RegisteredListener listener) {
            this.original.register(listener);
        }

        @Override
        public void registerAll(@NotNull Collection<RegisteredListener> listeners) {
            this.original.registerAll(listeners);
        }

        @Override
        public synchronized void unregister(@NotNull RegisteredListener listener) {
            this.original.unregister(listener);
        }

        @Override
        public synchronized void unregister(@NotNull Plugin plugin) {
            this.original.unregister(plugin);
        }

        @Override
        public synchronized void unregister(@NotNull Listener listener) {
            this.original.unregister(listener);
        }

        @NotNull
        @Override
        public RegisteredListener[] getRegisteredListeners() {
            return new RegisteredListener[0];
            // \/ Does not account for PaperPluginManager
            */
/*if (Reflection.getCallerClass() == SimplePluginManager.class) return new RegisteredListener[0];
            return this.original.getRegisteredListeners();*//*

        }

        @Override
        public synchronized void bake() {
            this.original.bake();
        }
    }

*/
/*    public static final class EventInterceptor {//must be public

        @RuntimeType
        public static void intercept(@Argument(0) Event event) throws Exception {
            Main.logInfo("INVOKED EVENT METHOD " + event.getClass().getSimpleName());// + " " + (superMethod == addPlayer));// + " with " + Arrays.toString(args) + " at: " + Arrays.toString(Thread.currentThread().getStackTrace()));
            if (event instanceof PlayerEvent) {
                PlayerEvent e = (PlayerEvent) event;
                if (e.getPlayer().getName().equals(virtualName))
                    return;//do not let the virtual user invoke any PlayerEvents
            }
            //call.run();
        }
    }*//*


    public static final class NetworkInterceptor {//must be public


        @RuntimeType
        public static void intercept(@AllArguments Object[] args, @Origin Method method, @SuperCall Runnable call) throws Exception {
            // + " " + (superMethod == addPlayer));// + " with " + Arrays.toString(args) + " at: " + Arrays.toString(Thread.currentThread().getStackTrace()));
            //call.run();
            //PacketReceiveEvent packetReceiveEvent = EventCreationUtil.createReceiveEvent(channel, user, player, buffer, autoProtocolTranslation);
            Object packet = args[0];
            //int packetID = PacketTransformation.getPacketId(packet);

            //Main.logInfo("INVOKED METHOD WITH " + args[0].getClass().getSimpleName());
            int packetID = PacketTransformation.getPacketId(packet);

            ByteBuf bufferPacketContents = PacketTransformation.getContentsOf(packet);//input the minecraft packet

            //ByteBufHelper.writeVarInt(bufferPacketContents, packetID);

            //We send, so PacketSide = SERVER
            //The connection state is PLAY because it's sent after "Finish configuration", or if configuration phase isn't a thing, after the login phase (So after Login Success):
            //https://wiki.vg/Protocol_FAQ#.E2.80.A6my_player_isn.27t_spawning.21
           */
/* PacketType.Play.Server type = (PacketType.Play.Server) PacketType.getById(PacketSide.SERVER, ConnectionState.PLAY, PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), packetID);
            Main.logInfo("INVOKED METHOD WITH " + args[0].getClass().getSimpleName() + " PE: " + type);
            *//*
*/
/*User user = new User(null, ConnectionState.PLAY, PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), new UserProfile(new UUID(2137, 420), virtualName));
            PacketPlayReceiveEvent event = new PacketPlayReceiveEvent(PacketSide.SERVER, null, null, )*//*
*/
/*
            switch (type) {
                case JOIN_GAME:
                    WrapperPlayServerJoinGame game = PacketTransformation.writeWrapperContents(WrapperPlayServerJoinGame.class, bufferPacketContents, type, packetID);
                    *//*
*/
/*Main.logError("JOIN GAME: " + AlixUtils.getFields(game));
                    Main.logInfo("OUT JOIN GAME TAGS: " + game.getDimensionCodec().getTags());
                    for (Map.Entry<String, NBT> e : game.getDimensionCodec().getTags().entrySet())
                        Main.logInfo("OUT JOIN GAME TAGS LISTED: KEY: " + e.getKey() + " VALUE: " + NBTCodec.nbtToJson(e.getValue(), false));
                    Main.logInfo("OUT JOIN GAME DIMENSION ATTRIBUTES " + NBTCodec.nbtToJson(game.getDimension().getAttributes(), false));
                    *//*
*/
/*joinPackets.add(game);
                    break;
*//*
*/
/*                case DECLARE_COMMANDS:
                    WrapperPlayServerDeclareCommands cmds = PacketTransformation.writeWrapperContents(WrapperPlayServerDeclareCommands.class, bufferPacketContents, type, packetID);
                    joinPackets.add(cmds);
                    break;*//*
*/
/*
*//*
*/
/*                case PLAYER_POSITION_AND_LOOK:
                    WrapperPlayServerPlayerPositionAndLook pos = PacketTransformation.writeWrapperContents(WrapperPlayServerPlayerPositionAndLook.class, bufferPacketContents, type, packetID);
                    Main.logInfo("POST AND LOOK: " + AlixUtils.getFields(pos));
                    joinPackets.add(pos);
                    break;*//*
*/
/*
            }*//*

        }
    }

    public static final class PlayerListInterceptor {//must be public

        @RuntimeType
        public static void intercept(@AllArguments Object[] args, @Origin Method method, @SuperCall Runnable call) throws Exception {
            Main.logInfo("INVOKED METHOD");// + " " + (superMethod == addPlayer));// + " with " + Arrays.toString(args) + " at: " + Arrays.toString(Thread.currentThread().getStackTrace()));
            Object entityPlayer = args[1];
            Player craftPlayer = (Player) ReflectionUtils.getBukkitEntity_CraftPlayer.invoke(entityPlayer);
            MAP.put(craftPlayer.getUniqueId(), call);

            //spoofPackets(craftPlayer);
            //call.run();
        }
    }

    public static void spoofPackets(User retrooperUser) {
        //Channel channel = (Channel) retrooperUser.getChannel();
        //for(PacketWrapper<?> o : joinPackets) retrooperUser.sendPacket(o);
        */
/*retrooperUser.sendPacket(new WrapperPlayClientPluginMessage("minecraft:brand", "Spigot".getBytes()));
        retrooperUser.sendPacket(new WrapperPlayServerSpawnPosition(SPAWN_POS, 0));
        retrooperUser.sendPacket(new WrapperPlayServerUpdateViewPosition(0, 0));*//*

        */
/*ChannelHandlerContext ctx = NettyUtils.getSilentContext((Channel) retrooperUser.getChannel());
        for (ByteBuf packet : joinPackets) ctx.writeAndFlush(packet);
        for (WrapperPlayServerChunkData chunk : wddfwdwdw(12)) {
            //Main.logInfo("SPOOFING " + chunk.getClass().getSimpleName());
            ctx.writeAndFlush(NettyUtils.createBuffer(chunk));
        }*//*

    }

    //From:
    //https://wiki.vg/Protocol_FAQ
    //https://wiki.vg/Registry_Data
    //https://github.com/Elytrium/LimboAPI/blob/master/plugin/src/main/java/net/elytrium/limboapi/server/LimboImpl.java#L771
    */
/*public static void spoofPackets(Player player) throws Exception {
        Vector3i spawnPos = SpigotConversionUtil.fromBukkitLocation(AlixWorld.TELEPORT_LOCATION).getPosition().toVector3i();
        //WrapperLoginServerLoginSuccess login = new WrapperLoginServerLoginSuccess(player.getUniqueId(), player.getName());
        *//*
*/
/*List<String> worlds = new ArrayList<>();
        Bukkit.getWorlds().forEach(world -> worlds.add("minecraft:" + world.getName()));

        ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();

        CompoundBinaryTag.Builder registryContainer = CompoundBinaryTag.builder();
        ListBinaryTag encodedDimensionRegistry = ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(DimensionMapping.createDimensionData(DimensionMapping.OVERWORLD, version)).add(DimensionMapping.createDimensionData(DimensionMapping.NETHER, version)).add(DimensionMapping.createDimensionData(DimensionMapping.THE_END, version)).build();

        if (version.compareTo(ServerVersion.V_1_16_2) >= 0) {
            CompoundBinaryTag.Builder dimensionRegistryEntry = CompoundBinaryTag.builder();
            dimensionRegistryEntry.putString("type", "minecraft:dimension_type");
            dimensionRegistryEntry.put("value", encodedDimensionRegistry);
            registryContainer.put("minecraft:dimension_type", dimensionRegistryEntry.build());
            registryContainer.put("minecraft:worldgen/biome", BiomeMapping.getRegistry(version));
            if (version.compareTo(ServerVersion.V_1_19) == 0) {
                registryContainer.put("minecraft:chat_type", CHAT_TYPE_119);
            } else if (version.compareTo(ServerVersion.V_1_19_1) >= 0) {
                registryContainer.put("minecraft:chat_type", CHAT_TYPE_1191);
            }

            if (version.compareTo(ServerVersion.V_1_19_4) == 0) {
                registryContainer.put("minecraft:damage_type", DAMAGE_TYPE_1194);
            } else if (version.compareTo(ServerVersion.V_1_20) >= 0) {
                registryContainer.put("minecraft:damage_type", DAMAGE_TYPE_120);
            }
        } else registryContainer.put("dimension", encodedDimensionRegistry);


        CompoundBinaryTag currentDimensionData = encodedDimensionRegistry.getCompound(DimensionType.OVERWORLD.getId());
        if (version.compareTo(ServerVersion.V_1_16_2) >= 0) {
            currentDimensionData = currentDimensionData.getCompound("element");
        }

        NBTCompound dimensionNbt = transformToNBTCompound(registryContainer.build()); //transformToNBTCompound(REGISTRY_DATA); //(NBTCompound) NBTCodec.jsonToNBT(LoginMappingHolder.V20_2.get("dimensionCodec"));
        //JSONComponentSerializer.json().

        WrapperPlayServerJoinGame joinGame =
                new WrapperPlayServerJoinGame(player.getEntityId(), false, GameMode.ADVENTURE, GameMode.ADVENTURE,
                        worlds, dimensionNbt, new Dimension(transformToNBTCompound(currentDimensionData)), Difficulty.PEACEFUL,"minecraft:world",
                        ThreadLocalRandom.current().nextLong(), Bukkit.getMaxPlayers(), 2, 2, false, false, true,
                        true, new WorldBlockPosition(new Dimension(transformToNBTCompound(currentDimensionData)), spawnPos), 0);

        //Main.logError("REGISTRY DATA: " + NBTCodec.nbtToJson(transformToNBTCompound(REGISTRY_DATA), false));

        Main.logInfo("OUT JOIN GAME: " + AlixUtils.getFields(joinGame));
        Main.logInfo("OUT JOIN GAME TAGS: " + joinGame.getDimensionCodec().getTags());
        for (Map.Entry<String, NBT> e : joinGame.getDimensionCodec().getTags().entrySet())
            Main.logInfo("OUT JOIN GAME TAGS LISTED: KEY: " + e.getKey() + " VALUE: " + NBTCodec.nbtToJson(e.getValue(), false));
        Main.logInfo("OUT JOIN GAME DIMENSION ATTRIBUTES " + NBTCodec.nbtToJson(joinGame.getDimension().getAttributes(), false));*//*
*/
/*

        User user = LoginVerdictManager.getExistingTempUser(player).reetrooperUser();
        *//*
*/
/*for (NBT nbt : joinGame.getDimensionCodec().getTags().values()) {
            Main.logInfo("OUT JOIN GAME: " + ((NBTCompound) nbt).getTagNames());
        }*//*
*/
/*
        user.sendPacketSilently(new WrapperPlayServerSpawnPosition(spawnPos, 0));

        //WrapperPlayServerTags tags = new WrapperPlayServerTags();
        WrapperPlayServerChunkData[] chunks = wddfwdwdw(3);

        //ChannelHandlerContext ctx = NettyUtils.getSilentContext((Channel) LoginVerdictManager.getExistingTempUser(player).reetrooperUser().getChannel());
        //ctx.writeAndFlush(NettyUtils.dynamic(login, ctx));

        //user.sendPacketSilently(joinGame);
        //user.sendPacketSilently(new WrapperPlayServerRespawn());

        for (WrapperPlayServerChunkData chunk : chunks) {
            //Main.logInfo("SPOOFING " + chunk.getClass().getSimpleName());
            user.sendPacketSilently(chunk);
        }
        user.sendPacketSilently(new WrapperPlayServerInitializeWorldBorder(600000, 600000, 600000, 600000, 0, 0, 100, 0));
        //ctx.writeAndFlush(NettyUtils.dynamic(new WrapperPlayServerUpdateEnabledFeatures(, ctx));

        //ctx.writeAndFlush(NettyUtils.dynamic(new WrapperPlayServerSpawnPosition(spawnPos, 0), ctx));
        //ctx.writeAndFlush(new WrapperConfigServerRegistryData(transformToNBTCompound(REGISTRY_DATA)));
        //ctx.writeAndFlush(NettyUtils.dynamic(new WrapperConfigServerConfigurationEnd(), ctx));
        //ctx.writeAndFlush(NettyUtils.dynamic(new WrapperPlayServerChangeGameState(WrapperPlayServerChangeGameState.Reason.START_LOADING_CHUNKS, 0), ctx));
        user.sendPacketSilently(new WrapperPlayServerUpdateViewPosition(0, 0));
        user.sendPacketSilently(new WrapperPlayServerSpawnPosition(spawnPos, 0));
        //user.sendPacketSilently(new WrapperPlayServerPlayerPositionAndLook(0, 0, 0, 0, 0, (byte) 0, 1, false));
    }*//*



    public static NBTCompound transformToNBTCompound(CompoundBinaryTag kyori) {
        NBTCompound p = new NBTCompound();
        for (String key : kyori.keySet()) {
            BinaryTag tag = kyori.get(key);
            p.setTag(key, fromKyori(tag));
        }
        return p;
    }

    private static NBTList<NBT> transformList(ListBinaryTag list) {
        NBTList<NBT> r = new NBTList<>(transformType(list.elementType()));
        for (int i = 0; i < list.size(); i++) r.addTag(fromKyori(list.get(i)));
        return r;
    }

    private static NBTType transformType(BinaryTagType type) {
        if (type == BinaryTagTypes.BYTE) return NBTType.BYTE;
        else if (type == BinaryTagTypes.SHORT) return NBTType.SHORT;
        else if (type == BinaryTagTypes.INT) return NBTType.INT;
        else if (type == BinaryTagTypes.LONG) return NBTType.LONG;
        else if (type == BinaryTagTypes.FLOAT) return NBTType.FLOAT;
        else if (type == BinaryTagTypes.DOUBLE) return NBTType.DOUBLE;
        else if (type == BinaryTagTypes.BYTE_ARRAY) return NBTType.BYTE_ARRAY;
        else if (type == BinaryTagTypes.INT_ARRAY) return NBTType.INT_ARRAY;
        else if (type == BinaryTagTypes.LONG_ARRAY) return NBTType.LONG_ARRAY;
        else if (type == BinaryTagTypes.STRING) return NBTType.STRING;
        else if (type == BinaryTagTypes.END) return NBTType.END;
        else if (type == BinaryTagTypes.COMPOUND) return NBTType.COMPOUND;
        else if (type == BinaryTagTypes.LIST) return NBTType.LIST;
        throw new AlixException("Invalid: " + type);
    }

    private static NBT fromKyori(BinaryTag tag) {
        BinaryTagType type = tag.type();
        if (type == BinaryTagTypes.BYTE) return new NBTByte(((ByteBinaryTag) tag).value());
        else if (type == BinaryTagTypes.SHORT) return new NBTShort(((ShortBinaryTag) tag).value());
        else if (type == BinaryTagTypes.INT) return new NBTInt(((IntBinaryTag) tag).value());
        else if (type == BinaryTagTypes.LONG) return new NBTLong(((LongBinaryTag) tag).value());
        else if (type == BinaryTagTypes.FLOAT) return new NBTFloat(((FloatBinaryTag) tag).value());
        else if (type == BinaryTagTypes.DOUBLE) return new NBTDouble(((DoubleBinaryTag) tag).value());
        else if (type == BinaryTagTypes.BYTE_ARRAY) return new NBTByteArray(((ByteArrayBinaryTag) tag).value());
        else if (type == BinaryTagTypes.INT_ARRAY) return new NBTIntArray(((IntArrayBinaryTag) tag).value());
        else if (type == BinaryTagTypes.LONG_ARRAY) return new NBTLongArray(((LongArrayBinaryTag) tag).value());
        else if (type == BinaryTagTypes.STRING) return new NBTString(((StringBinaryTag) tag).value());
        else if (type == BinaryTagTypes.END) return NBTEnd.INSTANCE;
        else if (type == BinaryTagTypes.COMPOUND) return transformToNBTCompound(((CompoundBinaryTag) tag));
        else if (type == BinaryTagTypes.LIST) return transformList((ListBinaryTag) tag);
        throw new AlixException("Invalid: " + type);
    }

    private static WrapperPlayServerChunkData[] wddfwdwdw(int renderDist) {
        try {
            List<WrapperPlayServerChunkData> list = new ArrayList<>();
            for (int x = -renderDist - 1; x <= renderDist + 1; x++) {
                for (int z = -renderDist - 1; z <= renderDist + 1; z++) {
                    BaseChunk[] bc = new BaseChunk[24];
                    for (int i = 0; i < bc.length; i++) bc[i] = BaseChunk.create();
                    WrapperPlayServerChunkData chunk = new WrapperPlayServerChunkData(new Column(x, z, true, bc, new TileEntity[0]));
                    Class<?> c = chunk.getClass();

                    Field f;
                    f = c.getDeclaredField("blockLightMask");
                    f.setAccessible(true);
                    f.set(chunk, new BitSet());

                    f = c.getDeclaredField("skyLightMask");
                    f.setAccessible(true);
                    f.set(chunk, new BitSet());

                    f = c.getDeclaredField("emptyBlockLightMask");
                    f.setAccessible(true);
                    f.set(chunk, new BitSet());

                    f = c.getDeclaredField("emptySkyLightMask");
                    f.setAccessible(true);
                    f.set(chunk, new BitSet());

                    list.add(chunk);
                }
            }
            return list.toArray(new WrapperPlayServerChunkData[0]);
        } catch (Exception e) {
            throw new AlixException(e);
        }
    }

    public static void invokeListAdd(UUID uuid) {
        try {
            MAP.remove(uuid).run();
            //placeNewPlayer.invoke(PLAYER_LIST_ORIGINAL, MAP.remove(uuid));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void remove(UUID uuid) {
        MAP.remove(uuid);
    }

    public static void init() {
    }

    private static final class PluginManagerInterceptor implements PluginManager {

        private final PluginManager original;

        private PluginManagerInterceptor(PluginManager original) {
            this.original = original;
        }

        //only change this method, and delegate the rest to the original
        @Override
        public void callEvent(@NotNull Event event) throws IllegalStateException {
            Main.logInfo("INVOKED EVENT METHOD " + event.getClass().getSimpleName());// + " " + (superMethod == addPlayer));// + " with " + Arrays.toString(args) + " at: " + Arrays.toString(Thread.currentThread().getStackTrace()));
            if (event instanceof PlayerEvent) {
                PlayerEvent e = (PlayerEvent) event;
                if (e.getPlayer().getName().equals(virtualName))
                    return;//do not let the virtual user invoke any PlayerEvents
            }
            this.original.callEvent(event);
        }

        @Override
        public void registerInterface(@NotNull Class<? extends PluginLoader> aClass) throws IllegalArgumentException {
            this.original.registerInterface(aClass);
        }

        @Nullable
        @Override
        public Plugin getPlugin(@NotNull String s) {
            return this.original.getPlugin(s);
        }

        @NotNull
        @Override
        public Plugin[] getPlugins() {
            return this.original.getPlugins();
        }

        @Override
        public boolean isPluginEnabled(@NotNull String s) {
            return this.original.isPluginEnabled(s);
        }

        @Override
        public boolean isPluginEnabled(@Nullable Plugin plugin) {
            return this.original.isPluginEnabled(plugin);
        }

        @Nullable
        @Override
        public Plugin loadPlugin(@NotNull File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
            return this.original.loadPlugin(file);
        }

        @NotNull
        @Override
        public Plugin[] loadPlugins(@NotNull File file) {
            return this.original.loadPlugins(file);
        }

        @Override
        public void disablePlugins() {
            this.original.disablePlugins();
        }

        @Override
        public void clearPlugins() {
            this.original.clearPlugins();
        }

        @Override
        public void registerEvents(@NotNull Listener listener, @NotNull Plugin plugin) {
            this.original.registerEvents(listener, plugin);
        }

        @Override
        public void registerEvent(@NotNull Class<? extends Event> aClass, @NotNull Listener listener, @NotNull EventPriority eventPriority, @NotNull EventExecutor eventExecutor, @NotNull Plugin plugin) {
            this.original.registerEvent(aClass, listener, eventPriority, eventExecutor, plugin);
        }

        @Override
        public void registerEvent(@NotNull Class<? extends Event> aClass, @NotNull Listener listener, @NotNull EventPriority eventPriority, @NotNull EventExecutor eventExecutor, @NotNull Plugin plugin, boolean b) {
            this.original.registerEvent(aClass, listener, eventPriority, eventExecutor, plugin, b);
        }

        @Override
        public void enablePlugin(@NotNull Plugin plugin) {
            this.original.enablePlugin(plugin);
        }

        @Override
        public void disablePlugin(@NotNull Plugin plugin) {
            this.original.disablePlugin(plugin);
        }

        @Nullable
        @Override
        public Permission getPermission(@NotNull String s) {
            return this.original.getPermission(s);
        }

        @Override
        public void addPermission(@NotNull Permission permission) {
            this.original.addPermission(permission);
        }

        @Override
        public void removePermission(@NotNull Permission permission) {
            this.original.removePermission(permission);
        }

        @Override
        public void removePermission(@NotNull String s) {
            this.original.removePermission(s);
        }

        @NotNull
        @Override
        public Set<Permission> getDefaultPermissions(boolean b) {
            return this.original.getDefaultPermissions(b);
        }

        @Override
        public void recalculatePermissionDefaults(@NotNull Permission permission) {
            this.original.recalculatePermissionDefaults(permission);
        }

        @Override
        public void subscribeToPermission(@NotNull String s, @NotNull Permissible permissible) {
            this.original.subscribeToPermission(s, permissible);
        }

        @Override
        public void unsubscribeFromPermission(@NotNull String s, @NotNull Permissible permissible) {
            this.original.unsubscribeFromPermission(s, permissible);
        }

        @NotNull
        @Override
        public Set<Permissible> getPermissionSubscriptions(@NotNull String s) {
            return this.original.getPermissionSubscriptions(s);
        }

        @Override
        public void subscribeToDefaultPerms(boolean b, @NotNull Permissible permissible) {
            this.original.subscribeToDefaultPerms(b, permissible);
        }

        @Override
        public void unsubscribeFromDefaultPerms(boolean b, @NotNull Permissible permissible) {
            this.original.unsubscribeFromDefaultPerms(b, permissible);
        }

        @NotNull
        @Override
        public Set<Permissible> getDefaultPermSubscriptions(boolean b) {
            return this.original.getDefaultPermSubscriptions(b);
        }

        @NotNull
        @Override
        public Set<Permission> getPermissions() {
            return this.original.getPermissions();
        }

        @Override
        public boolean useTimings() {
            return this.original.useTimings();
        }
    }

*/
/*    private static void override(Field originalField, Field copyField, Object originalInstance, Object copyInstance, Unsafe unsafe) throws IllegalAccessException {
        boolean ss = Modifier.isStatic(originalField.getModifiers());
        originalField.setAccessible(true);

        //Main.logError("NAME: " + copyField.getName() + " ORIGIN: " + (ss ? unsafe.staticFieldOffset(originalField) : unsafe.objectFieldOffset(originalField)) + " COPY: " + (ss ? unsafe.staticFieldOffset(copyField) : unsafe.objectFieldOffset(copyField)));

        Object originalFieldVal = originalField.get(originalInstance);
        long offset = ss ? unsafe.staticFieldOffset(copyField) : unsafe.objectFieldOffset(copyField);
        Object base = ss ? unsafe.staticFieldBase(copyField) : copyInstance;

        Class<?> c = originalFieldVal.getClass();

        if (c == byte.class) unsafe.putByte(base, offset, (byte) originalFieldVal);
        else if (c == short.class) unsafe.putShort(base, offset, (short) originalFieldVal);
        else if (c == int.class) unsafe.putInt(base, offset, (int) originalFieldVal);
        else if (c == long.class) unsafe.putLong(base, offset, (long) originalFieldVal);
        else if (c == float.class) unsafe.putFloat(base, offset, (float) originalFieldVal);
        else if (c == double.class) unsafe.putDouble(base, offset, (double) originalFieldVal);
        else if (c == char.class) unsafe.putChar(base, offset, (char) originalFieldVal);
        else if (c == boolean.class) unsafe.putBoolean(base, offset, (boolean) originalFieldVal);
        else unsafe.putObject(base, offset, originalFieldVal);
    }*//*


    */
/*String playerListClazzFullName = playerListClazz.getPackage().getName() + "." + playerListClazz.getSimpleName();
            byte[] targetClassBytes = readAllBytes(playerListClazz.getResourceAsStream(playerListClazz.getSimpleName() + ".class"));

            ClassReader classReader = new ClassReader(targetClassBytes);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            MyClassVisitor classVisitor = new MyClassVisitor(Opcodes.ASM7, classWriter);
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);


            byte[] modifiedClassBytes = classWriter.toByteArray();


            ClassLoader classLoader = new ClassLoader(playerListClazz.getClassLoader()) {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    Main.logError("NAME MATCH: " + playerListClazzFullName.equals(name) + " 1 " + playerListClazzFullName + " 2 " + name);
                    if (playerListClazzFullName.equals(name))
                        return defineClass(name, modifiedClassBytes, 0, modifiedClassBytes.length);
                    return super.findClass(name);
                }
            };


            // Instantiate the modified class
            Class<?> modifiedClass = classLoader.loadClass(playerListClazzFullName);

            Unsafe unsafe = AlixUnsafe.getUnsafe();
            Object instance = unsafe.allocateInstance(modifiedClass);//allocate the instance with Unsafe, in order to not invoke the constructor, which could have devastating results


            for (Field originalField : playerListClazz.getDeclaredFields()) {
                Main.logInfo("PLAYERLIST");
                boolean ss = Modifier.isStatic(originalField.getModifiers());
                originalField.setAccessible(true);

                Field copyField = originalField; //instance.getClass().getDeclaredField(originalField.getName());

                //Main.logError("NAME: " + copyField.getName() + " ORIGIN: " + (ss ? unsafe.staticFieldOffset(originalField) : unsafe.objectFieldOffset(originalField)) + " COPY: " + (ss ? unsafe.staticFieldOffset(copyField) : unsafe.objectFieldOffset(copyField)));

                Object originalObj = originalField.get(playerList);
                long offset = ss ? unsafe.staticFieldOffset(copyField) : unsafe.objectFieldOffset(copyField);
                Object base = ss ? unsafe.staticFieldBase(copyField) : instance;

                Class<?> c = originalObj.getClass();
                if (c == byte.class) unsafe.putByte(base, offset, (byte) originalObj);
                else if (c == short.class) unsafe.putShort(base, offset, (short) originalObj);
                else if (c == int.class) unsafe.putInt(base, offset, (int) originalObj);
                else if (c == long.class) unsafe.putLong(base, offset, (long) originalObj);
                else if (c == float.class) unsafe.putFloat(base, offset, (float) originalObj);
                else if (c == double.class) unsafe.putDouble(base, offset, (double) originalObj);
                else if (c == char.class) unsafe.putChar(base, offset, (char) originalObj);
                else if (c == boolean.class) unsafe.putBoolean(base, offset, (boolean) originalObj);
                else unsafe.putObject(base, offset, originalObj);
            }

            for (Field originalField : playerListClazz.getSuperclass().getDeclaredFields()) {
                Main.logInfo("PLAYERLIST");
                boolean ss = Modifier.isStatic(originalField.getModifiers());
                originalField.setAccessible(true);

                Field copyField = originalField; //instance.getClass().getSuperclass().getDeclaredField(originalField.getName());

                //Main.logError("NAME: " + copyField.getName() + " ORIGIN: " + (ss ? unsafe.staticFieldOffset(originalField) : unsafe.objectFieldOffset(originalField)) + " COPY: " + (ss ? unsafe.staticFieldOffset(copyField) : unsafe.objectFieldOffset(copyField)));

                Object originalObj = originalField.get(playerList);
                long offset = ss ? unsafe.staticFieldOffset(copyField) : unsafe.objectFieldOffset(copyField);
                Object base = ss ? unsafe.staticFieldBase(copyField) : instance;

                Class<?> c = originalObj.getClass();
                if (c == byte.class) unsafe.putByte(base, offset, (byte) originalObj);
                else if (c == short.class) unsafe.putShort(base, offset, (short) originalObj);
                else if (c == int.class) unsafe.putInt(base, offset, (int) originalObj);
                else if (c == long.class) unsafe.putLong(base, offset, (long) originalObj);
                else if (c == float.class) unsafe.putFloat(base, offset, (float) originalObj);
                else if (c == double.class) unsafe.putDouble(base, offset, (double) originalObj);
                else if (c == char.class) unsafe.putChar(base, offset, (char) originalObj);
                else if (c == boolean.class) unsafe.putBoolean(base, offset, (boolean) originalObj);
                else unsafe.putObject(base, offset, originalObj);
            }*//*

    */
/*    static byte[] readAllBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) baos.write(buffer, 0, length);
            return baos.toByteArray();
        }
    }

    static class MyClassVisitor extends ClassVisitor {
        public MyClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            // Intercept the addPlayer method
            if (addPlayer.getName().equals(name)) return new MyMethodVisitor(api, mv, name);

            return mv;
        }
    }

    public static class MyMethodVisitor extends MethodVisitor {
        private final String methodName;

        public MyMethodVisitor(int api, MethodVisitor mv, String methodName) {
            super(api, mv);
            this.methodName = methodName;
        }

        @Override
        public void visitCode() {
            // Insert code at the beginning of the method
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("Before " + methodName + " invocation");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            super.visitCode();
        }

        @Override
        public void visitInsn(int opcode) {
            // Insert code at the end of the method
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitLdcInsn("After " + methodName + " invocation");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            }
            super.visitInsn(opcode);
        }
    }*//*


}*/
