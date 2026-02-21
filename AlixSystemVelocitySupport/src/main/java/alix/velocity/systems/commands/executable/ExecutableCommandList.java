package alix.velocity.systems.commands.executable;

import alix.common.commands.executable.AbstractCommandList;
import alix.common.utils.config.ConfigProvider;
import alix.velocity.utils.user.VerifiedUser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.List;

import static alix.velocity.Main.SERVER;

public final class ExecutableCommandList extends AbstractCommandList<Player, ConsoleCommandSource, CommandSource> {

    private static final ExecutableCommandList
            registerCommandList = of("after-register-commands"),
            loginCommandList = of("after-login-commands"),
            autoRegisterCommandList = of("after-auto-register-commands"),
            autoLoginCommandList = of("after-auto-login-commands"),
            premiumJoinCommands = of("after-any-premium-join-commands");

    public static void executeFor(VerifiedUser user) {
        List<ExecutableCommandList> list = new ArrayList<>(1);

        var info = user.getDuplexProcessor().getLoginInfo();
        var verdict = info.verdict();

        if (!info.joinedRegistered() && !verdict.isAutoLogin()) list.add(registerCommandList);
        if (info.joinedRegistered() && !verdict.isAutoLogin()) list.add(loginCommandList);
        if (!info.joinedRegistered() && verdict.isAutoLogin()) list.add(autoRegisterCommandList);
        if (verdict.isAutoLogin()) list.add(autoLoginCommandList);
        if (verdict.isPremium()) list.add(premiumJoinCommands);

        for (ExecutableCommandList cmd : list) cmd.invoke(user.getPlayer());
    }

    private static ExecutableCommandList of(String path) {
        return new ExecutableCommandList(ConfigProvider.config.getStringList(path));
    }

    private ExecutableCommandList(List<String> config) {
        super(config, Player::getUsername, SERVER.getConsoleCommandSource(), ExecutableCommandList::dispatchCommand);
        //config.forEach(s -> SERVER.getConsoleCommandSource().sendPlainMessage("sex=" + s));
    }

    private static void dispatchCommand(CommandSource source, String cmd) {
        SERVER.getCommandManager().executeImmediatelyAsync(source, cmd);
    }
}
