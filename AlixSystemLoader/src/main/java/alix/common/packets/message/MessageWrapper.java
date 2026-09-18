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
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public final class MessageWrapper {

    //Parsing is client-version-agnostic (only serializing a Component back to wire bytes varies per real
    //client version - handled downstream, per version, by createWrapper(Component, ...) below), so a single
    //shared instance covers every caller. non-strict (strict(false)) is deliberate: see
    //legacyToMiniMessageTags()'s own comment for why a template's own literal "<player>"-style placeholder
    //text must render as plain text instead of throwing, rather than being escaped away at the source.
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().strict(false).build();

    //Parses a legacy '&'/'§'-formatted string (as produced by AlixFormatter.translateColors(), including its
    //own hex "&#RRGGBB"->"§x§R§R..." expansion and any {0}-style substituted values, already escaped for
    //MiniMessage by AlixFormatter's own formatSingle()/formatMultiple()) into a Component. Also resolves any
    //genuine MiniMessage tag syntax (<red>, <bold>, <gradient:...>, <hover:...>, <click:...>, ...) an operator
    //chose to write directly into a message file alongside legacy codes - see legacyToMiniMessageTags() for
    //how both are combined into one string before a single MiniMessage parse. Callers that only have a String
    //and no Component yet should use parseLegacy() once, up front, rather than re-parsing the same string on
    //every createWrapper(String, ...) call (see PacketPlayOutMessage).
    public static Component parseLegacy(String message) {
        return MINI_MESSAGE.deserialize(legacyToMiniMessageTags(message));
    }

    private static final Map<Character, String> LEGACY_TAG_NAMES = Map.ofEntries(
            Map.entry('0', "black"), Map.entry('1', "dark_blue"), Map.entry('2', "dark_green"), Map.entry('3', "dark_aqua"),
            Map.entry('4', "dark_red"), Map.entry('5', "dark_purple"), Map.entry('6', "gold"), Map.entry('7', "gray"),
            Map.entry('8', "dark_gray"), Map.entry('9', "blue"), Map.entry('a', "green"), Map.entry('b', "aqua"),
            Map.entry('c', "red"), Map.entry('d', "light_purple"), Map.entry('e', "yellow"), Map.entry('f', "white"),
            Map.entry('k', "obfuscated"), Map.entry('l', "bold"), Map.entry('m', "strikethrough"), Map.entry('n', "underlined"),
            Map.entry('o', "italic")
    );

    //True for the 16 named colors (0-9, a-f) - a color code, unlike a formatting one, resets every other
    //currently-open tag first, matching legacy Minecraft's own "a new color cancels prior bold/italic/etc."
    //behavior (see legacyToMiniMessageTags()).
    private static boolean isColorCode(char code) {
        return code >= '0' && code <= '9' || code >= 'a' && code <= 'f';
    }

    //Converts a §-normalized legacy-formatted string (AlixFormatter.translateColors()'s output) into
    //equivalent MiniMessage tag syntax, replicating legacy Minecraft's own semantics exactly: a color code
    //closes every currently-open tag (including any earlier formatting) before opening the new one, a
    //formatting code (bold/italic/etc.) just adds on top of whatever's already open, and §r closes
    //everything - the same rules Adventure's own LegacyComponentSerializer already applies, just emitted as
    //MiniMessage tag text instead of built directly into a Component, so the result can be parsed together
    //with any genuine MiniMessage tag syntax already present in the same string in one pass.
    //
    //Any text that isn't part of a recognized §-code sequence - including a template's own literal
    //"<player>"-style placeholder text, or an operator's genuine, intentional MiniMessage tag - passes
    //through completely untouched. The result is meant to be parsed by a non-strict MiniMessage instance (see
    //MINI_MESSAGE above), so any leftover literal "<...>" that isn't a real, recognized tag name renders as
    //plain text instead of throwing - confirmed none of this plugin's own bundled placeholders (<player>,
    //<password>, <status>, etc.) collide with a real MiniMessage tag name.
    private static String legacyToMiniMessageTags(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        Deque<String> openTags = new ArrayDeque<>(4);
        int len = s.length();
        int i = 0;
        while (i < len) {
            char ch = s.charAt(i);
            if (ch == '§' && i + 1 < len) {
                char code = s.charAt(i + 1);
                if (code == 'x' && isHexSequence(s, i)) {
                    closeAllTags(sb, openTags);
                    String hex = "#" + s.charAt(i + 3) + s.charAt(i + 5) + s.charAt(i + 7) + s.charAt(i + 9) + s.charAt(i + 11) + s.charAt(i + 13);
                    sb.append('<').append(hex).append('>');
                    openTags.push(hex);
                    i += 14;//"§x" + 6x("§"+hex digit)
                    continue;
                }

                String tag = LEGACY_TAG_NAMES.get(code);
                if (tag != null) {
                    if (isColorCode(code)) {
                        closeAllTags(sb, openTags);
                        sb.append('<').append(tag).append('>');
                        openTags.push(tag);
                    } else if (!openTags.contains(tag)) {//formatting code - stacks on top, but don't reopen if already active
                        sb.append('<').append(tag).append('>');
                        openTags.push(tag);
                    }
                    i += 2;
                    continue;
                }

                if (code == 'r') {//reset
                    closeAllTags(sb, openTags);
                    i += 2;
                    continue;
                }
            }
            sb.append(ch);
            i++;
        }
        closeAllTags(sb, openTags);
        return sb.toString();
    }

    //True if s.charAt(i) starts a well-formed "§x§R§R§G§G§B§B" sequence (14 characters: "§x" followed by 6
    //pairs of "§"+hex digit) - the exact, only shape AlixFormatter.translateColors() ever produces for a hex
    //color, so this only needs to validate structure, not the actual hex digits' validity.
    private static boolean isHexSequence(String s, int i) {
        int len = s.length();
        return i + 13 < len
                && s.charAt(i + 2) == '§' && s.charAt(i + 4) == '§' && s.charAt(i + 6) == '§'
                && s.charAt(i + 8) == '§' && s.charAt(i + 10) == '§' && s.charAt(i + 12) == '§';
    }

    private static void closeAllTags(StringBuilder sb, Deque<String> openTags) {
        while (!openTags.isEmpty()) sb.append("</").append(openTags.pop()).append('>');
    }

    public static PacketWrapper<?> createWrapper(String message, boolean actionBar, ServerVersion version) {
        return createWrapper(parseLegacy(message), actionBar, version);
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