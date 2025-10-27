package alix.common.packets.message;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.function.BiFunction;

public final class MessageWrapper {

    public static PacketWrapper<?> createWrapper(String message, boolean actionBar, ServerVersion version) {
        var component = AdventureSerializer.serializer(version.toClientVersion()).fromLegacy(message);
        return createWrapper(component, actionBar, version);
    }

    public static PacketWrapper<?> createWrapper(Component message, boolean actionBar, ServerVersion version) {
        return createWrapperFunc(version).apply(message, actionBar);
    }

    //From User#sendMessage(Component, ChatType)
    private static BiFunction<Component, Boolean, PacketWrapper<?>> createWrapperFunc(ServerVersion version) {
        if (version.isNewerThanOrEquals(ServerVersion.V_1_19)) {
            //do not use this, results in a "<> [message]" format
            /*if (version.isNewerThanOrEquals(ServerVersion.V_1_19_3))
                return (message, actionBar) -> new WrapperPlayServerDisguisedChat(message,
                        new ChatType.Bound(ChatTypes.CHAT, Component.empty(), null));*/

            return (message, actionBar) -> new WrapperPlayServerSystemChatMessage(actionBar, message);
        } else return (message, actionBar) -> {
            if (actionBar) {
                if (PacketType.Play.Server.ACTION_BAR.getId(version.toClientVersion()) < 0) {//doesn't exist
                    return constructOld(version, message, ChatTypes.GAME_INFO);
                }
                return new WrapperPlayServerActionBar(message);
            }

            return constructOld(version, message, ChatTypes.CHAT);
        };
    }

    private static final UUID ZERO_UUID = new UUID(0L, 0L);

    private static WrapperPlayServerChatMessage constructOld(ServerVersion version, Component message, ChatType type) {
        ChatMessage m;
        if (version.isNewerThanOrEquals(ServerVersion.V_1_16)) m = new ChatMessage_v1_16(message, type, ZERO_UUID);
        else m = new ChatMessageLegacy(message, type);

        return new WrapperPlayServerChatMessage(m);
    }
}