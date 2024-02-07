package alix.velocity.server.packets.users;

import alix.velocity.server.AlixServer;
import alix.velocity.server.packets.handlers.CaptchaSessionHandler;
import alix.velocity.server.packets.handlers.LoginSessionHandler;
import alix.velocity.systems.captcha.Captcha;
import alix.velocity.systems.captcha.CaptchaManager;
import alix.velocity.utils.data.PersistentUserData;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.netty.channel.Channel;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.player.LimboPlayer;

public final class UnverifiedUser {

    private static final Limbo limbo = AlixServer.getLimbo();
    private static final CaptchaManager captchaManager = new CaptchaManager();
    private final ConnectedPlayer player;
    private final PersistentUserData data;
    //private final VelocityServer originalServer;
    private final Channel channel;
    private final Captcha captcha;
    private volatile boolean hasCompletedCaptcha;

    public UnverifiedUser(ConnectedPlayer player, PersistentUserData data) {
        this.player = player;
        this.channel = player.getConnection().getChannel();
        //this.originalServer = player.getCurrentServer()
        this.hasCompletedCaptcha = data != null;
        this.captcha = hasCompletedCaptcha ? captchaManager.pollNextCaptcha() : null;
        this.data = data;

        limbo.spawnPlayer(player, hasCompletedCaptcha ? new LoginSessionHandler(this) : new CaptchaSessionHandler(this));
    }

    public final ConnectedPlayer getPlayer() {
        return player;
    }

    public final void sendLoginPackets() {
        if (!hasCompletedCaptcha) captcha.sendPackets(this.channel);
    }

    public boolean hasCompletedCaptcha() {
        return hasCompletedCaptcha;
    }

    public void completeCaptcha() {
        this.hasCompletedCaptcha = true;
    }
}