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
        PacketEvents.getAPI().getEventManager().registerListeners(new GeneralListener(), new LoginInStartListener());
        PacketEvents.getAPI().init();
        //Main.logInfo("Initialized additional functionality thanks to PacketEvents: Command Hiding");
    }

    private static final class GeneralListener extends PacketListenerAbstract {

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            if (event.getClass() == PacketPlayReceiveEvent.class) {
                AlixUser user = UserManager.get(event.getUser().getUUID());
                if (user != null) user.getPacketProcessor().onPacketReceive((PacketPlayReceiveEvent) event);//Main.logInfo("IN NONNULL: " + event.getPacketType().getName());
            }
            //Main.logInfo("IN: " + event.getPacketType().getName());
        }

        @Override
        public void onPacketSend(PacketSendEvent event) {
            if (event.getClass() == PacketPlaySendEvent.class) {
                AlixUser user = UserManager.get(event.getUser().getUUID());
                if (user != null) user.getPacketProcessor().onPacketSend((PacketPlaySendEvent) event);//Main.logInfo("OUT NONNULL: " + event.getPacketType().getName());
            }
            //Main.logInfo("OUT: " + event.getPacketType().getName());
        }

        @Override
        public void onUserDisconnect(UserDisconnectEvent event) {
            //Main.logInfo("DISCONNECTED: " + event.getUser().getName());
            AlixChannelHandler.onDisconnect(event.getUser());
            UserManager.removeTemp(event.getUser().getName());
        }

        private GeneralListener() {
            super(PacketListenerPriority.MONITOR);
        }
    }

    private static final class LoginInStartListener extends PacketListenerAbstract {

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            if (event.getPacketType() == PacketType.Login.Client.LOGIN_START) AlixChannelHandler.onLoginStart(event);
        }

        private LoginInStartListener() {
            super(PacketListenerPriority.LOWEST);//make sure we read the LoginInStart packet first to indicate to other plugins that this packet might be invalid
        }
    }

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