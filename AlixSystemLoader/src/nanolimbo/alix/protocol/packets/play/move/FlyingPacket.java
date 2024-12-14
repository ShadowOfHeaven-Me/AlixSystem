package nanolimbo.alix.protocol.packets.play.move;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import nanolimbo.alix.protocol.PacketIn;

public interface FlyingPacket extends PacketIn {

    WrapperPlayClientPlayerFlying wrapper();

}