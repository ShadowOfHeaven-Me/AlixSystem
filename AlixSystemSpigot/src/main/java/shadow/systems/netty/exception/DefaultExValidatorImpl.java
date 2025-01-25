package shadow.systems.netty.exception;

import io.netty.handler.codec.DecoderException;

final class DefaultExValidatorImpl implements ExceptionValidator {

    DefaultExValidatorImpl() {
    }

    @Override
    public boolean isInvalid(Throwable ex) {
        return ex instanceof DecoderException;
    }
}