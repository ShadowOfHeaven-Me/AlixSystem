package alix.velocity.utils.user;

import alix.api.user.AlixCommonUser;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.common.utils.other.annotation.ScheduledForFix;
import alix.velocity.server.AlixVelocityLimbo;
import alix.velocity.systems.packets.VerifiedPacketProcessor;
import alix.velocity.systems.packets.gui.AbstractAlixGUI;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.netty.channel.Channel;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.registry.Version;

public final class VerifiedUser implements AlixCommonUser {

    private final PersistentUserData data;
    public final User user;
    private final ConnectedPlayer player;
    private final Channel channel;
    private final VerifiedPacketProcessor verifiedPacketProcessor;
    private final Version version;
    //For PacketSnapshots
    @ScheduledForFix
    private final boolean isEncrypted;
    public AbstractAlixGUI gui;

    public VerifiedUser(ConnectedPlayer player) {
        this.player = player;
        this.data = UserFileManager.get(player.getUsername());
        this.user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        this.channel = (Channel) user.getChannel();
        this.version = Version.of(player.getProtocolVersion().getProtocol());
        this.verifiedPacketProcessor = new VerifiedPacketProcessor(this);
        this.isEncrypted = AlixVelocityLimbo.isEncrypted(this.channel);// this.data.getPremiumData().getStatus().isPremium();
    }

    public VerifiedPacketProcessor getDuplexProcessor() {
        return this.verifiedPacketProcessor;
    }

    public ConnectedPlayer getPlayer() {
        return player;
    }

    public PersistentUserData getData() {
        return data;
    }

    public Version getVersion() {
        return version;
    }

    public String getName() {
        return this.user.getName();
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public boolean isVerified() {
        return true;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void sendMessage(String msg) {
        this.user.sendMessage(msg);
    }

    public void write(PacketSnapshot snapshot) {
        if (this.isEncrypted) return;
        PacketUtils.write(this.channel, this.version, snapshot);
    }

    public void writeAndFlush(PacketSnapshot snapshot) {
        if (this.isEncrypted) return;
        this.write(snapshot);
        this.channel.flush();
    }

    public void writePacketSilently(PacketWrapper<?> wrapper) {
        this.user.writePacketSilently(wrapper);
    }

    public void sendPacketSilently(PacketWrapper<?> wrapper) {
        this.user.sendPacketSilently(wrapper);
    }

    //with PacketSnapshots
    @OptimizationCandidate
    public void closeInventory() {
        if (this.isEncrypted) this.user.closeInventory();
        else this.writeAndFlush(LoginState.CLOSE_INV);
    }
}