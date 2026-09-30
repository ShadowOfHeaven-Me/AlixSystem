package shadow.systems.login.auth;

import alix.common.login.auth.GoogleAuthExplanation;
import alix.common.login.auth.GoogleAuthUtils;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
                        //QR_CODE_TP_LOC.asModifiableCopy().add(x, y, z).getBlock().getBoundingBox()
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

    //Falls back to the click-event-free version of the message if serializing the clickable one throws -
    //some server builds ship a packetevents/Adventure combination whose ClickEvent NBT serialization is
    //broken (a reflection lookup failing internally to packetevents, not something this plugin controls),
    //which would otherwise throw here, inside this class's static initializer, and per normal Java semantics
    //permanently break every use of GoogleAuth for the rest of the server's uptime the first time anyone
    //tried to view a QR code - not merely that one attempt. "/confirm"/"/cancel" still work fully as typed
    //commands in the fallback; only their clickability is lost.
    public static final ByteBuf MESSAGE = constructMessageSafely();

    private static ByteBuf constructMessageSafely() {
        try {
            return OutMessagePacketConstructor.constructConst(GoogleAuthExplanation.COMBINED);
        } catch (Throwable t) {
            shadow.Main.logWarning("Failed to build the 2FA QR code message with clickable confirm/cancel text - "
                    + "falling back to a non-clickable version. This is very likely a packetevents/Adventure "
                    + "version mismatch on this server build, not something wrong with your AlixSystem setup. "
                    + "Cause: " + t);
            return OutMessagePacketConstructor.constructConst(GoogleAuthExplanation.COMBINED_NO_CLICK_EVENTS);
        }
    }

    public static void showQRCode(VerifiedUser user, Player player) {
        if (user == null) return;

        var token = user.getData().getToken();

        try {
            String joinedWithIp = AlixChannelHandler.getJoinedWithIP(user.getChannel());
            //InetAddress.getByName(joinedWithIp).
            byte[] bytes = GoogleAuthUtils.createQRCode(GoogleAuthUtils.getGoogleAuthenticatorBarCode(token, joinedWithIp, "AlixSystem"), 256, 256);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));

            ByteBuf[] buffers = ImageRenderer.qrCode(image);

            Channel channel = user.getChannel();

            user.getDuplexProcessor().startQRCodeShow();
            Location loc = player.getLocation();
            user.originalLocation.set(loc);

            OriginalLocationsManager.add(player, loc);//try to prevent any potential data loss

            AlixScheduler.sync(() -> {
                //A cross-world teleport (this one, into the dedicated captcha world) while the player has an
                //item actively "in use" - a charged/aimed trident (Riptide-style wind-up), a drawn bow, food
                //being eaten, a raised shield - is a known vanilla edge case where that item can be lost
                //outright, surviving even a relog, since the loss happens server-side as part of the
                //dimension change itself rather than being a client-only display desync. clearActiveItem()
                //is Paper's documented, safe way to end an active-use action without losing the item -
                //calling it right before the teleport, whenever there's an active item to clear, closes that
                //window instead of letting the teleport interrupt it uncontrolled.
                if (player.hasActiveItem()) player.clearActiveItem();

                MethodProvider.teleportAsyncPluginCause(player, QR_CODE_TP_LOC).thenAccept(b -> {
                    if (!b) {
                        for (ByteBuf buf : buffers) buf.release();
                        user.originalLocation.set(null);
                        channel.eventLoop().execute(() -> {
                            user.writeAndFlushConstSilently(tpFailedMessage);
                            user.getDuplexProcessor().endQRCodeShow();
                            MethodProvider.closeInventoryAsyncSilently(user.silentContext());
                        });
                        return;
                    }
                    channel.eventLoop().execute(() -> {
                        user.writeConstSilently(OutGameStatePacketConstructor.SPECTATOR_GAMEMODE_PACKET);
                        user.writeConstSilently(PLAYER_ABILITIES_PACKET);
                        for (ByteBuf buf : buffers) user.writeSilently(buf);

                        user.writeConstSilently(MESSAGE);
                        //user.flush();
                        MethodProvider.closeInventoryAsyncSilently(user.silentContext());//serves as a flush
                    });
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