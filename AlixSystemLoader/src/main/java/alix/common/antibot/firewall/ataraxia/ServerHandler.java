package alix.common.antibot.firewall.ataraxia;

import alix.common.AlixCommonMain;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.filters.GeoIPTracker;
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
final class ServerHandler extends ChannelInboundHandlerAdapter {

    static final ServerHandler INSTANCE = new ServerHandler();

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
        return Unpooled.directBuffer();
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
        int len = buf.readInt();
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
                    AlixCommonMain.logError("Server Alix and Ataraxia have mismatched protocols - ataraxia=" + protocol + " server=" + AtaraxiaProtocol.PROTOCOL_VERSION + "! Shutting off the connection!");

                    this.channel.close();
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
        }
        return true;
    }

    private void validateState(ConnectionState state) {
        if (this.state == state)
            return;

        throw new AlixError("Expected: " + state + " has " + this.state);

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.validateState(ConnectionState.DISCONNECTED);

        this.channel = ctx.channel();
        this.state = ConnectionState.PRE_HANDSHAKE;

        this.writeAndFlush(AtaraxiaProtocol.encodeJ2RHandshake());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.printf("Received EOF. Total data transferred: %.2f MB (%d bytes)%n",
                totalBytesRead / (1024.0 * 1024.0), totalBytesRead);
        this.channel = null;
        this.releaseCumulation();
        this.state = ConnectionState.DISCONNECTED;
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