package alix.api.event.types;

import alix.api.user.AlixCommonUser;

import java.util.concurrent.ExecutorService;

public class UserAuthenticateEvent extends AbstractAlixEvent {

    public UserAuthenticateEvent(AlixCommonUser user, ExecutorService executor, ThreadSource source) {
        super(user, executor, source);
    }
}