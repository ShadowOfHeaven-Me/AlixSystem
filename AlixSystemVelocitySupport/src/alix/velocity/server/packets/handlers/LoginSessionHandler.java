package alix.velocity.server.packets.handlers;

import alix.velocity.server.packets.users.UnverifiedUser;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.player.LimboPlayer;

public final class LoginSessionHandler implements LimboSessionHandler {

    private final UnverifiedUser user;
    private LimboPlayer player;

    public LoginSessionHandler(UnverifiedUser user) {
        this.user = user;
    }

    @Override
    public void onSpawn(Limbo server, LimboPlayer player) {
        this.player = player;
        this.user.sendLoginPackets();
    }

    @Override
    public void onChat(String chat) {

    }
}