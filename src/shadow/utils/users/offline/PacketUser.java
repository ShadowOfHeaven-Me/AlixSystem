/*
package shadow.utils.users.offline;

import org.bukkit.entity.Player;
import shadow.utils.objects.filter.packet.types.PacketBlocker;
import shadow.utils.objects.savable.data.PersistentUserData;

public class PacketUser extends UnverifiedUser {

    //private final Location loc;

    public PacketUser(Player p, PersistentUserData data) {
        super(p, data);
*/
/*        loc = p.getLocation();
        double y = loc.getY();
        loc.setY(10000);
        p.teleport(loc);
        loc.setY(y);*//*

        this.blocker = PacketBlocker.getPacketBlocker(this);
    }

    @Override
    public void disableActionBlocker() {
        returnToOriginalSelf();
        this.blocker.stop();
        getPlayer().teleport(getPlayer().getLocation()); // <- refresh (as packets not being delivered to the server create an illusion of moving for the player) & primordial location return
    }

    @Override
    public void completeCaptcha() {
        blocker.startLoginKickTask();
        super.completeCaptcha();
    }
}*/
