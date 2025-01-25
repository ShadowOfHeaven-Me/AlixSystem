package alix.common.data.security.password.matcher;

import alix.common.data.security.password.Password;

public interface PasswordMatcher {

    boolean matches(Password password, String unhashedInput);

    byte matcherId();

}