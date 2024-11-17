package shadow.systems.netty.exception;

import alix.libs.com.github.retrooper.packetevents.exception.PacketProcessException;
import io.netty.handler.codec.DecoderException;

final class PE_ExValidatorImpl implements ExceptionValidator {

    PE_ExValidatorImpl() {
    }

    @Override
    public boolean isInvalid(Throwable ex) {
        return ex instanceof DecoderException || ex instanceof PacketProcessException;
    }
}