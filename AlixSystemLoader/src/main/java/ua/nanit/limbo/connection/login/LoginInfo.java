package ua.nanit.limbo.connection.login;

import alix.common.login.LoginVerdict;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public record LoginInfo(boolean joinedRegistered, LoginVerdict verdict) {

    public static final AttributeKey<LoginInfo> JOIN_INFO = AttributeKey.newInstance("alix:join_info");

    public static void set(Channel channel, boolean joinedRegistered, LoginVerdict verdict) {
       channel.attr(LoginInfo.JOIN_INFO).set(new LoginInfo(joinedRegistered, verdict));
    }
}