package alix.velocity.systems.filters;

import alix.common.connection.filters.ConnectionFilter;
import com.velocitypowered.api.event.connection.PreLoginEvent;

import java.net.InetAddress;

public final class ConnectionFilterAdapter implements VelocityConnectionFilter {

    private final ConnectionFilter filter;
    private final PreLoginEvent.PreLoginComponentResult result;

    public ConnectionFilterAdapter(ConnectionFilter filter) {
        this.filter = filter;
        this.result = VelocityConnectionFilter.wrap(this.filter.getReason());
    }

    @Override
    public boolean disallowJoin(InetAddress ip, String name) {
        return this.filter.disallowJoin(ip, ip.getHostName(), name);
    }

    @Override
    public PreLoginEvent.PreLoginComponentResult getResult() {
        return this.result;
    }
}