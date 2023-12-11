/*
package shadow.utils.objects.filter.packet.custom;

import cf.zdybek.packethandler.LizuPacketEvents;
import cf.zdybek.packethandler.handlers.*;
import cf.zdybek.packethandler.impl.injection.ChannelHandler;
import cf.zdybek.packethandler.impl.injection.PacketHandler;
import org.bukkit.entity.Player;
import shadow.utils.objects.filter.packet.custom.types.DefaultPacketBlocker;
import shadow.utils.objects.filter.packet.custom.types.PingCheckPacketBlocker;
import shadow.utils.users.offline.UnverifiedUser;

public abstract class PacketBlocker implements PacketHandler {

    private static final String packetHandlerName = "alix_system_packet_handler";
    private final Player player;
    protected final ChannelHandler handler;

    public PacketBlocker(Player player) {
        this.player = player;
        this.handler = LizuPacketEvents.registerPacketHandlerFor(player, packetHandlerName, this);
    }

    public static PacketBlocker getPacketBlocker(UnverifiedUser user) {
        if (!user.isRegistered()) return new PingCheckPacketBlocker(user.getPlayer());
        return new DefaultPacketBlocker(user.getPlayer());
    }

    public void stop() {
        LizuPacketEvents.unregisterPacketHandler(player, packetHandlerName);
    }

    @Override
    public boolean inFlying(InFlying inFlying) {
        return false;
    }

    @Override
    public boolean inCloseWindow(InCloseWindow inCloseWindow) {
        return false;
    }

    @Override
    public boolean inWindowClick(InWindowClick inWindowClick) {
        return false;
    }

}
*/
