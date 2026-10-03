package alix.common.packets.message;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;
import java.util.function.BiFunction;

public final class MessageWrapper {

    // Serializer for handling section character '§' and Hex colors
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    // Serializer for handling ampersand character '&' and Hex colors
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.AMPERSAND_CHAR)
            .hexColors()
            .build();

    /**
     * Parses legacy formatted string (§ or &) into a Component,
     * explicitly turning off default italics.
     */
    public static Component parseLegacy(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        // Handle ampersands first if present, then section symbols. Re-serializing with
        // AMPERSAND_SERIALIZER here (as this used to) is a no-op - a LegacyComponentSerializer uses the
        // same configured character for both directions, so it just writes the '&' codes straight back out.
        // SECTION_SERIALIZER.deserialize() below then never recognizes any of them (it only looks for '§'),
        // so every '&'-coded string - most of this plugin's configured text - came out as literal, unparsed
        // text with the '&' codes still in it. Re-serializing with SECTION_SERIALIZER instead actually
        // converts '&' codes to '§' codes, which the deserialize() call below can then parse, while still
        // correctly handling any '§' codes that were already present in the same string.
        String formatConverted = message.indexOf('&') != -1
                ? SECTION_SERIALIZER.serialize(AMPERSAND_SERIALIZER.deserialize(message))
                : message;

        Component parsed = SECTION_SERIALIZER.deserialize(formatConverted);

        // Explicitly set italic to false if not explicitly set by a §o code
        if (parsed.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            parsed = parsed.decoration(TextDecoration.ITALIC, false);
        }

        return parsed;
    }

    public static String parseToLegacyString(String message) {
        return SECTION_SERIALIZER.serialize(parseLegacy(message));
    }

    public static PacketWrapper<?> createWrapper(String message, boolean actionBar, ServerVersion version) {
        return createWrapper(parseLegacy(message), actionBar, version);
    }

    public static PacketWrapper<?> createWrapper(Component message, boolean actionBar, ServerVersion version) {
        return createWrapperFunc(version).apply(message, actionBar);
    }

    private static BiFunction<Component, Boolean, PacketWrapper<?>> createWrapperFunc(ServerVersion version) {
        if (version.isNewerThanOrEquals(ServerVersion.V_1_19)) {
            return (message, actionBar) -> new WrapperPlayServerSystemChatMessage(actionBar, message);
        } else {
            return (message, actionBar) -> {
                if (actionBar) {
                    if (PacketType.Play.Server.ACTION_BAR.getId(version.toClientVersion()) < 0) {
                        return constructOld(version, message, ChatTypes.GAME_INFO);
                    }
                    return new WrapperPlayServerActionBar(message);
                }
                return constructOld(version, message, ChatTypes.CHAT);
            };
        }
    }

    private static final UUID ZERO_UUID = new UUID(0L, 0L);

    private static WrapperPlayServerChatMessage constructOld(ServerVersion version, Component message, ChatType type) {
        ChatMessage m;
        if (version.isNewerThanOrEquals(ServerVersion.V_1_16)) {
            m = new ChatMessage_v1_16(message, type, ZERO_UUID);
        } else {
            m = new ChatMessageLegacy(message, type);
        }
        return new WrapperPlayServerChatMessage(m);
    }
}