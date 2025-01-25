package shadow.virtualization;

import alix.common.connection.filters.AntiVPN;
import alix.common.connection.filters.ConnectionManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumStatus;
import alix.common.data.premium.name.PremiumNameManager;
import alix.common.utils.AlixCache;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.google.common.cache.Cache;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import shadow.systems.login.autoin.PremiumSetting;
import shadow.systems.login.autoin.PremiumUtils;
import shadow.systems.login.autoin.premium.ClientPublicKey;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.netty.AlixChannelHandler;
import shadow.systems.netty.AlixInterceptor;
import shadow.virtualization.commands.LimboCommandHandler;
import ua.nanit.limbo.commands.CommandHandler;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.VarIntFrameDecoder;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.integration.PreLoginResult;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.protocol.packets.login.disconnect.PacketLoginDisconnect;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//https://wiki.vg/Protocol_FAQ

public final class LimboServerIntegration implements LimboIntegration<LimboConnection> {

    private static final Map<String, InetAddress> completedCaptchaCache;
    //private static final Map<InetAddress, String> ;

    static {
        Cache<String, InetAddress> cache = AlixCache.newBuilder().expireAfterWrite(20, TimeUnit.SECONDS).build();
        completedCaptchaCache = cache.asMap();
    }

    @Override
    public void setHasCompletedCaptcha(InetAddress address, String name) {
        completedCaptchaCache.put(name, address);
    }

    /*public static boolean hasCompletedCaptcha(InetAddress address) {
        return completedCaptchaCache.get(address) != null;
    }

    public static boolean hasCompletedCaptcha(Channel channel) {
        return getCompletedCaptchaName(channel) != null;
    }*/

    @Nullable
    public static boolean hasCompletedCaptcha(Channel channel, String name) {
        return hasCompletedCaptcha(((InetSocketAddress) channel.remoteAddress()).getAddress(), name);
    }

    @Nullable
    public static boolean hasCompletedCaptcha(InetAddress address, String name) {
       return address.equals(completedCaptchaCache.get(name));
    }

    /*@Nullable
    public static String removeCompletedCaptchaName(InetAddress address) {
        return completedCaptchaCache.remove(address);
    }*/

    //@Override
    public void invokeSilentServerChannelRead(Channel channel) {
        AlixInterceptor.invokeSilentChannelRead(channel);
    }

    @Override
    public LimboConnection newConnection(Channel channel, LimboServer server, PacketDuplexHandler duplexHandler, VarIntFrameDecoder frameDecoder) {
        return new LimboConnection(channel, server, duplexHandler, frameDecoder);
    }

    @Override
    public boolean hasCompletedCaptcha(String name, Channel channel) {
        return Captcha.hasCompletedCaptcha(name, channel);// || ((InetSocketAddress) channel.remoteAddress()).getAddress().equals(this.completedCaptchaCache.getIfPresent(name));
    }

    @Override
    public void completeCaptcha(LimboConnection conn) {
    }

    @Override
    public void onHandshake(LimboConnection connection, PacketHandshake handshake) {
    }

    private static final PacketSnapshot
            //alreadyConnectingPacket = PacketPlayOutDisconnect.snapshot(Messages.get("already-connecting")),
            invalidNamePacket = PacketLoginDisconnect.snapshot(AlixChannelHandler.invalidNameMessage),
            preventFirstTimeJoinPacket = PacketLoginDisconnect.snapshot(ConnectionManager.preventFirstTimeJoinMessage),
            maxTotalAccountsPacket = PacketLoginDisconnect.snapshot(GeoIPTracker.maxAccountsReached),
            vpnDetectedPacket = PacketLoginDisconnect.snapshot(AntiVPN.antiVpnMessage);

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
    public PreLoginResult onLoginStart(LimboConnection connection, PacketLoginStart packet, boolean[] recode) {
        String name = packet.getUsername();
        Channel channel = connection.getChannel();
        PersistentUserData data = UserFileManager.get(name);
        UUID uuid = packet.getUUID();

        String prefixedName = name;

        boolean isPremium;
        if (Bukkit.getServer().getOnlineMode()) isPremium = true;
        else {
            ClientVersion version = connection.getClientVersion().getRetrooperVersion().toClientVersion();
            ClientPublicKey publicKey = ClientPublicKey.createKey(packet.getSignatureData());
            PremiumStatus suggestsStatus = PremiumUtils.suggestsStatus(uuid, publicKey, version);

            /*PremiumData premiumData = PremiumUtils.getOrRequestAndCacheData(data, name);
            //the player can be premium if: either the nickname is premium or we couldn't determine if the nickname is premium
            boolean canBePremium = premiumData.getStatus().isPremium() || premiumData.getStatus().isUnknown();*/

            isPremium = PremiumSetting.requirePremium(PersistentUserData.isRegistered(data), suggestsStatus) && PremiumUtils.getOrRequestAndCacheData(data, name).getStatus().isPremium();
            prefixedName = PremiumUtils.getPrefixedName(name, isPremium);

            //Main.debug("isPremium: " + isPremium + " suggestsStatus: " + suggestsStatus);
            //PersistentUserData prefixedNameData = UserFileManager.get(prefixedName);

            boolean shouldReEncodeName = false;

            //data of the account <name>
            if (data != null) {
                //both: the joining player and the existing data of the account <name> have been determined to have the same isPremium
                // - just assume we can connect the player normally
                if (data.getPremiumData().getStatus().isPremium() == isPremium) {
                    //nothing to do here
                } else {
                    //their isPremium was determined to be different - recode the packet to the prefixed name,
                    // as that should be the correct player (can't tell of pre 1.19)
                    shouldReEncodeName = true;
                }
            } else {//when data of account <name> was null
                //get the data of the <name> account, since the data of the account <name>
                // could have been erased after we made the <prefixed name> account
                PersistentUserData prefixedNameData = UserFileManager.get(prefixedName);

                //we were right - the <prefixed name> account of predicted premium status exists, so again
                //this *should* be our player (gotta guess tho)
                if (prefixedNameData != null) {
                    shouldReEncodeName = true;
                }//else if the data of the accounts <name> & <prefixed name> is erased,
                // the player could always be logged in as <name> (since that nickname would
                // now be free, the player could get stuck with the <name> account, even if
                // he was previously on the <prefixed name> account
                //the exact same thing could happen to the original owner of the <name> account
            }


            boolean enabled = false;
            if (enabled && shouldReEncodeName) {
                recode[0] = true;
                packet.wrapper().setUsername(prefixedName);

                //special case:
                // map a premium player's nickname that is 16 characters long,
                // when a cracked player has made an account with the premium player's nickname
                // (we cannot simply substring(1) from the nickname to get the original,
                // since the minecraft protocol only allows nicknames up to 16 characters)
                if (name.length() >= 16)
                    PremiumNameManager.mapOverflow(prefixedName, name);
                name = prefixedName;
            }
        }

        switch (AlixChannelHandler.getPreLoginVerdict(channel, name, prefixedName, data, isPremium)) {
            case ALLOWED:
                break;
            case DISALLOWED_INVALID_NAME:
                connection.sendPacketAndClose(invalidNamePacket);
                return PreLoginResult.DISCONNECTED;
            case DISALLOWED_MAX_ACCOUNTS_REACHED:
                connection.sendPacketAndClose(maxTotalAccountsPacket);
                return PreLoginResult.DISCONNECTED;
            case DISALLOWED_PREVENT_FIRST_JOIN:
                connection.sendPacketAndClose(preventFirstTimeJoinPacket);
                return PreLoginResult.DISCONNECTED;
            case DISALLOWED_VPN_DETECTED:
                connection.sendPacketAndClose(vpnDetectedPacket);
                return PreLoginResult.DISCONNECTED;
            default:
                throw new AlixError();
        }

        return data != null || isPremium || hasCompletedCaptcha(channel, name) ? PreLoginResult.CONNECT_TO_MAIN_SERVER : PreLoginResult.CONNECT_TO_LIMBO;
    }

    //@Override
    public CommandHandler<LimboConnection> createCommandHandler() {
        return new LimboCommandHandler();
    }

    //@Override
    public String getServerIP() {
        return Bukkit.getServer().getIp();
    }

    //@Override
    public int getPort() {
        return Bukkit.getPort();
    }
}
