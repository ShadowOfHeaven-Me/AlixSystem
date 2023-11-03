package shadow.systems.login.autoin;

import java.util.UUID;

public interface LoginAuthenticator {

    boolean isPremium(UUID uuid);

}