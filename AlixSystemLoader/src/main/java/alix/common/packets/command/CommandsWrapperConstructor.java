package alix.common.packets.command;

import alix.common.utils.netty.WrapperTransformer;
import alix.common.utils.netty.WrapperUtils;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.chat.Node;
import com.github.retrooper.packetevents.protocol.chat.Parsers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareCommands;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CommandsWrapperConstructor {

    public static ByteBuf constructOneArg(List<String> commands, String argName, boolean supportAllChars, WrapperTransformer transformer, ServerVersion version) {
        List<Node> list = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        //Main.logError("SUPPORT ALL CHARS " + supportAllChars + " " + Main.config.getKeys(false) + " " + Main.config.contains("command-support-all-characters") + " " + Main.config.getString("command-support-all-characters") + " B GET NOW " + Main.config.getBoolean("command-support-all-characters"));

        int index = 2;
        for (String ignored : commands) {
            indices.add(index++);
        }

        list.add(newNode0(NodeType.ROOT, null, indices, null, null));//0
        list.add(newNode0(NodeType.ARGUMENT, argName, Collections.emptyList(), Parsers.BRIGADIER_STRING, supportAllChars ? BrigadierString.GREEDY_PHRASE : BrigadierString.SINGLE_WORD, NodeFlag.IS_EXECUTABLE));//1

        for (String alias : commands) {
            list.add(newNode0(NodeType.LITERAL, alias, Collections.singletonList(1), null, null, NodeFlag.IS_EXECUTABLE));
        }

        var wrapper = new WrapperPlayServerDeclareCommands(list, 0);
        WrapperUtils.setVersion(wrapper, version);
        //PacketEventsManager.debugCommands(new WrapperPlayServerDeclareCommands(list, 0));

        return transformer.apply(wrapper);
    }

    public static ByteBuf constructTwoArg(List<String> commands, String arg1Name, String arg2Name, boolean supportAllChars, WrapperTransformer transformer, ServerVersion version) {
        List<Node> list = new ArrayList<>();
        List<Integer> indices = new ArrayList<>(commands.size());

        int index = supportAllChars ? 2 : 3;
        for (String ignored : commands) {
            indices.add(index++);
        }

        list.add(newNode0(NodeType.ROOT, null, indices, null, null));//0

        //Main.logError("SUPPORT ALL CHARS " + supportAllChars);

        if (supportAllChars) {
            list.add(newNode0(NodeType.ARGUMENT, arg1Name + ">] [<" + arg2Name, Collections.emptyList(), Parsers.BRIGADIER_STRING, BrigadierString.GREEDY_PHRASE, NodeFlag.IS_EXECUTABLE));//1
        } else {
            list.add(newNode0(NodeType.ARGUMENT, arg1Name, Collections.singletonList(2), Parsers.BRIGADIER_STRING, BrigadierString.SINGLE_WORD, NodeFlag.IS_EXECUTABLE));//1
            list.add(newNode0(NodeType.ARGUMENT, arg2Name, Collections.emptyList(), Parsers.BRIGADIER_STRING, BrigadierString.SINGLE_WORD, NodeFlag.IS_EXECUTABLE));//2
        }

        for (String alias : commands) {
            list.add(newNode0(NodeType.LITERAL, alias, Collections.singletonList(1), null, null, NodeFlag.IS_EXECUTABLE));
        }

        var wrapper = new WrapperPlayServerDeclareCommands(list, 0);
        WrapperUtils.setVersion(wrapper, version);
        //PacketEventsManager.debugCommands(new WrapperPlayServerDeclareCommands(list, 0));

        return transformer.apply(wrapper);
    }

    private static Node newNode0(NodeType nodeType, String name, List<Integer> children, Parsers.Parser parser, List<Object> properties, NodeFlag... flags) {
        int bitMask = nodeType.getBitMask();

        //Main.logError("ENCODED WITH " + nodeType + " " + Arrays.toString(flags));
        //Main.logError("ENCODING BIT MASK " + Integer.toBinaryString(bitMask));

        for (NodeFlag flag : flags) {
            bitMask |= flag.getBitMask();
            //Main.logError("ENCODING MASK v2 " + Integer.toBinaryString(bitMask) + " FLAG " + Integer.toBinaryString(flag.getBitMask()) + " " + flag.name());
        }

        /*StringBuilder sb = new StringBuilder();

        switch (bitMask & 0b11) {
            case 0:
                sb.append("ROOT ");
                break;
            case 1:
                sb.append("LITERAL ");
                break;
            case 2:
                sb.append("ARGUMENT ");
                break;
            case 3:
                sb.append("NOT USED ");
                break;
        }
        if ((bitMask & 0x04) == 0x04) sb.append("EXECUTABLE " + Integer.toBinaryString(0x04) + " ");
        if ((bitMask & 0x08) == 0x08) sb.append("HAS REDIRECT " + Integer.toBinaryString(0x08) + " ");
        if ((bitMask & 0x10) == 0x10) sb.append("HAS SUGGESTIONS TYPE " + Integer.toBinaryString(0x10) + " ");

        Main.logError("DECODED WITH " + sb);*/

        return new Node((byte) bitMask, children, 0, name, parser, properties, null);
    }
    //LiteralArgumentBuilder.literal("login").build();
}