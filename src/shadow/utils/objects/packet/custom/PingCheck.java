//package shadow.utils.objects.filter.packet.custom;
//
//import cf.zdybek.packethandler.LizuPacketEvents;
//import cf.zdybek.packethandler.handlers.InKeepAlive;
//import cf.zdybek.packethandler.impl.injection.ChannelHandler;
//import cf.zdybek.packethandler.impl.injection.PacketHandler;
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//import alix.common.scheduler.JavaScheduler;
//
//import java.util.concurrent.TimeUnit;
//
//public class PingCheck implements PacketHandler {
//
//    // private static final LongSupplier time = System::currentTimeMillis;
//    private final ChannelHandler handler;
//    private boolean cancelled;
//    //private long lastKeepAlive;
//
//    public PingCheck(Player p) {
//        ChannelHandler handler = LizuPacketEvents.registerPacketHandlerFor(p, "alix_system_ping_handler", this);
//        handler.startPingCheck(1);
//        this.handler = handler;
//        JavaScheduler.runLaterAsync(this::cancel, 30, TimeUnit.SECONDS);
//    }
//
//    public boolean hasFinished() {
//        return cancelled;
//    }
//
//    public synchronized void cancel() {
//        this.handler.stopPingCheck();
//        this.cancelled = true;
//    }
//
//    @Override
//    public boolean inKeepAlive(InKeepAlive inKeepAlive) {
//        //this.lastKeepAlive = time.getAsLong();
//        Bukkit.broadcastMessage("Ping: " + handler.getPing());
//        return true;
//    }/**/
//
///*    @Override
//    public void outKeepAlive(OutKeepAlive outKeepAlive) {
//
//    }*/
//}