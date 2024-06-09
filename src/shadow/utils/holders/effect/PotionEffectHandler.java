package shadow.utils.holders.effect;

import shadow.utils.users.types.UnverifiedUser;

public interface PotionEffectHandler {

    void resetEffects();

    void returnEffects();

    static PotionEffectHandler newHandlerFor(UnverifiedUser user) {
        return AbstractEffectHandler.newHandler(user);
    }

}