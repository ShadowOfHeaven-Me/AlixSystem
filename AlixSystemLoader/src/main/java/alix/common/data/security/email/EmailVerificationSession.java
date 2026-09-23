package alix.common.data.security.email;

import java.util.concurrent.atomic.AtomicInteger;

record EmailVerificationSession(String code, String email, AtomicInteger wrongAttempts) {
    EmailVerificationSession(String code, String email) {
        this(code, email, new AtomicInteger());
    }
}
