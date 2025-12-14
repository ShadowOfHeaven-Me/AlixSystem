package alix.api.event.types;

import alix.api.event.AlixEvent;

public interface UserAuthenticateEvent extends AlixEvent {

    AuthReason getAuthReason();

}