package shadow.utils.holders.packet.constructors;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import io.netty.buffer.ByteBuf;
import shadow.utils.netty.NettyUtils;

public final class OutGameStatePacketConstructor {

    public static final ByteBuf ADVENTURE_GAMEMODE_PACKET = NettyUtils.constBuffer(new WrapperPlayServerChangeGameState(WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE, 2));

    /*static {
        Class<?> packetClazz = ReflectionUtils.outGameStatePacketClass;
        Object adventurePacket = null;
        try {
            for (Constructor<?> c : packetClazz.getConstructors()) {
                Class<?>[] params = c.getParameterTypes();
                if (params.length == 2) {
                    if (params[0] == int.class && params[1] == float.class) {
                        adventurePacket = c.newInstance(3, 2f);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        if (adventurePacket == null && ReflectionUtils.protocolVersion)
            adventurePacket = ProtocolAccess.newGameModePacket(GameMode.ADVENTURE);
        if (adventurePacket == null) throw new AlixError();
        ADVENTURE_GAMEMODE_PACKET = adventurePacket;
    }*/

    private OutGameStatePacketConstructor() {
    }
}