package shadow.utils.misc.packet.constructors;

import alix.libs.com.github.retrooper.packetevents.PacketEvents;
import alix.libs.com.github.retrooper.packetevents.manager.server.ServerVersion;
import alix.libs.com.github.retrooper.packetevents.protocol.chat.ChatType;
import alix.libs.com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import alix.libs.com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import alix.libs.com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import alix.libs.com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import alix.libs.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import shadow.utils.netty.NettyUtils;

import java.util.UUID;
import java.util.function.BiFunction;

public final class OutMessagePacketConstructor {

/*    private static final Constructor<?> constructor;
    private static final boolean newerConstructor;
    private static final Enum<?> SYSTEM_MESSAGE_TYPE, ACTION_BAR;

    static {
        Class<?> clazz = ReflectionUtils.outChatMessagePacketClass;
        boolean newerCons = clazz.getSimpleName().equals("ClientboundSystemChatPacket");//PacketPlayOutChat is the older one
        Constructor<?> cons = null;
        for (Constructor<?> c : clazz.getConstructors()) {
            Class<?>[] params = c.getParameterTypes();
            if (newerCons && params.length == 2 && params[0] == ReflectionUtils.IChatBaseComponentClass && params[1] == boolean.class
                    || !newerCons && params.length == 3 && params[0] == ReflectionUtils.IChatBaseComponentClass && params[1] == ReflectionUtils.chatMessageType
                    && params[2] == UUID.class) {
                cons = c;
                break;
            }
        }
        constructor = cons;
        newerConstructor = newerCons;
        SYSTEM_MESSAGE_TYPE = newerCons ? null : (Enum<?>) ReflectionUtils.chatMessageType.getEnumConstants()[1];
        ACTION_BAR = newerCons ? null : (Enum<?>) ReflectionUtils.chatMessageType.getEnumConstants()[2];
    }*/

    public static ByteBuf constructDynamic(String message) {
        return constructDynamic(message, false);
    }

    public static ByteBuf constructDynamic(String message, boolean actionBar) {
        return NettyUtils.createBuffer(packetWrapper(Component.text(message), actionBar));
        //return newerConstructor ? construct_1_19(message, actionBar) : construct_old(message, actionBar);
    }

    public static ByteBuf constructConst(String message) {
        return constructConst(Component.text(message), false);
    }

    public static ByteBuf constructConst(String message, boolean actionBar) {
        return constructConst(Component.text(message), actionBar);
        //return newerConstructor ? construct_1_19(message, actionBar) : construct_old(message, actionBar);
    }

    public static ByteBuf constructConst(String message, boolean actionBar, boolean direct) {
        return constructConst(Component.text(message), actionBar, direct);
        //return newerConstructor ? construct_1_19(message, actionBar) : construct_old(message, actionBar);
    }

    public static ByteBuf constructConst(Component message) {
        return constructConst(message, false);
    }

    public static ByteBuf constructConst(Component message, boolean actionBar) {
        return NettyUtils.constBuffer(packetWrapper(message, actionBar));
        //return newerConstructor ? construct_1_19(message, actionBar) : construct_old(message, actionBar);
    }

    public static ByteBuf constructConst(Component message, boolean actionBar, boolean direct) {
        return NettyUtils.constBuffer(packetWrapper(message, actionBar), direct);
        //return newerConstructor ? construct_1_19(message, actionBar) : construct_old(message, actionBar);
    }
    
    public static PacketWrapper<?> packetWrapper(Component message) {
        return packetWrapper(message, false);
    }

    private static final ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
    private static final BiFunction<Component, Boolean, PacketWrapper<?>> packetWrapperFunc = createPacketWrapperFunc0();

    //From User#sendMessage(Component, ChatType)
    public static PacketWrapper<?> packetWrapper(Component message, boolean actionBar) {
        return packetWrapperFunc.apply(message, actionBar);
    }

    //From User#sendMessage(Component, ChatType)
    private static BiFunction<Component, Boolean, PacketWrapper<?>> createPacketWrapperFunc0() {
        if (version.isNewerThanOrEquals(ServerVersion.V_1_19)) {
            return (message, actionBar) -> new WrapperPlayServerSystemChatMessage(actionBar, message);
        } else return (message, actionBar) -> {
            if (actionBar) {
                if (PacketType.Play.Server.ACTION_BAR.getId(version.toClientVersion()) == -1) {//doesn't exist
                    return constructOld(message, ChatTypes.GAME_INFO);
                }
                return new WrapperPlayServerActionBar(message);
            }

            return constructOld(message, ChatTypes.CHAT);
        };
    }

    private static WrapperPlayServerChatMessage constructOld(Component message, ChatType type) {
        ChatMessage m;
        if (version.isNewerThanOrEquals(ServerVersion.V_1_16)) m = new ChatMessage_v1_16(message, type, new UUID(0L, 0L));
        else m = new ChatMessageLegacy(message, type);

        return new WrapperPlayServerChatMessage(m);
    }

    /*private static Object construct_1_19(String message, boolean actionBar) throws Exception {
        return constructor.newInstance(ReflectionUtils.constructTextComponents(message)[0], actionBar);
    }

    private static Object construct_old(String message, boolean actionBar) throws Exception {
        return constructor.newInstance(ReflectionUtils.constructTextComponents(message)[0], actionBar ? ACTION_BAR : SYSTEM_MESSAGE_TYPE, null);
    }*/

    private OutMessagePacketConstructor() {
    }
}