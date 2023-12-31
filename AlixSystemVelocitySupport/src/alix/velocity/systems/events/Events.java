package alix.velocity.systems.events;

import alix.common.antibot.connection.ConnectionFilter;
import alix.velocity.utils.AlixHandler;
import alix.velocity.utils.users.UnverifiedUser;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import net.kyori.adventure.text.Component;

public final class Events {

    private final ConnectionFilter[] filters = AlixHandler.getConnectionFilters();
    //private final ResultedEvent.ComponentResult DENIED = ResultedEvent.ComponentResult.denied(Component.text(""));


    @Subscribe(order = PostOrder.LAST)
    public void onPreConnect(ServerPreConnectEvent event) {

    }

    @Subscribe(order = PostOrder.LAST)
    public void onPreLogin(PreLoginEvent event) {
        if (!event.getResult().isAllowed()) return;

        String name = event.getUsername();
        String ip = event.getConnection().getRemoteAddress().getAddress().getHostAddress();

        for (ConnectionFilter filter : filters) {
            if (filter.disallowJoin(ip, name)) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(filter.getReason())));
                //event.setResult(ResultedEvent.ComponentResult.denied(Component.text(filter.getReason())));
            }
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onJoin(LoginEvent event) {
        UnverifiedUser user = AlixHandler.handleOfflineUserJoin((ConnectedPlayer) event.getPlayer());
    }
}