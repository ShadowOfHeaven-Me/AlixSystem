package shadow.virtualization;

import alix.common.utils.floodgate.GeyserUtil;
import io.netty.channel.Channel;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.netty.AlixChannelHandler;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.integration.PreLoginResult;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.server.LimboServer;

import java.util.function.Function;

//https://wiki.vg/Protocol_FAQ

public final class BukkitLimboIntegration extends LimboIntegration<LimboConnection> {

    /*@Nullable
    public static String removeCompletedCaptchaName(InetAddress address) {
        return completedCaptchaCache.remove(address);
    }*/

    static {
        GEYSER_UTIL = Dependencies.util;
    }

    //@Override
    /*public void invokeSilentServerChannelRead(Channel channel) {
        AlixInterceptor.invokeSilentChannelRead(channel);
    }*/

    @Override
    public PreLoginResult onLoginStart(LimboConnection connection, PacketLoginStart packet, boolean[] recode) {
        var result = super.onLoginStart(connection, packet, recode);
        if (result == PreLoginResult.CONNECT_TO_MAIN_SERVER)
            AlixChannelHandler.assignLoginUUID(connection.getChannel(), packet);
        return result;
    }

    @Override
    public LimboConnection newConnection(Channel channel, LimboServer server, Function<ClientConnection, VerifyState> state) {
        return new LimboConnection(channel, server, state);
    }

    /*@Override
    public boolean hasCompletedCaptcha(String name, Channel channel) {
        return Captcha.hasCompletedCaptcha(name, channel);// || ((InetSocketAddress) channel.remoteAddress()).getAddress().equals(this.completedCaptchaCache.getIfPresent(name));
    }*/

    @Override
    public void onHandshake(LimboConnection connection, PacketHandshake handshake) {
        AlixChannelHandler.onHandshake(connection.getChannel(), handshake);
    }

    /*private void join(LimboConnection connection, PacketLoginStart packet, boolean[] recode) {
        String name = packet.getUsername();
        Channel channel = connection.getChannel();
        PersistentUserData data = UserFileManager.get(name);
        UUID uuid = packet.getUUID();

        ClientPublicKey publicKey = ClientPublicKey.createKey(packet.getSignatureData());
        PremiumStatus suggestsStatus = PremiumUtils.suggestsStatus(uuid, publicKey);

        PremiumData premiumData = PremiumUtils.getOrRequestAndCacheData(data, name);
        //the player can be premium if: either the nickname is premium or we couldn't determine if the nickname is premium
        boolean canBePremium = premiumData.getStatus().isPremium() || premiumData.getStatus().isUnknown();

        boolean isPremium = canBePremium && PremiumSetting.isPremium(suggestsStatus);

        String prefixedName = PremiumUtils.getPrefixedName(name, isPremium);
        //PersistentUserData prefixedNameData = UserFileManager.get(prefixedName);

        //data of the account <name>
        if (data != null) {
            //both: the joining player and the existing data of the account <name> have been determined to have the same isPremium
            // - just assume we can connect the player normally
            if (data.getPremiumData().getStatus().isPremium() == isPremium) {
                //nothing to do here
            } else {
                //their isPremium was determined to be different - recode the packet to the prefixed name, as that should be our
                recode[0] = true;
                packet.wrapper().setUsername(prefixedName);
            }
        }

        *//*if (prefixedNameData != null) {
            //the account of the predicted premium status exists - this *should* be our player (we can't tell for pre 1.19 clients)
        } else {

        }*//*
    }*/


    @Override
    public GeyserUtil geyserUtil() {
        return Dependencies.util;
    }

    /*@Override
    public boolean isTransferAccepted() {
        return false;//ReflectionUtils.serverProperties.getBoolean("accepts-transfers");
    }*/

    //@Override
    /*public CommandHandler<LimboConnection> createCommandHandler() {
        return new LimboCommandHandler();
    }*/

    //@Override
    /*public String getServerIP() {
        return Bukkit.getServer().getIp();
    }

    //@Override
    public int getPort() {
        return Bukkit.getPort();
    }*/
}
