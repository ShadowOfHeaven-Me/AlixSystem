package alix.common.data.security.password.matcher;

import alix.common.data.security.password.Password;
import alix.common.data.security.password.hashing.HashingAlgorithm;

public interface PasswordMatcher {

    boolean matches(Password password, String unhashedInput);

    String hash(String input, HashingAlgorithm algo, String salt);

    byte matcherId();

}