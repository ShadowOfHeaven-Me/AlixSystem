package ua.nanit.limbo.integration;

public record PreLoginInfo(PreLoginResult result, boolean recode, StateSupplier supplier) {

    public static final PreLoginInfo EMPTY_DISCONNECTED = new PreLoginInfo(PreLoginResult.DISCONNECTED, false, null);

}