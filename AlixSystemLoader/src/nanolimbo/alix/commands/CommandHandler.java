package nanolimbo.alix.commands;

import nanolimbo.alix.connection.ClientConnection;

import java.util.function.Function;

public interface CommandHandler<T extends ClientConnection> {

     void handleCommand(T conn, String cmd);

     Function<T, LimboCommand> getCommandToSend();
}