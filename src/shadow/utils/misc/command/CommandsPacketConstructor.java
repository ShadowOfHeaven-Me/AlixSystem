package shadow.utils.misc.command;

import alix.common.messages.Messages;
import alix.libs.com.github.retrooper.packetevents.protocol.chat.Node;
import alix.libs.com.github.retrooper.packetevents.protocol.chat.Parsers;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareCommands;
import io.netty.buffer.ByteBuf;
import shadow.Main;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.UnverifiedUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CommandsPacketConstructor {

    private static final boolean supportAllChars = Main.config.getBoolean("command-support-all-characters");
    public static final ByteBuf REGISTER = CommandsPacketConstructor.constructRegister();
    public static final ByteBuf LOGIN = CommandsPacketConstructor.constructLogin();

    //https://wiki.vg/Command_Data
    private static ByteBuf constructLogin() {
        List<String> aliases = AlixCommandManager.getCommand("login").createAliasesList();
        aliases.add("login");

        return constructOneArg(aliases, Messages.get("commands-login-password-arg"));
    }

    private static ByteBuf constructRegister() {
        List<String> aliases = AlixCommandManager.getCommand("register").createAliasesList();
        aliases.add("register");

        if (AlixUtils.requirePasswordRepeatInRegister)
            return constructTwoArg(aliases, Messages.get("commands-register-password-arg"), Messages.get("commands-register-password-second-arg"));

        return constructOneArg(aliases, Messages.get("commands-register-password-arg"));
    }

    private static ByteBuf constructOneArg(List<String> commands, String argName) {
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

        //PacketEventsManager.debugCommands(new WrapperPlayServerDeclareCommands(list, 0));

        return NettyUtils.constBuffer(new WrapperPlayServerDeclareCommands(list, 0));
    }

    private static ByteBuf constructTwoArg(List<String> commands, String arg1Name, String arg2Name) {
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

        //PacketEventsManager.debugCommands(new WrapperPlayServerDeclareCommands(list, 0));

        return NettyUtils.constBuffer(new WrapperPlayServerDeclareCommands(list, 0));
    }

    public static void writeAndFlush(UnverifiedUser user) {
        if (user.hasCompletedCaptcha()) user.writeAndFlushConstSilently(buffer(user));
    }

    public static void write(UnverifiedUser user) {
        if (user.hasCompletedCaptcha()) user.writeConstSilently(buffer(user));
    }

    private static ByteBuf buffer(UnverifiedUser user) {
        return user.isRegistered() ? LOGIN : REGISTER;
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