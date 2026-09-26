package alix.common.data.security.email;

import alix.common.messages.Messages;
import alix.common.utils.config.alix.AlixYamlConfig;
import alix.common.utils.file.AlixFileManager;

public final class EmailConfig {

    public static final EmailConfig INSTANCE = new EmailConfig();
    private final AlixYamlConfig config;
    public final String host, username, password, email, sender;
    public final int port;
    //file name of a custom HTML verification-email template, relative to the plugin's "email-templates" folder; empty means the built-in default template is used
    public final String customVerifyEmailTemplate;
    //settings for the optional built-in webserver that lets players verify their email via a clickable link instead of typing a code in-game
    public final boolean enableWebVerification;
    public final String webVerificationBindAddress, webVerificationPublicUrl;
    public final int webVerificationPort, webVerificationTokenExpiryMinutes;
    //how long a 6-digit verification code stays valid for (previously never expired). Generous default since
    //console/"/as sendverifyemail" has no real time pressure.
    public final int verifyCodeExpiryMinutes;
    //title/message shown on each outcome page of the web verification link - see messages.properties'
    //"web-verification.*" keys, same as every other player-facing string. Rendered in a browser, so any
    //color codes in them are ignored.
    public final String webVerificationPageInvalidTitle, webVerificationPageInvalidMessage,
            webVerificationPageErrorTitle, webVerificationPageErrorNotFoundMessage, webVerificationPageErrorGenericMessage,
            webVerificationPageSuccessTitle, webVerificationPageSuccessMessage,
            webVerificationPageMethodNotAllowedTitle, webVerificationPageMethodNotAllowedMessage,
            webVerificationPageConfirmTitle, webVerificationPageConfirmMessage, webVerificationPageConfirmButton;

    EmailConfig() {
        var file = AlixFileManager.getOrCreatePluginFile("email-config.yml", AlixFileManager.FileType.CONFIG);
        this.config = new AlixYamlConfig(file);
        this.host = config.getString("host");
        this.username = config.getString("username");
        this.password = config.getString("password");
        this.email = config.getString("email");
        this.sender = config.getString("sender");
        this.port = config.getInt("port");
        this.customVerifyEmailTemplate = config.getString("custom-verify-email-template", "");
        this.enableWebVerification = config.getBoolean("enable-web-verification", false);
        this.webVerificationBindAddress = config.getString("web-verification-bind-address", "0.0.0.0");
        this.webVerificationPort = config.getInt("web-verification-port", 8091);
        this.webVerificationPublicUrl = config.getString("web-verification-public-url", "");
        this.webVerificationTokenExpiryMinutes = config.getInt("web-verification-token-expiry-minutes", 30);
        this.verifyCodeExpiryMinutes = config.getInt("verify-code-expiry-minutes", 60);
        this.webVerificationPageInvalidTitle = Messages.get("web-verification.invalid-title");
        this.webVerificationPageInvalidMessage = Messages.get("web-verification.invalid-message");
        this.webVerificationPageErrorTitle = Messages.get("web-verification.error-title");
        this.webVerificationPageErrorNotFoundMessage = Messages.get("web-verification.error-not-found-message");
        this.webVerificationPageErrorGenericMessage = Messages.get("web-verification.error-generic-message");
        this.webVerificationPageSuccessTitle = Messages.get("web-verification.success-title");
        this.webVerificationPageSuccessMessage = Messages.get("web-verification.success-message");
        this.webVerificationPageMethodNotAllowedTitle = Messages.get("web-verification.method-not-allowed-title");
        this.webVerificationPageMethodNotAllowedMessage = Messages.get("web-verification.method-not-allowed-message");
        this.webVerificationPageConfirmTitle = Messages.get("web-verification.confirm-title");
        this.webVerificationPageConfirmMessage = Messages.get("web-verification.confirm-message");
        this.webVerificationPageConfirmButton = Messages.get("web-verification.confirm-button");

        //Create the "email-templates" (+ "images") folder on startup rather than lazily - see
        //EmailTemplateLoader#ensureFoldersExist().
        EmailTemplateLoader.ensureFoldersExist();
    }

    public static void init() {
    }

    public static AlixYamlConfig getConfig() {
        return INSTANCE.config;
    }
}