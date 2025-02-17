package alix.api.event.types;

import alix.api.event.EventManager;
import alix.api.user.AlixCommonUser;

import java.util.concurrent.ExecutorService;

public class UserPostLoginEvent extends AbstractAlixEvent {

    private final boolean isPremium;

    public UserPostLoginEvent(boolean isPremium, AlixCommonUser user, ExecutorService executor, ThreadSource source) {
        super(user, executor, source);
        this.isPremium = isPremium;
    }

    /**
     * Returns whether the joining user was determined and verified to be premium
     **/
    public boolean isPremium() {
        return this.isPremium;
    }

    public static void callEvent(UserPostLoginEvent event) {
        EventManager.callOnPostLogin(event);
    }
}