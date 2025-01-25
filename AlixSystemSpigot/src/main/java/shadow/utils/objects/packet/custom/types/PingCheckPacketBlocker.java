/*
package shadow.utils.objects.filter.packet.custom.types;

import cf.zdybek.packethandler.handlers.InFlying;
import cf.zdybek.packethandler.handlers.InKeepAlive;
import cf.zdybek.packethandler.handlers.OutKeepAlive;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import shadow.utils.objects.filter.packet.custom.PacketBlocker;

public class PingCheckPacketBlocker extends PacketBlocker {

    public PingCheckPacketBlocker(Player p) {
        super(p);
        Bukkit.broadcastMessage("rtyuiop");
        super.handler.startPingCheck(1);
    }

    @Override
    public boolean inKeepAlive(InKeepAlive inKeepAlive) {
        Bukkit.broadcastMessage("Ping: " + handler.getPing());
        return true;
    }

*/
/*    @Override
    public boolean inFlying(InFlying inFlying) {
        Bukkit.broadcastMessage("ghjk");
        return super.inFlying(inFlying);
    }*//*


    */
/*    @Override
    public void stop() {
        super.stop();
        handler.stopPingCheck(); //Lis już to robi
    }*//*

}*/
