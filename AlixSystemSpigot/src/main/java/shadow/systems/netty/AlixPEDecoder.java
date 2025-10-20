package shadow.systems.netty;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.common.utils.other.annotation.ScheduledForFix;
import com.github.retrooper.packetevents.exception.PacketProcessException;
import io.github.retrooper.packetevents.injector.handlers.PacketEventsDecoder;
import io.netty.channel.ChannelHandlerContext;
import shadow.Main;

import java.net.InetSocketAddress;

//Stacktrace filling can be heavy
//Remaster exception throwing with a PacketEvents pull request
@OptimizationCandidate
public final class AlixPEDecoder extends PacketEventsDecoder {

    //private final PacketEventsDecoder delegate;

    public AlixPEDecoder(PacketEventsDecoder delegate) {
        super(delegate);
    }

    @Override
    @ScheduledForFix
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (this.user.getClientVersion().isPreRelease()) {
            ctx.channel().close();
            return;
        }
        Main.logError("ERROR: " + cause.getMessage());

        //cause.printStackTrace();
        if (isPPE(cause)) {
            //TODO: UHHHHHHHH
            FireWallManager.addCauseException((InetSocketAddress) ctx.channel().remoteAddress(), cause);
            ctx.channel().close();
            return;
        }
        super.exceptionCaught(ctx, cause);
    }

    private static boolean isPPE(Throwable t) {
        do {
            if (t instanceof PacketProcessException)
                return true;
        } while ((t = t.getCause()) != null);
        return false;
    }
}