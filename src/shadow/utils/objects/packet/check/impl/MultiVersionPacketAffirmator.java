package shadow.utils.objects.packet.check.impl;

import shadow.utils.holders.ReflectionUtils;

public final class MultiVersionPacketAffirmator extends AbstractPacketAffirmator {

    @Override
    public boolean isCommandOrKeepAlive(Object packet) {//the switch case instead of isCommand || isKeepAlive is because this one is faster
        switch (packet.getClass().getSimpleName()) {//client chat packet name varies in different versions
            case "PacketPlayInChat":
            case "PacketPlayInKeepAlive": //The player will time out without this one
            case "PacketPlayInClientCommand":
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isKeepAlive(Object packet) {
        return ReflectionUtils.inKeepAlivePacketClass == packet.getClass();
    }

    @Override
    public boolean isCommand(Object packet) {
        return isCommand0(packet.getClass().getSimpleName());
    }

    @Override
    public boolean isMove(Object packet) {
        return isMove0(packet.getClass().getSimpleName());
    }

    @Override
    public boolean isCommand(String name) {
        return isCommand0(name); //client chat packet name varies in different versions
    }

    private boolean isCommand0(String name) {
        return name.equals("PacketPlayInChat") || name.equals("PacketPlayInClientCommand");
    }
}