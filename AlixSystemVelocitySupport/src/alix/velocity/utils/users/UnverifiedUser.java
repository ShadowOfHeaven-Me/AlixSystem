package alix.velocity.utils.users;

import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.netty.channel.Channel;

public final class UnverifiedUser {

    private final ConnectedPlayer player;
    private final Channel channel;

    public UnverifiedUser(ConnectedPlayer player) {
        this.player = player;
        this.channel = player.getConnection().getChannel();
    }
}