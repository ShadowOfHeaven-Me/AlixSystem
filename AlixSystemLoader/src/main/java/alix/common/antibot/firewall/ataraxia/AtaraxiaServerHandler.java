package alix.common.antibot.firewall.ataraxia;

import alix.common.AlixCommonMain;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.connection.timestamp.ClockSkew;
import alix.common.utils.netty.BufUtils;
import alix.common.utils.other.throwable.AlixError;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ByteToMessageDecoder.Cumulator;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static alix.common.antibot.firewall.ataraxia.AtaraxiaProtocol.*;

@Sharable
final class AtaraxiaServerHandler extends ChannelInboundHandlerAdapter {

    static final AtaraxiaServerHandler HANDLER = new AtaraxiaServerHandler();

    //perfect
    //still can `this.state = null;` without a warning
    @NonNull
    @NotNull
    @lombok.NonNull
    @org.checkerframework.checker.nullness.qual.NonNull
    @Nonnull
    private volatile ConnectionState state = ConnectionState.DISCONNECTED;
    volatile Channel channel;
    private long totalBytesRead = 0;

    static ByteBuf buffer() {
        var ch = HANDLER.channel;
        return ch != null ? ch.alloc().buffer() : Unpooled.directBuffer();
    }

    static boolean isConnected() {
        return HANDLER.state != ConnectionState.DISCONNECTED;
    }

    public void write(ByteBuf buf) {
        this.channel.write(buf, this.channel.voidPromise());
    }

    public void flush() {
        this.channel.flush();
    }

    public void writeAndFlush(ByteBuf buf) {
        this.write(buf);
        this.flush();
    }

    ByteBuf cumulation;
    private final Cumulator cumulator = ByteToMessageDecoder.MERGE_CUMULATOR;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;

        //AlixCommonMain.logWarning("Received " + in.readableBytes() + " bytes");
        totalBytesRead += in.readableBytes();

        if (this.cumulation == null) this.cumulation = in;
        else this.cumulation = this.cumulator.cumulate(BufUtils.UNPOOLED, this.cumulation, in);

        while (this.decode(this.cumulation)) ;

        if (!this.cumulation.isReadable())
            this.releaseCumulation();
        else
            this.cumulation.discardSomeReadBytes();
    }

    private void releaseCumulation() {
        if (this.cumulation == null)
            return;

        this.cumulation.release();
        this.cumulation = null;
    }

    private boolean decode(ByteBuf buf) {
        if (buf.readableBytes() < 4)
            return false;

        buf.markReaderIndex();
        int len = buf.readInt();//min of 1 cuz of intention
        this.validate(len > 0, "len <= 0: " + len);

        if (buf.readableBytes() < len) {
            buf.resetReaderIndex();
            return false;
        }

        int intention = buf.readByte();

        switch (intention) {
            case R2J_HANDSHAKE_REPLY -> {
                this.validateState(ConnectionState.PRE_HANDSHAKE);
                this.state = ConnectionState.POST_HANDSHAKE;

                int protocol = buf.readByte();
                if (protocol != AtaraxiaProtocol.PROTOCOL_VERSION) {
                    AlixCommonMain.logError("Java Alix and Ataraxia have mismatched protocols - ataraxia=" + protocol + " server=" + AtaraxiaProtocol.PROTOCOL_VERSION + "! Shutting off the connection!");

                    this.close();
                    return false;
                }

                AtaraxiaIPC.syncAll(this);
            }

            case R2J_UPDATE_MAP -> {
                this.validateState(ConnectionState.POST_HANDSHAKE);

                boolean isBlacklist = buf.readBoolean();
                boolean add = buf.readBoolean();
                int size = buf.readInt();

                List<InetAddress> list = new ArrayList<>(size);

                for (int i = 0; i < size; i++) {
                    list.add(readAddr(buf));
                }
                if (isBlacklist) {
                    if (add)
                        FireWallManager.addDynamic(list);
                    else
                        FireWallManager.removeDynamic(list);
                } else {
                    list.forEach(ip -> {
                        if (add)
                            GeoIPTracker.addExisting(ip, false);
                        else
                            GeoIPTracker.removeIP(ip);
                    });
                }
            }
            case R2J_SND_TS -> {
                long nanos = buf.readLong();
                long tsval = Integer.toUnsignedLong(buf.readInt());
                long tsecr = Integer.toUnsignedLong(buf.readInt());
                int addr = buf.readInt();
                int port = Short.toUnsignedInt(buf.readShort());
                buf.readShort();//padding

                ClockSkew.on_timestamp(nanos, addr, port, tsval);

                /*try {
                    AlixCommonMain.logInfo("tsval=" + tsval + " tsecr=" + tsecr + " addr=" + InetAddress.getByAddress(IPUtils.ipv4ByteArray(addr)).getHostAddress() + " port=" + port);
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }*/
            }
            default -> {
                this.error("Unknown intention: " + intention);
            }
        }
        return true;
    }

    private void validateState(ConnectionState state) {
        this.validate(this.state == state, "Expected: " + state + " has " + this.state);
    }

    private void validate(boolean b, String s) {
        if (!b)
            this.error(s);
    }

    private void error(String s) {
        this.close();
        throw new AlixError(s);
    }

    public void close() {
        this.channel.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        AlixCommonMain.logInfo("Ataraxia connected");
        this.validateState(ConnectionState.DISCONNECTED);

        this.channel = ctx.channel();
        this.state = ConnectionState.PRE_HANDSHAKE;

        this.writeAndFlush(AtaraxiaProtocol.encodeJ2RHandshake());

        this.channel.closeFuture().addListener(f -> this.onClose());
    }

    void onClose() {
        AlixCommonMain.logInfo(String.format("Ataraxia disconnected. Total data transferred: %.2f MB (%d bytes)%n", totalBytesRead / 1e6f, totalBytesRead));
        this.channel = null;
        this.releaseCumulation();
        this.state = ConnectionState.DISCONNECTED;
        this.totalBytesRead = 0;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}