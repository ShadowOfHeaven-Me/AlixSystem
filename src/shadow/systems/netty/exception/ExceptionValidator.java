package shadow.systems.netty.exception;

public interface ExceptionValidator {

    boolean isInvalid(Throwable ex);

    static ExceptionValidator createImpl() {
        return new PE_ExValidatorImpl();
    }
}