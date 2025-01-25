package shadow.utils.misc.effect;

import shadow.utils.users.types.UnverifiedUser;

public interface PotionEffectHandler {

    void resetEffects();

    void returnEffects();

    static PotionEffectHandler newHandlerFor(UnverifiedUser user) {
        return AbstractEffectHandler.newHandler(user);
    }

    static void init() {
        AbstractEffectHandler.init();
    }
}