package shadow.systems.executors.packetevents;

import alix.libs.com.github.retrooper.packetevents.PacketEvents;
import alix.libs.com.github.retrooper.packetevents.event.*;
import alix.libs.com.github.retrooper.packetevents.event.simple.PacketLoginReceiveEvent;
import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import alix.libs.com.github.retrooper.packetevents.protocol.chat.Node;
import alix.libs.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import alix.libs.com.github.retrooper.packetevents.util.TimeStampMode;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareCommands;
import alix.libs.io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.netty.channel.Channel;
import shadow.Main;
import shadow.systems.login.autoin.premium.PremiumAuthenticator;
import shadow.systems.netty.AlixChannelHandler;
import shadow.utils.users.UserManager;
import shadow.utils.users.types.AlixUser;

import java.util.List;

public final class PacketEventsManager {

    public static void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(Main.plugin));
        PacketEvents.getAPI().getSettings().timeStampMode(TimeStampMode.NONE).bStats(true).debug(false).reEncodeByDefault(false).downsampleColors(false).checkForUpdates(false);
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

        private static final boolean debugPackets = false;
        private final PremiumAuthenticator premiumAuthenticator = new PremiumAuthenticator();

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            //if (event.getUser().getUUID() == null) return;
            /* = UserManager.get(event.getUser().getUUID());
            if (user instanceof TemporaryUser) {
                ((TemporaryUser) user).getTempProcessor().onPacketReceive(event);
            }*/

/*            if (event.getPacketType() == PacketType.Configuration.Client.SELECT_KNOWN_PACKS) {
                Main.logError("CLIENT PACKS: " + new WrapperConfigClientSelectKnownPacks(event).getKnownPacks());
            }*/

            //Main.debug("PACKET IN: " + event.getPacketType().getName());// + " NAMES " + ((Channel) event.getUser().getChannel()).pipeline().names());

            /*if (event.getPacketType() == PacketType.Configuration.Client.CLIENT_SETTINGS) {
                Main.logError("SETTINGS: " + AlixUtils.getFields(new WrapperConfigClientSettings(event)));
            }*/

            /*if (event.getPacketType() == PacketType.Play.Client.CLIENT_SETTINGS) {
                Main.logError("SETTINGS2: " + AlixUtils.getFields(new WrapperPlayClientSettings(event)));
            }*/
/*            if (event.getPacketType() == PacketType.Play.Client.PLAYER_ABILITIES) {
                Main.logError("ABILITIES: " + AlixUtils.getFields(new WrapperPlayClientPlayerAbilities(event)));
            }*/

            //if (event.getClass() != PacketPlayReceiveEvent.class) Main.debug("PACKET IN: " + event.getPacketType().getName());
            if (debugPackets) Main.debug("PACKET IN: " + event.getPacketType().getName());
            //Main.debug("PACKET IN: " + event.getPacketType().getName() + " PIPELINE: " + ((Channel) event.getUser().getChannel()).pipeline().names());

            AlixUser user;

            if (event.getClass() == PacketPlayReceiveEvent.class && (user = UserManager.get(event.getUser().getUUID())) != null) {
                /*AlixUtils.getMethodTime(() -> {
                    AlixUser user2 = UserManager.getAttr((Channel) event.getChannel());
                });*/
                user.getPacketProcessor().onPacketReceive((PacketPlayReceiveEvent) event);//Main.logInfo("IN NONNULL: " + event.getPacketType().getName());
                return;
            }

            if (event.getPacketType() == PacketType.Handshaking.Client.HANDSHAKE) {
                AlixChannelHandler.onHandshake(event);
                return;
            }

            if (event.getClass() == PacketLoginReceiveEvent.class) {
                PacketLoginReceiveEvent e = (PacketLoginReceiveEvent) event;
                switch (e.getPacketType()) {
                    case LOGIN_START:
                    case ENCRYPTION_RESPONSE:
                        this.premiumAuthenticator.onPacketReceive(e);
                }
            }
            /*if (event.getPacketType() == PacketType.Play.Client.CHAT_PREVIEW || event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE || event.getPacketType() == PacketType.Play.Client.CHAT_ACK) {
                Main.logInfo("PACKET IN: " + event.getPacketType().getName() + " CANCELLED: " + event.isCancelled());
            }*/
            /*if(event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
                Main.logError("MSG CLIENT: " + new WrapperPlayClientPluginMessage(event).getChannelName() + " " + new String(new WrapperPlayClientPluginMessage(event).getData()));
            }*/
        }

        //private static final ByteBuf loginCommands = CommandsPacketConstructor.constructLogin();

        @Override
        public void onPacketSend(PacketSendEvent event) {
            //if (event.getUser().getUUID() == null) return;


/*            if (event.getPacketType() == PacketType.Play.Server.UPDATE_VIEW_POSITION) {
                Main.logInfo("PACKET OUT: " + event.getPacketType().getName() + " " + AlixUtils.getFields(new WrapperPlayServerUpdateViewPosition(event)));
            } else if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
                Main.logInfo("PACKET OUT: " + event.getPacketType().getName() + " " + AlixUtils.getFields(new WrapperPlayServerPlayerPositionAndLook(event)));
            } else if (event.getPacketType() == PacketType.Play.Server.SPAWN_POSITION) {
                Main.logInfo("PACKET OUT: " + event.getPacketType().getName() + " " + AlixUtils.getFields(new WrapperPlayServerSpawnPosition(event)));
            }else if (event.getPacketType() == PacketType.Play.Server.CHUNK_DATA) {
                if(new WrapperPlayServerChunkData(event).getColumn().getHeightMaps().getCompoundListTagOrThrow("MOTION_BLOCKING"))
                Main.logInfo("PACKET OUT: " + event.getPacketType().getName() + " " + AlixUtils.getFields(new WrapperPlayServerChunkData(event).getColumn()));
            }
            Main.logInfo("JUST PACKET OUT: " + event.getPacketType().getName());*/

            /* = UserManager.get(event.getUser().getUUID());

            if (user instanceof TemporaryUser) {
                ((TemporaryUser) user).getTempProcessor().onPacketSend(event);
            }*/
/*
            if (event.getPacketType() == PacketType.Configuration.Server.SELECT_KNOWN_PACKS) {
                new WrapperConfigServerSelectKnownPacks(event).getKnownPacks().add(new KnownPack("alix", "56789","1"));
                event.markForReEncode(true);
                Main.logError("SERVER PACKS: " + new WrapperConfigServerSelectKnownPacks(event).getKnownPacks());
            }*/

            /*Main.logInfo("PACKET OUT: " + event.getPacketType().getName());// + " NAMES " + ((Channel) event.getUser().getChannel()).pipeline().names());

            if (event.getPacketType() == PacketType.Play.Server.MAP_DATA) {
                Main.logError("DATA " + AlixUtils.getFields(new WrapperPlayServerMapData(event)));
            }
            if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {

                WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(event);
                int id = wrapper.getEntityId();
                Bukkit.getScheduler().runTask(Main.plugin, () -> {
                    Optional<Entity> opt = Bukkit.getWorlds().get(0).getEntities().stream().filter(e -> e.getEntityId() == id).findFirst();
                    if (opt.isPresent() && opt.get().getType() == EntityType.ITEM_FRAME) {
                        Main.logError("SPAWNNNN " + AlixUtils.getFields(wrapper));
                    }
                });
            }
            if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {

                WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);
                int id = wrapper.getEntityId();
                Bukkit.getScheduler().runTask(Main.plugin, () -> {
                    Optional<Entity> opt = Bukkit.getWorlds().get(0).getEntities().stream().filter(e -> e.getEntityId() == id).findFirst();
                    if (opt.isPresent() && opt.get().getType() == EntityType.ITEM_FRAME) {
                        wrapper.getEntityMetadata().forEach(data -> {
                            Main.logError("DATAAAAAAA " + data.getIndex() + " " + data.getType().getName() + " " + data.getValue());
                        });
                    }
                });
            }*/

                /*if (event.getPacketType() == PacketType.Play.Server.DECLARE_COMMANDS) {
                    //WrapperPlayServerDeclareCommands commands = new WrapperPlayServerDeclareCommands(event);
                    //debugCommands(loginCommands);
                    user.writeAndFlushConstSilently(CommandsPacketConstructor.LOGIN);
                    *//*AlixScheduler.runLaterAsync(() -> {
                        user.writeAndFlushConstSilently(loginCommands);
                        user.writeDynamicMessageSilently(Component.text("HEHEHHEHE"));
                    }, 10, TimeUnit.SECONDS);*//*

                    //debugCommands(commands);

                    event.setCancelled(true);
                    //event.markForReEncode(true);
                    return;
                }*/

            //if (event.getClass() != PacketPlaySendEvent.class) Main.debug("PACKET OUT: " + event.getPacketType().getName());
            if (debugPackets) Main.debug("PACKET OUT: " + event.getPacketType().getName());
            /*Channel channel = (Channel) event.getUser().getChannel();

            if (event.getClass() == PacketLoginSendEvent.class
                    && event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS
                    && LimboServerIntegration.hasCompletedCaptcha(channel)) {
                channel.unsafe().flush();
                Main.debug("FLUSHED");
                //ChannelPipeline pipeline = channel.pipeline();
                //ChannelHandlerContext ctx = pipeline.context("FlushConsolidationHandler#0");
                //if (ctx != null) pipeline.replace(ctx.name(), ctx.name(), new FlushConsolidationHandler());
            }*/

/*            if (event.getPacketType() == PacketType.Play.Server.WINDOW_PROPERTY) {
                Main.logInfo("PACKET OUT: " + event.getPacketType().getName() + " " + AlixUtils.getFields(new WrapperPlayServerWindowProperty(event)));
            }*/
            AlixUser user;

            if (event.getClass() == PacketPlaySendEvent.class && (user = UserManager.get(event.getUser().getUUID())) != null)
                user.getPacketProcessor().onPacketSend((PacketPlaySendEvent) event);//Main.logInfo("OUT NONNULL: " + event.getPacketType().getName());

            //if (!event.isCancelled()) Main.debug("PACKET OUT ALLOWED: " + event.getPacketType().getName());

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

        /*@Override
        public void onUserConnect(UserConnectEvent event) {
            Main.debug("CONNECTED: " + event.getUser().getChannel());
        }*/

        @Override
        public void onUserDisconnect(UserDisconnectEvent event) {
            String name = event.getUser().getName();
            AlixChannelHandler.removeFromTimeOut((Channel) event.getUser().getChannel());
            if (name == null) return;

            UserManager.removeConnecting(name);//can be done like this, since the temp name accounts for bedrock
            //Main.logError("DISCONNECTED: " + event.getUser().getName());
            //AlixChannelHandler.onDisconnect(event.getUser());
            //AlixUser user = LoginVerdictManager.getNullable(event.getUser().getUUID());
            //if (user == null) UserManager.removeConnecting(event.getUser().getName());
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

    public static void debugCommands(WrapperPlayServerDeclareCommands commands) {
        List<Node> list = commands.getNodes();
        for (Node n : list) {
            StringBuilder sb = new StringBuilder(list.indexOf(n) + " " + n.getName() + " ");
            byte flags = n.getFlags();

            switch (flags & 0b11) {
                case 0:
                    sb.append("ROOT ");
                    break;
                case 1:
                    sb.append("LITERAL ");
                    break;
                case 2:
                    sb.append("ARGUMENT ");
                    break;
                case 3:
                    sb.append("NOT USED ");
                    break;
            }
            if ((flags & 0x04) == 0x04) sb.append("EXECUTABLE ");
            if ((flags & 0x08) == 0x08) sb.append("HAS REDIRECT ");
            if ((flags & 0x10) == 0x10) sb.append("HAS SUGGESTIONS TYPE ");

            sb.append("CHILDREN: [");

            for (Integer i : n.getChildren()) sb.append(list.get(i).getName()).append(" (").append(i).append("), ");

            sb.append("] ");

            sb.append("PARSER: ").append(n.getParser().isPresent() ? n.getParser().get().getName() : n.getParser()).append(" ");

            if ((flags & 0x08) == 0x08)
                sb.append("REDIRECT: ").append(list.get(n.getRedirectNodeIndex()).getName()).append(" (").append(n.getRedirectNodeIndex()).append(") ");

            sb.append("PROPERTIES: ").append(n.getProperties()).append(" ");

            if ((flags & 0x10) == 0x10) sb.append("SUGGESTIONS TYPE: ").append(n.getSuggestionsType());

            Main.logInfo(sb.toString());
        }
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