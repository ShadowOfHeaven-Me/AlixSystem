package shadow.systems.executors.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.TimeStampMode;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import shadow.Main;
import shadow.systems.netty.AlixChannelHandler;
import shadow.utils.users.UserManager;
import shadow.utils.users.types.AlixUser;

public final class PacketEventsManager {

    public static void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(Main.plugin));
        PacketEvents.getAPI().getSettings().timeStampMode(TimeStampMode.NONE).bStats(true).debug(false).reEncodeByDefault(false).downsampleColors(false);
        PacketEvents.getAPI().load();
    }

    public static void onEnable() {
        //https://github.com/Tofaa2/EntityLib
/*        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(Main.plugin);
        APIConfig settings = new APIConfig(PacketEvents.getAPI())
                .debugMode()
                .usePlatformLogger();
        EntityLib.init(platform, settings);*/


        PacketEvents.getAPI().getEventManager().registerListeners(new GeneralListener());//, new LoginInStartListener());
        PacketEvents.getAPI().init();
        //Main.logInfo("Initialized additional functionality thanks to PacketEvents: Command Hiding");
    }

    private static final class GeneralListener extends PacketListenerAbstract {

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            //if (event.getUser().getUUID() == null) return;
            AlixUser user;/* = UserManager.get(event.getUser().getUUID());
            if (user instanceof TemporaryUser) {
                ((TemporaryUser) user).getTempProcessor().onPacketReceive(event);
            }*/

            //Main.logInfo("PACKET IN: " + event.getPacketType().getName());// + " NAMES " + ((Channel) event.getUser().getChannel()).pipeline().names());

            if (event.getPacketType() == PacketType.Login.Client.LOGIN_START) {
                AlixChannelHandler.onLoginStart(event);
                return;
            }

            if (event.getClass() == PacketPlayReceiveEvent.class && (user = UserManager.get(event.getUser().getUUID())) != null)
                user.getPacketProcessor().onPacketReceive((PacketPlayReceiveEvent) event);//Main.logInfo("IN NONNULL: " + event.getPacketType().getName());

            /*if (event.getPacketType() == PacketType.Play.Client.CHAT_PREVIEW || event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE || event.getPacketType() == PacketType.Play.Client.CHAT_ACK) {
                Main.logInfo("PACKET IN: " + event.getPacketType().getName() + " CANCELLED: " + event.isCancelled());
            }*/
            /*if(event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
                Main.logError("MSG CLIENT: " + new WrapperPlayClientPluginMessage(event).getChannelName() + " " + new String(new WrapperPlayClientPluginMessage(event).getData()));
            }*/
        }

        @Override
        public void onPacketSend(PacketSendEvent event) {
            //if (event.getUser().getUUID() == null) return;
            AlixUser user;/* = UserManager.get(event.getUser().getUUID());
            if (user instanceof TemporaryUser) {
                ((TemporaryUser) user).getTempProcessor().onPacketSend(event);
            }*/


/*            if (event.getPacketType() == PacketType.Play.Server.PLAYER_ABILITIES) {
                Main.logError("ABILITIIIESSS SENNDD " + AlixUtils.getFields(new WrapperPlayServerPlayerAbilities(event)));
            }*/

            //Main.logInfo("PACKET OUT: " + event.getPacketType().getName());// + " NAMES " + ((Channel) event.getUser().getChannel()).pipeline().names());
            //Bukkit.broadcastMessage("PACKET OUT: " + event.getPacketType().getName());

            if (event.getClass() == PacketPlaySendEvent.class && (user = UserManager.get(event.getUser().getUUID())) != null)
                user.getPacketProcessor().onPacketSend((PacketPlaySendEvent) event);//Main.logInfo("OUT NONNULL: " + event.getPacketType().getName());
            /*else if (event.getPacketType() == PacketType.Status.Server.RESPONSE) {
                WrapperStatusServerResponse wrapper = new WrapperStatusServerResponse(event);
                JsonObject obj = wrapper.getComponent();
                JsonObject playersObj = obj.get("players").getAsJsonObject();
                playersObj.addProperty("online", UserManager.userCount());
                wrapper.setComponent(obj);
                event.markForReEncode(true);
            }*/
            /*if(event.getPacketType() == PacketType.Play.Server.PLUGIN_MESSAGE) {
                Main.logError("MSG: " + new WrapperPlayServerPluginMessage(event).getChannelName() + " " + new String(new WrapperPlayServerPluginMessage(event).getData()));
            }*/
            /*if(event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS) {
                event.getTasksAfterSend().add(() -> UserVirtualization.spoofPackets(event.getUser()));
            }
            if(event.getPacketType() == BUNDLE) {
                event.setCancelled(true);
            }*/
            /*if (event.getPacketType() == JOIN_GAME) {
                WrapperPlayServerJoinGame c = new WrapperPlayServerJoinGame(event);
                NettyUtils.getSilentContext((Channel) event.getUser().getChannel()).writeAndFlush(NettyUtils.createBuffer(c));
                event.setCancelled(true);
                *//*Main.logInfo("OUT JOIN GAME: " + AlixUtils.getFields(c));
                Main.logInfo("OUT JOIN GAME TAGS: " + c.getDimensionCodec().getTags());
                for (Map.Entry<String, NBT> e : c.getDimensionCodec().getTags().entrySet())
                    Main.logInfo("OUT JOIN GAME TAGS LISTED: KEY: " + e.getKey() + " VALUE: " + NBTCodec.nbtToJson(e.getValue(), false));
                Main.logInfo("OUT JOIN GAME DIMENSION ATTRIBUTES " + NBTCodec.nbtToJson(c.getDimension().getAttributes(), false));*//*
            }*/
            /*if (event.getPacketType() == CHUNK_DATA) {
                Column c = new WrapperPlayServerChunkData(event).getColumn();
                Main.logInfo("OUT: X " + c.getX() + " Z " + c.getZ() + " HAS BIOME DATA " + c.hasBiomeData() + " IS FULL " + c.isFullChunk()
                        + " TILE ENTITIES LENGTH " + c.getTileEntities().length + " CHUNKS LENGTH " + c.getChunks().length);
            }*/
            //if(event.getPacketType() == TAGS) Main.logInfo("OUT: " + new WrapperPlaySe`rverTags(event).getTags());
        }

//        @Override
//        public void onUserLogin(UserLoginEvent event) {
//            super.onUserLogin(event);
//        }


/*        @Override
        public void onUserConnect(UserConnectEvent event) {
            //Main.logInfo("NAMES CONNECT: " + ((Channel) event.getUser().getChannel()).pipeline().names());
            Main.logInfo("NAMES CONNECT: " + ((Channel) event.getUser().getChannel()).pipeline().names());
            User user = event.getUser();
            Channel channel = (Channel) user.getChannel();
            //BufPreProcessor processor = (BufPreProcessor) channel.pipeline().context(BufPreProcessor.BUF_PRE_PROCESSOR_NAME).handler();
            //processor.setUser(user);
        }*/

        @Override
        public void onUserDisconnect(UserDisconnectEvent event) {
            String name = event.getUser().getName();
            if (name == null) return;
            //Main.logError("DISCONNECTED: " + event.getUser().getName());
            UserManager.removeConnecting(name);
            //AlixChannelHandler.onDisconnect(event.getUser());
            /*AlixUser user = LoginVerdictManager.getNullable(event.getUser().getUUID());
            if (user == null) UserManager.removeConnecting(event.getUser().getName());*/
            //else //if (user.isVerified()) UserSemiVirtualization.invokeQuit0(user);
        }

        private GeneralListener() {
            super(PacketListenerPriority.MONITOR);
        }
    }
/*
    private static final class LoginInStartListener extends PacketListenerAbstract {

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            //Main.logInfo("PACKET TYPE: " + event.getPacketType() + " NAMES: " + ((Channel) (event.getUser().getChannel())).pipeline().names());
            if (event.getPacketType() == PacketType.Login.Client.LOGIN_START) AlixChannelHandler.onLoginStart(event);
        }

*//*        @Override
        public void onUserConnect(UserConnectEvent event) {
            User user = event.getUser();
            Channel channel = (Channel) user.getChannel();
            BufPreProcessor.addPreProcessor(channel, user);
        }*//*

        private LoginInStartListener() {
            super(PacketListenerPriority.LOWEST);//make sure we read the LoginInStart packet first to indicate to other plugins that this packet might be invalid
        }
    }*/

    private PacketEventsManager() {
    }
/*    public static void initializeFireWall() {
        boolean fastRaw = Main.config.getBoolean("fast-raw-firewall");
        PacketEvents.getAPI().getEventManager().registerListener(fastRaw ? new FastRawFireWall() : new NormalFireWall());
        Main.logInfo("Using PacketEvents for the FireWall Protection initialization. Fast Mode: " + (fastRaw ? "ON" : "OFF"));
    }*/

/*    private static final class NormalFireWall extends PacketListenerAbstract {

        private final Object kickPacket = OutDisconnectKickPacketConstructor.constructAtLoginPhase(Messages.get("anti-bot-firewalled"));

        private NormalFireWall() {
            super(PacketListenerPriority.LOWEST);
        }

        @Override
        public final void onPacketReceive(PacketReceiveEvent event) {
            if (event.getPacketType() == PacketType.Login.Client.LOGIN_START && FireWallManager.isBlocked(event.getUser().getAddress()))
                ((Channel) event.getChannel()).writeAndFlush(kickPacket).addListener(ChannelFutureListener.CLOSE);
        }
    }*/

/*    private static final class FastRawFireWall extends PacketListenerCommon {

        private FastRawFireWall() {
            super(PacketListenerPriority.LOWEST);
        }

        @Override
        public final void onUserConnect(UserConnectEvent event) {
            if (FireWallManager.isBlocked(event.getUser().getAddress())) event.setCancelled(true);
        }
        *//*event.getUser().sendPacket(new WrapperLoginServerDisconnect(Component.text("Yeeeeeessss")));
            event.getUser().closeConnection();
            Main.logError("rftgyhjuikoiuytre");*//*
    }*/

/*    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Login.Client.LOGIN_START) {
            //Main.logInfo(event.getUser().getName() + " - " + event.getUser().getAddress());
            Channel channel = (Channel) event.getChannel();
            *//*channel.pipeline().addBefore("packet_handler","sex", new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    Main.logInfo(msg.getClass().getSimpleName());
                    super.channelRead(ctx, msg);
                }


                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    Main.logInfo(msg.getClass().getSimpleName());
                    super.write(ctx, msg, promise);
                }
            });*//*

            //AlixScheduler.runLaterAsync(() -> channel.writeAndFlush(kickPacket).addListener(ChannelFutureListener.CLOSE), 10, TimeUnit.MILLISECONDS);

            channel.writeAndFlush(kickPacket).addListener(ChannelFutureListener.CLOSE);
            Main.logInfo("SENT");
            //((Channel) event.getChannel()).
            //WrapperHandshakingClientHandshake packet = new WrapperHandshakingClientHandshake(event);
        }
    }*/



/*    if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
        UnverifiedUser user = Verifications.get(event.getUser().getUUID());
        if (user == null) return;
        if (!user.isGUIInitialized()) {//the player didn't click an alix inventory
            event.setCancelled(true);//cancel the packet
            return;//stop further processing
        }
        WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
        AlixGui.onAsyncClick(user, packet.getSlot());
        event.setCancelled(true);//cancelling the event to discourage further processing by the server
    }*/

/*    @Override
    public void onPacketSend(PacketSendEvent event) {
        if(event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE) {
            WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(event);

        }
    }*/
}