package alix.api.user;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public interface AlixCommonUser {

    /**
     * @return The netty Channel associated with this user
     **/
    Channel getChannel();

    //can be used for mapping, as it's never-changing per active session
    @NotNull
    default ChannelId id() {
        return this.getChannel().id();
    }

    default InetAddress getAddress() {
        return ((InetSocketAddress) this.getChannel().remoteAddress()).getAddress();
    }

    /**
     * @return Whether this user is verified (aka. will not be and currently isn't undergoing login/captcha)
     **/
    boolean isVerified();
}