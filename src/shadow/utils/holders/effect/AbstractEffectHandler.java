package shadow.utils.holders.effect;

import com.google.common.base.Function;
import shadow.utils.users.types.UnverifiedUser;

abstract class AbstractEffectHandler implements PotionEffectHandler {

    static PotionEffectHandler newHandler(UnverifiedUser user) {
        return FuncStorage.FUNC.apply(user);
    }

    //prevent a possible class loading deadlock by using an inner class
    private static final class FuncStorage {

        private static final Function<UnverifiedUser, PotionEffectHandler> FUNC;

        static {
            /*Function<UnverifiedUser, PotionEffectHandler> func;

            try {
                UnsafeEffectHandler.init();//check if it's available
                func = UnsafeEffectHandler::new;//use the faster handler if possible
            } catch (Throwable e) {
                func = APIEffectHandler::new;//use the slower handler using the spigot API as fallback
            }*/
            FUNC = UnsafeEffectHandler::new;
        }
    }
}