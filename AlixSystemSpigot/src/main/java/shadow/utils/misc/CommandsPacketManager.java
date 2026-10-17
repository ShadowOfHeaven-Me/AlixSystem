package shadow.utils.misc;

import alix.common.messages.Messages;
import alix.common.utils.netty.WrapperTransformer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.netty.buffer.ByteBuf;
import shadow.Main;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.UnverifiedUser;

import java.util.List;

import static alix.common.packets.command.CommandsWrapperConstructor.constructOneArg;
import static alix.common.packets.command.CommandsWrapperConstructor.constructTwoArg;

public final class CommandsPacketManager {

    private static final ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
    private static final boolean supportAllChars = Main.config.getBoolean("command-support-all-characters");
    public static final ByteBuf REGISTER = constructRegister();
    public static final ByteBuf LOGIN = constructLogin();
    public static final ByteBuf TERMS = constructTerms();
    public static final ByteBuf VERIFY_EMAIL = constructVerifyEmail();

    private static ByteBuf constructTerms() {
        List<String> aliases = AlixCommandManager.getCommand("terms").createAliasesList();
        aliases.add("terms");

        return constructOneArg(aliases, Messages.get("commands-terms-arg"), WrapperTransformer.CONST, version);
    }

    private static ByteBuf constructVerifyEmail() {
        List<String> aliases = AlixCommandManager.getCommand("verifyemail").createAliasesList();
        aliases.add("verifyemail");

        return constructOneArg(aliases, Messages.get("commands-verifyemail-code-arg"), WrapperTransformer.CONST, version);
    }

    //https://wiki.vg/Command_Data
    private static ByteBuf constructLogin() {
        List<String> aliases = AlixCommandManager.getCommand("login").createAliasesList();
        aliases.add("login");

        return constructOneArg(aliases, Messages.get("commands-login-password-arg"), WrapperTransformer.CONST, version);
    }

    private static ByteBuf constructRegister() {
        List<String> aliases = AlixCommandManager.getCommand("register").createAliasesList();
        aliases.add("register");

        if (AlixUtils.requirePasswordRepeatInRegister)
            return constructTwoArg(aliases, Messages.get("commands-register-password-arg"), Messages.get("commands-register-password-second-arg"), NettyUtils::constBuffer, version);

        return constructOneArg(aliases, Messages.get("commands-register-password-arg"), WrapperTransformer.CONST, version);
    }

    public static void writeAndFlush(UnverifiedUser user) {
        if (user.hasCompletedCaptcha()) user.writeAndFlushConstSilently(buffer(user));
    }

    public static void write(UnverifiedUser user) {
        if (user.hasCompletedCaptcha()) user.writeConstSilently(buffer(user));
    }

    //The advertised command list always needs to match whichever command the player is actually expected to
    //type next, or the client shows it in red as if it doesn't exist (even though it's handled fine
    //server-side) - mirrors LoginState#writeCommands() on Velocity.
    private static ByteBuf buffer(UnverifiedUser user) {
        if (user.isTermsGateBlocking()) return TERMS;
        if (user.isEmailRegisterGateBlocking()) return VERIFY_EMAIL;
        return user.isRegistered() ? LOGIN : REGISTER;
    }
}