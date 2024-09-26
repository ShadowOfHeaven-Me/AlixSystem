package shadow.systems.login.auth;

import alix.common.antibot.captcha.secrets.files.UserTokensFileManager;
import alix.common.login.auth.GoogleAuthUtils;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.other.keys.secret.MapSecretKey;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import shadow.systems.login.reminder.AuthReminder;
import shadow.systems.netty.AlixChannelHandler;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.misc.captcha.ImageRenderer;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.OutGameStatePacketConstructor;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.misc.packet.constructors.OutPositionPacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.VerifiedUser;
import shadow.utils.world.AlixWorld;
import shadow.utils.world.location.ConstLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;

public final class GoogleAuth {

    /*
     * #Defines after how many seconds the Google Authenticator QR code should be deleted. Set it to 0 or less in order to disable it
     * qr-code-lifetime: 60
     */
    //private static final int qrCodeLifeTime = Main.config.getInt("qr-code-lifetime");
    public static final ConstLocation QR_CODE_TP_LOC = new ConstLocation(AlixWorld.CAPTCHA_WORLD, 1000, 100, 1000, 0, 0);
    public static final ConstLocation QR_CODE_SHOW_LOC = QR_CODE_TP_LOC.asModifiableCopy().add(0, 2, 1).toConst();
    public static final ByteBuf QR_LOC_TELEPORT = OutPositionPacketConstructor.constructConst(QR_CODE_TP_LOC);
    private static final ByteBuf tpFailedMessage = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("google-auth-tp-failed"));

    static {
        AlixScheduler.sync(() -> {
            for (int x = -1; x <= 1; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -1; z <= 1; z++) {
                        QR_CODE_TP_LOC.asModifiableCopy().add(x, y, z).getBlock().setType(Material.AIR);
                    }
                }
            }
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    QR_CODE_TP_LOC.asModifiableCopy().add(x, -1, z).getBlock().setType(Material.BARRIER);
                }
            }
        });
    }

    private static final ByteBuf PLAYER_ABILITIES_PACKET = NettyUtils.constBuffer(new WrapperPlayServerPlayerAbilities(true, false, false, false, 0.05f, 0.1f));

    public static void showQRCode(VerifiedUser user, Player player) {
        if (user == null) return;

        //Main.logError("INVOKED QR SHOW");
        MapSecretKey key = MapSecretKey.uuidKey(player.getUniqueId());
        String token = UserTokensFileManager.getTokenOrSupply(key, GoogleAuthUtils::generateSecretKey);

        try {
            String joinedWithIp = AlixChannelHandler.getJoinedWithIP(user.getChannel());
            //InetAddress.getByName(joinedWithIp).
            byte[] bytes = GoogleAuthUtils.createQRCode(GoogleAuthUtils.getGoogleAuthenticatorBarCode(token, joinedWithIp, "AlixSystem"), 256, 256);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));

            ByteBuf[] buffers = ImageRenderer.qrCode(image);

            /*AlixScheduler.repeatAsync(() -> {
                Main.logError("CODE: " + GoogleAuthUtils.getTOTPCode(token) + " TOKEN: " + token);
            }, 1, TimeUnit.SECONDS);*/

            Channel channel = user.getChannel();

            user.getDuplexProcessor().startQRCodeShow();
            Location loc = player.getLocation();
            user.originalLocation.set(loc);

            OriginalLocationsManager.add(player, loc);//try to prevent any potential data loss
            CompletableFuture<Boolean> tpFuture = MethodProvider.teleportAsyncPluginCause(player, QR_CODE_TP_LOC);
            tpFuture.thenAccept(b -> {
                if (!b) {
                    for (ByteBuf buf : buffers) buf.release();
                    user.writeAndFlushConstSilently(tpFailedMessage);
                    user.originalLocation.set(null);
                    user.getDuplexProcessor().endQRCodeShow();
                    MethodProvider.closeInventoryAsyncSilently(user.silentContext());
                    return;
                }
                channel.eventLoop().execute(() -> {
                    user.writeConstSilently(OutGameStatePacketConstructor.SPECTATOR_GAMEMODE_PACKET);
                    user.writeConstSilently(PLAYER_ABILITIES_PACKET);
                    for (ByteBuf buf : buffers) {
                        user.writeSilently(buf);
                        //if (i % 3 == 0) user.flush();
                    }
                    user.writeConstSilently(AuthReminder.MESSAGE);
                    //user.flush();
                    MethodProvider.closeInventoryAsyncSilently(user.silentContext());//serves as a flush
                });
            });


/*            if (qrCodeLifeTime > 3)
                channel.eventLoop().schedule(() -> {
                    int idStart = ImageRenderer.QR_ENTITY_ID_START + 1;

                    user.writeAndFlushSilently(OutEntityDestroyPacketConstructor.constructDynamic(idStart, buffers.length / 3));
                }, qrCodeLifeTime, TimeUnit.SECONDS);*/

            //Main.logError("BUFFERS " + buffers.length);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}