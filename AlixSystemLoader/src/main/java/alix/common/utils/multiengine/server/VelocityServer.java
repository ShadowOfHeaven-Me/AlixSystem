package alix.common.utils.multiengine.server;

import alix.common.packets.message.MessageWrapper;
import com.velocitypowered.api.command.CommandSource;

public final class VelocityServer implements AbstractServer<CommandSource> {

    //Was Component.text(message) - see AlixUtils#sendMessage(CommandSource, String) for why that breaks hex codes.
    @Override
    public void sendMessage(CommandSource receiver, String message) {
        receiver.sendMessage(MessageWrapper.parseLegacy(message));
    }
}