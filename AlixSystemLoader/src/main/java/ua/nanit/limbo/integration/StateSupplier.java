package ua.nanit.limbo.integration;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.connection.captcha.CaptchaState;
import ua.nanit.limbo.connection.login.LoginState;

import java.util.function.Function;

@FunctionalInterface
public interface StateSupplier extends Function<ClientConnection, VerifyState> {

    StateSupplier CAPTCHA = CaptchaState::new;
    StateSupplier LOGIN = LoginState::new;

}