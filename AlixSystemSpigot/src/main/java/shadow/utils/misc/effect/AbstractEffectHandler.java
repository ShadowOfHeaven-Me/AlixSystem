package shadow.utils.misc.effect;

import com.google.common.base.Function;
import io.netty.util.internal.PlatformDependent;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.types.UnverifiedUser;

abstract class AbstractEffectHandler implements PotionEffectHandler {

    static PotionEffectHandler newHandler(UnverifiedUser user) {
        return FuncStorage.FUNC.apply(user);
    }

    //prevent a possible class loading deadlock by using an inner class
    private static final class FuncStorage {

        private static final Function<UnverifiedUser, PotionEffectHandler> FUNC;

        static {
            Function<UnverifiedUser, PotionEffectHandler> func;

            try {
                UnsafeEffectHandler.init();//check if it's available
                func = UnsafeEffectHandler::new;//use the faster handler if possible
                Main.logInfo("Using the optimized UnsafeEffectHandler for potion effect management");
            } catch (Throwable e) {
                func = APIEffectHandler::new;//use the slower handler using the spigot API as fallback
                Main.logInfo("Using the unoptimized APIEffectHandler for potion effect management");
                if (AlixUtils.isDebugEnabled) {
                    Main.logInfo("Send this to the developer: " + PlatformDependent.hasUnsafe());
                    e.printStackTrace();
                } else Main.logInfo("If you wish to use the faster implementation, enable 'debug' in config.yml, and contact the developer");
            }
            FUNC = func;
        }

        static void init() {
        }
    }

    static void init() {
        FuncStorage.init();
    }
}