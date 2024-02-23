package shadow.systems.executors.packetevents;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareCommands;
import shadow.Main;

public final class PacketEventsAccess {

    public static void initAll() {
        //PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener());
        //Main.logInfo("Initialized additional functionality thanks to PacketEvents: Command Hiding");
    }

    private static final class PacketEventsListener extends PacketListenerAbstract {


        @Override
        public void onPacketSend(PacketSendEvent event) {
            if (event.getPacketType() == PacketType.Play.Server.DECLARE_COMMANDS) {// && Verifications.has(event.getUser().getUUID())) {
                WrapperPlayServerDeclareCommands packet = new WrapperPlayServerDeclareCommands(event);
                //WrapperPlayServerResourcePackSend
                Main.logWarning("DECLARE COMMANDS: " + packet.getNodes().toString());
                /*List<Node> list = new ArrayList<>();
                list.add(new Node((byte) 0, null, null, "yes", 5, null, null));
                list.add(new Node((byte) 0, null, null, "login", 5, null, null));
                packet.setNodes(list);*/
            }
        }

        private PacketEventsListener() {
        }
    }

    private PacketEventsAccess() {
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