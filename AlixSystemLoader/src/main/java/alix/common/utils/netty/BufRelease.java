package alix.common.utils.netty;

import io.netty.buffer.ByteBuf;

import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface BufRelease extends Function<ByteBuf, Boolean>, Consumer<ByteBuf> {

    //null-safe release methods
    BufRelease CONST = BufRelease::releaseConst;
    BufRelease DYNAMIC = BufRelease::release;

    @Override
    default void accept(ByteBuf buf) {
        this.apply(buf);
    }

    static BufRelease of(boolean isConst) {
        return isConst ? CONST : DYNAMIC;
    }

    /*static boolean safeRelease(ByteBuf buf) {
        if (buf != null) return false;
    }*/

    static boolean release(ByteBuf buf) {
        return buf != null && buf.release();
    }

    static boolean releaseConst(ByteBuf buf) {
        return buf != null && release(buf.unwrap());
    }
}