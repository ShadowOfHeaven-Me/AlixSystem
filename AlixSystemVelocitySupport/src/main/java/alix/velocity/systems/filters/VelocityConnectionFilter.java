package alix.velocity.systems.filters;

import com.velocitypowered.api.event.connection.PreLoginEvent;
import net.kyori.adventure.text.Component;

import java.net.InetAddress;

public interface VelocityConnectionFilter {

    boolean disallowJoin(InetAddress ip, String name);

    PreLoginEvent.PreLoginComponentResult getResult();

    static PreLoginEvent.PreLoginComponentResult wrap(String reason) {
        return PreLoginEvent.PreLoginComponentResult.denied(Component.text(reason));
    }
}