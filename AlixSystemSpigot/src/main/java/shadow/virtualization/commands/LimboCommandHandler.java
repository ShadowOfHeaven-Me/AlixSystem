/*
package shadow.virtualization.commands;

import alix.common.messages.Messages;
import alix.common.commands.file.AlixCommandInfo;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.utils.main.AlixUtils;
import shadow.virtualization.LimboConnection;
import ua.nanit.limbo.commands.CommandHandler;
import ua.nanit.limbo.commands.LimboCommand;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class LimboCommandHandler implements CommandHandler<LimboConnection> {

    private final Map<String, BiConsumer<LimboConnection, String>> map = new ConcurrentHashMap<>();
    private final LimboCommand registerCommand;
    private final LimboCommand loginCommand;

    public LimboCommandHandler() {
        this.registerCommand = AlixUtils.requirePasswordRepeatInRegister ? this.construct(AlixCommandManager.getCommand("register"), Messages.get("commands-register-password-arg"), Messages.get("commands-register-password-second-arg")) : this.construct(AlixCommandManager.getCommand("register"), Messages.get("commands-register-password-arg"));
        this.loginCommand = this.construct(AlixCommandManager.getCommand("login"), Messages.get("commands-login-password-arg"));
    }

    private LimboCommand construct(AlixCommandInfo info, String arg1Name) {
        List<String> list = info.createAliasesList();
        list.add(info.getCommand());

        return LimboCommand.construct(list, arg1Name);
    }

    private LimboCommand construct(AlixCommandInfo info, String arg1Name, String arg2Name) {
        List<String> list = info.createAliasesList();
        list.add(info.getCommand());

        return LimboCommand.construct(list, arg1Name, arg2Name);
    }

    @Override
    public void handleCommand(LimboConnection conn, String cmd) {
        String[] split = AlixUtils.split(cmd, ' ', 2);
        String command = split[0];

        if (split.length == 1) {

            return;
        }

    }

    */
/*@Override
    public Function<LimboConnection, LimboCommand> getCommandToSend() {
        return conn -> {
            *//*
*/
/*switch (conn.getVerificationReason()) {
                case LOGIN:
                    return this.loginCommand;
                case CAPTCHA:
                case REGISTER:
                    return this.registerCommand;
                default:
                    throw new AlixError("Invalid: " + conn.getVerificationReason());
            }*//*
*/
/*
        };
    }*//*

}*/
