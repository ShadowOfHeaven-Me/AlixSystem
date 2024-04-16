package shadow.utils.users.types;

import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import shadow.systems.login.result.LoginInfo;
import shadow.utils.objects.packet.EmptyProcessor;
import shadow.utils.objects.packet.PacketProcessor;

public final class TemporaryUser implements AlixUser {

    private final User user;
    private final LoginInfo loginInfo;

    public TemporaryUser(User user, LoginInfo loginInfo) {
        this.user = user;
        this.loginInfo = loginInfo;
    }

    @Override
    public User reetrooperUser() {
        return user;
    }

    public LoginInfo getLoginInfo() {
        return loginInfo;
    }

    @Override
    public PacketProcessor getPacketProcessor() {
        return EmptyProcessor.INSTANCE;
        //throw new AlixException("getPacketProcessor() should not be invoked on a TemporaryUser");
    }

    @Override
    public boolean isVerified() {
        return this.loginInfo.getVerdict().isAutoLogin();//otherwise needs verification, thus isn't currently verified
        //throw new AlixException("isVerified() should not be invoked on a TemporaryUser");
    }

    @Override
    public Channel getChannel() {
        return (Channel) user.getChannel();
    }

    @Override
    public ChannelHandlerContext silentContext() {
        throw new AlixException("silentContext() should not be invoked on a TemporaryUser");
    }
}