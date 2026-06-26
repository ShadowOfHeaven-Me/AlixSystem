package shadow.virtualization;

import alix.common.data.PersistentUserData;
import alix.common.utils.AlixCache;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.floodgate.GeyserUtil;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.login.result.LoginVerdictManager;
import shadow.systems.netty.AlixChannelHandler;
import shadow.systems.netty.AlixInterceptor;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.integration.PreLoginInfo;
import ua.nanit.limbo.integration.PreLoginResult;
import ua.nanit.limbo.integration.StateSupplier;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.protocol.packets.play.transfer.PacketPlayOutTransfer;
import ua.nanit.limbo.protocol.registry.Version;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

//https://wiki.vg/Protocol_FAQ

public final class BukkitLimboIntegration extends LimboIntegration<ClientConnection> {

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
    public void onLoginStart(ClientConnection connection, PacketLoginStart packet, Consumer<PreLoginInfo> consumer) {
        super.onLoginStart(connection, packet, result -> {
            if (result.result() == PreLoginResult.CONNECT_TO_MAIN_SERVER)
                AlixChannelHandler.assignLoginUUID(connection.getChannel(), packet);

            consumer.accept(result);
        });
    }

    private static final boolean limboLogin = ConfigProvider.config.getBoolean("virtual-limbo-login");
    //public static final Map<String, byte[]> LIMBO_LOGINS = AlixCache.newBuilder().maximumSize(300).expireAfterWrite(1, TimeUnit.MINUTES).<String, byte[]>build().asMap();
    public static final Map<String, InetAddress> LIMBO_LOGINS = !limboLogin ? null : AlixCache.newBuilder().maximumSize(300).expireAfterWrite(20, TimeUnit.SECONDS).<String, InetAddress>build().asMap();

    public static boolean hasVerifiedInLimbo(String name, InetAddress addr) {
        if (!limboLogin)
            return false;

        var stored = LIMBO_LOGINS.get(name);
        return stored != null && stored.equals(addr);
    }

    @Override
    public boolean limboLogin(ClientConnection connection, PersistentUserData data) {
        if (!limboLogin)
            return false;
        //disabled for now
        /*if (true)
            return false;*/

        if (connection.getClientVersion().less(Version.V1_20_5))
            return false;

        var addr = connection.getAddress();
        if (LoginVerdictManager.getVerdict(addr, data).isAutoLogin())
            return false;

        //already done in LoginVerdictManager
        //if(hasVerifiedInLimbo())

        connection.setVerifyState(StateSupplier.LOGIN);
        //connection.writeAndFlushPacket(new PacketOutLoginCookieRequest());

        NanoLimbo.LIMBO.getClientChannelInitializer().initAfterLimboLogin(connection, data,
                conn -> {
                    LIMBO_LOGINS.put(conn.getUsername(), addr);
                    var handshakePacket = conn.getHandshakePacket();

                    String host = handshakePacket.getExtractedHost();
                    int port = handshakePacket.getPort();

                    //Version.V1_20_5 >=
                    conn.sendPacketAndClose(new PacketPlayOutTransfer().setHost(host).setPort(port));
                }, this.geyserUtil());
        return true;
    }

    /*@Override
    public ClientConnection newConnection(Channel channel, LimboServer server, Function<ClientConnection, VerifyState> state) {
        return new ClientConnection(channel, server, state);
    }*/

    /*@Override
    public boolean hasCompletedCaptcha(String name, Channel channel) {
        return Captcha.hasCompletedCaptcha(name, channel);// || ((InetSocketAddress) channel.remoteAddress()).getAddress().equals(this.completedCaptchaCache.getIfPresent(name));
    }*/

    @Override
    public boolean isProxyProtocol() {
        return AlixInterceptor.PROXY_PROTOCOL;
    }

    @Override
    public void onHandshake(ClientConnection connection, PacketHandshake handshake) {
        AlixChannelHandler.onHandshake(connection.getChannel(), handshake);
    }

    /*private void join(ClientConnection connection, PacketLoginStart packet, boolean[] recode) {
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
    /*public CommandHandler<ClientConnection> createCommandHandler() {
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
