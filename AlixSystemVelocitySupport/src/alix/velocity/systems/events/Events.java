package alix.velocity.systems.events;


import alix.velocity.systems.autoin.PremiumAutoIn;
import alix.velocity.systems.filters.ConnectionFilter;
import alix.velocity.utils.AlixHandler;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

import java.net.InetAddress;

public final class Events {

    //private static final JavaNetInetAddressAccess access = SharedSecrets.getJavaNetInetAddressAccess();
    private final ConnectionFilter[] filters = AlixHandler.getConnectionFilters();
    //private final ResultedEvent.ComponentResult DENIED = ResultedEvent.ComponentResult.denied(Component.text(""));


/*    @Subscribe(order = PostOrder.LAST)
    public void onPreConnect(ServerPreConnectEvent event) {

    }*/

    @Subscribe(order = PostOrder.LAST)
    public void onPreLogin(PreLoginEvent event) {
        if (!event.getResult().isAllowed()) return;

        String name = event.getUsername();
        if (PremiumAutoIn.contains(name)) return;

        InetAddress ip = event.getConnection().getRemoteAddress().getAddress();
        //String ip = event.getConnection().getRemoteAddress().getAddress().getHostAddress();

        for (ConnectionFilter filter : filters)
            if (filter.disallowJoin(ip, name))
                event.setResult(filter.getResult());
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPostLogin(PostLoginEvent event) {
        AlixHandler.handleJoin((ConnectedPlayer) event.getPlayer());
    }
}