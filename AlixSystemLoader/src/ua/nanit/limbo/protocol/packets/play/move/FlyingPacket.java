package ua.nanit.limbo.protocol.packets.play.move;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import ua.nanit.limbo.protocol.PacketIn;

public interface FlyingPacket extends PacketIn {

    WrapperPlayClientPlayerFlying wrapper();

}