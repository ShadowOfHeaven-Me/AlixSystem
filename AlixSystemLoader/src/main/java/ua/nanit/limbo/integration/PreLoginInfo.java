package ua.nanit.limbo.integration;

public record PreLoginInfo(PreLoginResult result, boolean recode, StateSupplier supplier) {

    public static final PreLoginInfo EMPTY_DISCONNECTED = new PreLoginInfo(PreLoginResult.DISCONNECTED, false, null);
    public static final PreLoginInfo MAIN_SERVER_NO_RECODE = new PreLoginInfo(PreLoginResult.CONNECT_TO_MAIN_SERVER, false, null);

}