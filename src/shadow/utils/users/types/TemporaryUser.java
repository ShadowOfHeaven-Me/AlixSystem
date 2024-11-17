package shadow.utils.users.types;

import alix.libs.com.github.retrooper.packetevents.protocol.player.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import shadow.systems.login.result.LoginInfo;
import shadow.utils.netty.unsafe.ByteBufHarvester;
import shadow.utils.objects.packet.EmptyProcessor;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.objects.packet.TemporaryUnverifiedProcessor;

public final class TemporaryUser implements AlixUser {

    private final User user;
    private final LoginInfo loginInfo;
    private final ByteBufHarvester bufHarvester;
    private final TemporaryUnverifiedProcessor unverifiedProcessor;
    //private final TemporaryProcessor processor = new TemporaryProcessor();

    public TemporaryUser(User user, LoginInfo loginInfo) {
        this.user = user;
        this.loginInfo = loginInfo;
        this.bufHarvester = ByteBufHarvester.newHarvesterFor(this.getChannel());
        this.unverifiedProcessor = this.isVerified() ? null : new TemporaryUnverifiedProcessor();
    }

    @Override
    public User reetrooperUser() {
        return user;
    }

    public LoginInfo getLoginInfo() {
        return loginInfo;
    }

    public ByteBufHarvester getBufHarvester() {
        return bufHarvester;
    }

    @Override
    public PacketProcessor getPacketProcessor() {
        return this.unverifiedProcessor != null ? this.unverifiedProcessor : EmptyProcessor.INSTANCE;
        //throw new AlixException("getPacketProcessor() should not be invoked on a TemporaryUser");
    }

    public TemporaryUnverifiedProcessor getUnverifiedProcessor() {
        return unverifiedProcessor;
    }

    @Override
    public boolean isVerified() {
        return this.loginInfo.getVerdict().isAutoLogin();//otherwise needs verification, thus isn't currently verified
        //throw new AlixException("isVerified() should not be invoked on a TemporaryUser");
    }

    @Override
    public Channel getChannel() {
        return (Channel) this.user.getChannel();
    }

    @Override
    public ChannelHandlerContext silentContext() {
        return null;
        //throw new AlixException("silentContext() should not be invoked on a TemporaryUser");
    }
}