/*
package alix.bungee.systems.message.redirector;

import alix.common.utils.other.throwable.AlixException;
import com.google.common.io.ByteArrayDataInput;
import net.md_5.bungee.api.connection.ProxiedPlayer;

enum RedirectorIntention {

    INFORM_BACKEND_CAPTCHA_AND_REGISTER,
    INFORM_BACKEND_REGISTER,
    INFORM_BACKEND_LOGIN,
    INFORM_PROXY_SUCCESSFUL_REGISTER(RedirectorInterpreter::onSuccessfulRegister),
    INFORM_PROXY_SUCCESSFUL_LOGIN;

    private final MessageReader reader;

    byte getIntentionId() {
        return (byte) this.ordinal();
    }

    RedirectorIntention() {
        this.reader = (p, i) -> {
            throw new AlixException("Intention " + this.name() + " is not readable!");
        };
    }

    RedirectorIntention(MessageReader reader) {
        this.reader = reader;
    }

    void read(ProxiedPlayer player, ByteArrayDataInput input) {
        this.reader.read(player, input);
    }

    @FunctionalInterface
    private interface MessageReader {

        void read(ProxiedPlayer player, ByteArrayDataInput input);

    }
}*/
