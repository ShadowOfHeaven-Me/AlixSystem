package shadow.utils.misc.effect;

import alix.common.utils.other.AlixUnsafe;
import com.google.common.base.Function;
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
            FUNC = AlixUnsafe.hasUnsafe() ? unsafeFunc0() : apiFunc0();
        }

        private static Function<UnverifiedUser, PotionEffectHandler> apiFunc0() {
            Main.logInfo("Using APIEffectHandler for potion effect management");
            return APIEffectHandler::new;//use the slower handler using the spigot API as fallback
        }

        private static Function<UnverifiedUser, PotionEffectHandler> unsafeFunc0() {
            Function<UnverifiedUser, PotionEffectHandler> func;
            try {
                UnsafeEffectHandler.init();//check if it's available
                func = UnsafeEffectHandler::new;//use the faster handler if possible
                Main.logInfo("Using the optimized UnsafeEffectHandler for potion effect management");
            } catch (Throwable e) {
                func = APIEffectHandler::new;//use the slower handler using the spigot API as fallback
                Main.logInfo("Using the unoptimized APIEffectHandler for potion effect management");

                if (AlixUtils.isDebugEnabled) {
                    Main.logInfo("Send this to the developer: " + AlixUnsafe.hasUnsafe());
                    e.printStackTrace();
                } else
                    Main.logInfo("If you wish to use the faster implementation, enable 'debug' in config.yml, and contact the developer");
            }
            return func;
        }


        static void init() {
        }
    }

    static void init() {
        FuncStorage.init();
    }
}