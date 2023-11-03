package shadow.utils.objects.filter.packet.check.impl;

import shadow.Main;
import shadow.utils.objects.filter.packet.check.PacketAffirmator;

public abstract class AbstractPacketAffirmator implements PacketAffirmator {

    @Override
    public boolean isWindowOrKeepAlive(Object packet) {
        //Main.logInfo(packet.getClass().getSimpleName());
        switch (packet.getClass().getSimpleName()) {
            case "PacketPlayInWindowClick":
            case "PacketPlayInCloseWindow":
            case "PacketPlayInKeepAlive": //The player will time out without this one
                //Main.logInfo("RECEIVED: " + packet.getClass().getSimpleName());
                return true; //sends packets only if they are gui-related
        }
        //Main.logInfo("BLOCKED: " + packet.getClass().getSimpleName());
        return false;
    }

    @Override
    public boolean isKeepAlive(String name) {
        return name.equals("PacketPlayInKeepAlive");
    }

    @Override
    public boolean isMove(String name) {
        return isMove0(name);
    }

    protected boolean isMove0(String name) {
        switch (name) {
            case "PacketPlayInPosition":
            case "PacketPlayInPositionLook":
            case "PacketPlayInLook":
            case "d":
                return true;
            default:
                return false;
        }
    }

}