package ua.nanit.limbo.commands;

import ua.nanit.limbo.connection.ClientConnection;

public interface CommandHandler<T extends ClientConnection> {

     void handleCommand(T conn, String cmd);

     //Function<T, LimboCommand> getCommandToSend();
}