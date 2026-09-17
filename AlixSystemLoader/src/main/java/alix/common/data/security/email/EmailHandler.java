package alix.common.data.security.email;

import alix.common.AlixCommonMain;
import alix.common.data.PersistentUserData;
import alix.common.data.settings.ServerSettingsManager;
import alix.common.data.settings.Setting;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCache;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.config.ConfigParams;
import com.sun.mail.handlers.message_rfc822;
import com.sun.mail.handlers.multipart_mixed;
import com.sun.mail.handlers.text_html;
import com.sun.mail.handlers.text_plain;
import com.sun.mail.imap.IMAPProvider;
import com.sun.mail.imap.IMAPSSLProvider;
import com.sun.mail.smtp.SMTPProvider;
import com.sun.mail.smtp.SMTPSSLProvider;
import lombok.SneakyThrows;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Session;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EmailHandler {

    //caller -> code. expireAfterWrite is generous (see EmailConfig#verifyCodeExpiryMinutes) since this cache
    //is shared by every verification-code flow - email registration, account recovery, a player's own
    //"/account verifyemail", and the server's own "/as sendverifyemail" - and the last of those in particular
    //has no real time pressure.
    private static final Map<Object, EmailVerificationSession> VERIFY_CODES = AlixCache.<Object, EmailVerificationSession>newBuilder()
            .maximumSize(512)
            .expireAfterWrite(Math.max(1, EmailConfig.INSTANCE.verifyCodeExpiryMinutes), TimeUnit.MINUTES)
            .build().asMap();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    //Kept for existing call sites that don't have/need a player name (e.g. the console/server-email-verify flow, which isn't tied to any player account) - equivalent to passing playerName=null below, meaning no clickable web-verification link is included.
    public static <T> void sendVerifyMail(T caller, String email, boolean console, BiConsumer<T, String> sendMessage) {
        sendVerifyMail(caller, null, email, console, sendMessage);
    }

    //Same as above, but with the player's name, allowing a clickable web-verification link (see WebVerificationServer) to be
    //included in the email alongside the code - only meaningful when console=false (there is no "player" for the server's own
    //outgoing-address verification). Pass null for playerName if unavailable/not applicable.
    public static <T> void sendVerifyMail(T caller, String playerName, String email, boolean console, BiConsumer<T, String> sendMessage) {
        if (!isValidEmail(email)) {
            sendMessage.accept(caller, Messages.get("verify-mail.invalid-email"));
            return;
        }

        var verifyCode = AlixCommonUtils.generateCode(6);
        VERIFY_CODES.put(caller, new EmailVerificationSession(verifyCode, email));

        sendMessage.accept(caller, Messages.get("verify-mail.requesting-send"));
        sendEmail(email, Messages.get("verify-mail.email-subject"), buildVerifyEmailBody(verifyCode, playerName, email, console)).whenComplete((v, ex) -> {
            if (ex != null) {
                sendMessage.accept(caller, Messages.get("verify-mail.send-failed"));
                return;
            }
            sendMessage.accept(caller, Messages.get("verify-mail.sent-successfully"));
        });
    }

    //builds the HTML body of the verification email, using a custom operator-provided template (if configured) instead of the built-in default
    private static String buildVerifyEmailBody(String verifyCode, String playerName, String email, boolean console) {
        String command = console ? "/as verifyemail " + verifyCode : "/account verifyemail " + verifyCode;
        //only meaningful for a real player's own email, not the server's own outgoing-address verification (console=true)
        String link = (!console && playerName != null)
                ? WebVerificationServer.createVerificationLink(playerName, email).orElse("")
                : "";

        String customTemplate = EmailConfig.INSTANCE.customVerifyEmailTemplate;

        if (customTemplate != null && !customTemplate.isBlank()) {
            var loaded = EmailTemplateLoader.load(customTemplate);
            if (loaded.isPresent())
                return loaded.get().replace("{code}", verifyCode).replace("{command}", command).replace("{verification_link}", link);
            //falls through to the default template below if the custom one could not be loaded
        }

        String body = Messages.get("verify-mail.email-body", command);
        if (!link.isEmpty()) body += "<br><br>" + Messages.get("verify-mail.email-body-link", link);
        return body;
    }

    public static <T> void sendRecoveryMail(T caller, String email, BiConsumer<T, String> sendMessage) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            sendMessage.accept(caller, Messages.getWithPrefix("verify-mail.invalid-email"));
            return;
        }

        var verifyCode = AlixCommonUtils.generateCode(6);
        VERIFY_CODES.put(caller, new EmailVerificationSession(verifyCode, email));

        sendMessage.accept(caller, Messages.getWithPrefix("verify-mail.requesting-send"));
        sendEmail(email, Messages.get("email-recovery-subject"), Messages.get("email-recovery-body", verifyCode)).whenComplete((v, ex) -> {
            //AlixCommonMain.logInfo("CALLED " + ex);
            if (ex != null) {
                sendMessage.accept(caller, Messages.getWithPrefix("verify-mail.send-failed"));
                return;
            }
            try {
                sendMessage.accept(caller, Messages.getWithPrefix("email-recovery-code-sent"));
            } catch (Exception e) {
                AlixCommonUtils.logException(e);
            }
        });
    }

    public static <T> boolean hasSession(T caller) {
        return VERIFY_CODES.containsKey(caller);
    }

    public static <T> boolean verifyRecoveryCode(T caller, String code) {
        return verifyCode(caller, code);
    }

    //Generic "does this code match the caller's pending session" check, consuming the session either way it
    //resolves valid/matched - used both by account recovery (verifyRecoveryCode() above) and by
    //LoginState.handleRegisterVerifyEmailCommand() for 'require-email-in-register' registrations. Deliberately
    //independent of PersistentUserData/verifyMail() below, since neither caller has an account to attach the
    //result to at the point they call this - recovery doesn't need to (see EmailRecovery), and a pending
    //registration's account doesn't exist yet at all.
    public static <T> boolean verifyCode(T caller, String code) {
        var session = VERIFY_CODES.get(caller);
        if (session == null) return false;
        if (code.trim().equals(session.code())) {
            VERIFY_CODES.remove(caller);
            return true;
        }
        return false;
    }

    public static <T> void verifyMail(T caller, PersistentUserData data, String code, boolean console, BiConsumer<T, String> sendMessage) {
        var session = VERIFY_CODES.get(caller);
        if (session == null) {
            sendMessage.accept(caller, Messages.get("verify-mail.send-first", console ? "/as sendverifyemail" : "/account sendverifyemail"));
            return;
        }
        if (!code.equals(session.code())) {
            sendMessage.accept(caller, Messages.get("verify-mail.code-mismatch"));
            return;
        }

        if (console) {
            ServerSettingsManager.set(Setting.VERIFIED_EMAIL, true);
            sendMessage.accept(caller, Messages.get("verify-mail.server-success"));
            return;
        }

        if (data.setEmail(session.email())) {
            sendMessage.accept(caller, Messages.get("verify-mail.user-success"));
        } else {
            sendMessage.accept(caller, Messages.get("verify-mail.encryption-failed"));
        }
    }

    public static CompletableFuture<Void> sendEmail(String email, String subject, String content) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        AlixScheduler.asyncBlocking(() -> {
            try {
                sendEmail0(email, subject, content);
                future.complete(null);
            } catch (EmailException e) {
                //e.getMessage() alone is just "Sending the email to the following server failed : host:port" -
                //the actual reason (wrong password, connection refused, TLS handshake failure...) is the
                //cause, which was being silently swallowed, leaving no way to tell an auth failure apart
                //from a firewalled port from this log line alone.
                Throwable cause = e.getCause();
                AlixCommonMain.logWarning("Could not send email: " + e.getMessage()
                        + (cause != null ? " (" + cause + ")" : "")
                        + (!ConfigParams.isDebugEnabled ? " - enable 'debug' in config.yml for the full stacktrace" : ""));
                if (ConfigParams.isDebugEnabled) e.printStackTrace();
                future.completeExceptionally(e);
            } catch (Exception e) {
                future.completeExceptionally(e);
                throw e;
            }
        });
        return future;
    }

    private static void sendEmail0(String email, String subject, String content) throws EmailException {
        var config = EmailConfig.INSTANCE;
        var port = config.port;

        var mail = new HtmlEmail();

        mail.setCharset(EmailConstants.UTF_8);
        mail.setHostName(config.host);
        mail.setSmtpPort(port);
        mail.setSubject(subject);
        mail.setAuthentication(config.username, config.password);
        mail.addTo(email);
        mail.setFrom(config.email, config.sender);

        switch (port) {
            case 465 -> {
                mail.setSslSmtpPort(String.valueOf(port));
                mail.setSSLOnConnect(true);//why was it false?
            }
            case 587 -> {
                mail.setStartTLSEnabled(true);
                mail.setStartTLSRequired(true);
            }
            default -> {
                mail.setStartTLSEnabled(true);
                mail.setSSLOnConnect(true);
                mail.setSSLCheckServerIdentity(true);
            }
        }

        content = embedTemplateImages(mail, content);
        mail.setHtmlMsg(content);

        var loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Session.class.getClassLoader());

        mail.send();

        Thread.currentThread().setContextClassLoader(loader);
    }

    //matches 'cid:<filename>' references (e.g. <img src="cid:logo.png">), the way a custom HTML email template embeds its own images
    private static final Pattern CID_IMAGE_PATTERN = Pattern.compile("cid:([\\w.\\-]+)");

    //Embeds every image referenced via 'cid:<filename>' in the email body, sourced from this plugin's "email-templates/images"
    //folder, so a custom HTML template (see EmailTemplateLoader/EmailConfig#customVerifyEmailTemplate) can include a logo or any
    //other artwork, on top of ordinary remotely-hosted <img src="https://..."> images which already work without any of this.
    // Missing/unreadable files are left as broken references (non-fatal), with a warning logged.
    private static String embedTemplateImages(HtmlEmail mail, String content) {
        Matcher matcher = CID_IMAGE_PATTERN.matcher(content);
        Map<String, String> embeddedCids = new HashMap<>(); //file name -> generated Content-ID, avoids embedding the same file twice
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            String fileName = matcher.group(1);
            if (fileName.equals(".") || fileName.contains("..")) continue; //defensive - not a real filename, leave untouched

            String cid = embeddedCids.get(fileName);
            if (cid == null) {
                var imageFile = EmailTemplateLoader.loadImageFile(fileName);
                if (imageFile.isEmpty()) {
                    AlixCommonMain.logWarning("Custom email template references image 'cid:" + fileName + "', but '" + fileName + "' was not found in the 'email-templates/images' folder!");
                    continue;
                }
                try {
                    cid = mail.embed(imageFile.get(), fileName);
                    embeddedCids.put(fileName, cid);
                } catch (EmailException e) {
                    AlixCommonMain.logWarning("Could not embed image '" + fileName + "': " + e.getMessage());
                    continue;
                }
            }

            result.append(content, lastEnd, matcher.start()).append("cid:").append(cid);
            lastEnd = matcher.end();
        }
        result.append(content, lastEnd, content.length());
        return result.toString();
    }

    static {
        init();
    }

    @SneakyThrows
    private static void init() {
        var lookup = MethodHandles.lookup();
        //I hate this
        lookup.ensureInitialized(IMAPSSLProvider.class);
        lookup.ensureInitialized(IMAPProvider.class);
        lookup.ensureInitialized(SMTPProvider.class);
        lookup.ensureInitialized(SMTPSSLProvider.class);

        lookup.ensureInitialized(text_html.class);
        lookup.ensureInitialized(text_plain.class);
        lookup.ensureInitialized(multipart_mixed.class);
        lookup.ensureInitialized(message_rfc822.class);

        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }
}
