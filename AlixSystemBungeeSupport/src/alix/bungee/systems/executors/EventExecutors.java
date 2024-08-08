/*
package alix.bungee.systems.executors;

import alix.bungee.server.messages.BMessages;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public final class EventExecutors implements Listener {

    //private final Map<ProxiedPlayer, > map = new ConcurrentHashMap();
    private final BaseComponent invalidProtocolIdUserMessage = BMessages.get("invalid-alix-protocol-user");
    private final String invalidProtocolIdConsoleMessage = BMessages.getRaw("invalid-alix-protocol-console");

    @EventHandler
    public void onKick(ServerKickEvent event) {

    }

*/
/*    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals(BungeeProxyMessenger.ID_TAG)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        try {
            BungeeProxyMessenger.interpret(player, event.getData());
        } catch (Exception e) {
            player.disconnect(this.invalidProtocolIdUserMessage);
            AlixCommonMain.logError(this.invalidProtocolIdConsoleMessage);
            e.printStackTrace();
        }
    }*//*

}*/
