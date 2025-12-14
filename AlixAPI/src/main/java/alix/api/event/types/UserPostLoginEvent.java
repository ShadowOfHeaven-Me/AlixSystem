package alix.api.event.types;

import alix.api.event.AlixEvent;

public interface UserPostLoginEvent extends AlixEvent {

    /**
     * Returns whether the joining user was determined and verified to be premium
     **/
    boolean isPremium();
}