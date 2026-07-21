package alix.common.data.security.email;

import alix.common.AlixCommonMain;
import alix.common.data.PersistentUserData;
import alix.common.data.settings.ServerSettingsManager;
import alix.common.data.settings.Setting;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCache;
import alix.common.utils.AlixCommonUtils;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public final class EmailHandler {

    //caller -> code
    private static final Map<Object, EmailVerificationSession> VERIFY_CODES = AlixCache.newBuilder().maximumSize(512).<Object, EmailVerificationSession>build().asMap();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$");

    public static <T> void sendVerifyMail(T caller, String email, boolean console, BiConsumer<T, String> sendMessage) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            sendMessage.accept(caller, Messages.get("verify-mail.invalid-email"));
            return;
        }

        var verifyCode = AlixCommonUtils.generateCode(6);
        VERIFY_CODES.put(caller, new EmailVerificationSession(verifyCode, email));

        sendMessage.accept(caller, Messages.get("verify-mail.requesting-send"));
        sendEmail(email, Messages.get("verify-mail.email-subject"), Messages.get("verify-mail.email-body",
                console ? "/as verifymail " + verifyCode : "/verifyemail " + verifyCode)).whenComplete((v, ex) -> {
            if (ex != null) {
                sendMessage.accept(caller, Messages.get("verify-mail.send-failed"));
                return;
            }
            sendMessage.accept(caller, Messages.get("verify-mail.sent-successfully"));
        });
    }

    public static <T> void verifyMail(T caller, PersistentUserData data, String code, boolean console, BiConsumer<T, String> sendMessage) {
        var session = VERIFY_CODES.get(caller);
        if (session == null) {
            sendMessage.accept(caller, Messages.get("verify-mail.send-first", console ? "/as sendverifymail" : "/sendverifymail"));
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
                AlixCommonMain.logWarning("Could not send email: " + e.getMessage());
                future.completeExceptionally(e);
            } catch (Exception e) {
                future.completeExceptionally(e);
                throw e;
            }
        });
        return future;
    }

    static void sendEmail0(String email, String subject, String content) throws EmailException {
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
                mail.setSSLOnConnect(false);
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

        mail.setHtmlMsg(content);

        Thread.currentThread().setContextClassLoader(Session.class.getClassLoader());

        mail.send();
    }

    static {
        init();
    }

    @SneakyThrows
    static void init() {
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
