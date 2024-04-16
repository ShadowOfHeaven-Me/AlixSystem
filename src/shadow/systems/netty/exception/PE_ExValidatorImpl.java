package shadow.systems.netty.exception;

import io.netty.handler.codec.DecoderException;

final class PE_ExValidatorImpl implements ExceptionValidator {

    PE_ExValidatorImpl() {
    }

    @Override
    public boolean isInvalid(Throwable ex) {
        return ex instanceof DecoderException;
    }
}