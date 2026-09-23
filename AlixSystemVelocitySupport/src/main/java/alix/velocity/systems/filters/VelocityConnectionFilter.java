package alix.velocity.systems.filters;

import alix.common.packets.message.MessageWrapper;
import com.velocitypowered.api.event.connection.PreLoginEvent;

import java.net.InetAddress;

public interface VelocityConnectionFilter {

    boolean disallowJoin(InetAddress ip, String name);

    PreLoginEvent.PreLoginComponentResult getResult();

    //Was Component.text(reason) - see AlixUtils#sendMessage(CommandSource, String) for why that breaks hex codes.
    static PreLoginEvent.PreLoginComponentResult wrap(String reason) {
        return PreLoginEvent.PreLoginComponentResult.denied(MessageWrapper.parseLegacy(reason));
    }
}