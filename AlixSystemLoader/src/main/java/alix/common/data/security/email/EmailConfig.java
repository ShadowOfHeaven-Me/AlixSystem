package alix.common.data.security.email;

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
    //how long a 6-digit verification code (sent by sendVerifyMail()/sendRecoveryMail() - covers email
    //registration, account recovery, a player's own "/account verifyemail", and the server's own "/as
    //verifyemail") stays valid for. Previously these never expired at all (bounded only by a 512-entry LRU
    //cache and the attempt limit) - generous by default since the console/"/as sendverifyemail" flow in
    //particular has no real time pressure and shouldn't need redoing just because an admin took a while to
    //check their inbox.
    public final int verifyCodeExpiryMinutes;
    //the plain-text (no color codes - this renders in a browser, not in-game) title/message shown on each
    //outcome page of the web verification link, configurable since they're seen by a player's browser and
    //an operator may want to reword/rebrand them
    public final String webVerificationPageInvalidTitle, webVerificationPageInvalidMessage,
            webVerificationPageErrorTitle, webVerificationPageErrorNotFoundMessage, webVerificationPageErrorGenericMessage,
            webVerificationPageSuccessTitle, webVerificationPageSuccessMessage,
            webVerificationPageMethodNotAllowedTitle, webVerificationPageMethodNotAllowedMessage;

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
        this.webVerificationPageInvalidTitle = config.getString("web-verification-page-invalid-title", "Link invalid or expired");
        this.webVerificationPageInvalidMessage = config.getString("web-verification-page-invalid-message", "This verification link is no longer valid. Please request a new one in-game via /account sendverifyemail.");
        this.webVerificationPageErrorTitle = config.getString("web-verification-page-error-title", "Something went wrong");
        this.webVerificationPageErrorNotFoundMessage = config.getString("web-verification-page-error-not-found-message", "Your account could not be found or the email could not be saved. Please try again in-game.");
        this.webVerificationPageErrorGenericMessage = config.getString("web-verification-page-error-generic-message", "Please try again in-game.");
        this.webVerificationPageSuccessTitle = config.getString("web-verification-page-success-title", "Email verified!");
        this.webVerificationPageSuccessMessage = config.getString("web-verification-page-success-message", "Your email has been successfully verified. You can now close this page.");
        this.webVerificationPageMethodNotAllowedTitle = config.getString("web-verification-page-method-not-allowed-title", "Method not allowed");
        this.webVerificationPageMethodNotAllowedMessage = config.getString("web-verification-page-method-not-allowed-message", "Only GET requests are supported.");

        //Create the "email-templates" (+ "images") folder right away rather than only as a side effect of
        //actually sending a custom-templated email - see EmailTemplateLoader#ensureFoldersExist() for why
        //that lazy behavior meant the folder could simply never appear for an operator.
        EmailTemplateLoader.ensureFoldersExist();
    }

    public static void init() {
    }

    public static AlixYamlConfig getConfig() {
        return INSTANCE.config;
    }
}