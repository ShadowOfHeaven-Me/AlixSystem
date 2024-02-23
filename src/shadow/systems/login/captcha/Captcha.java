package shadow.systems.login.captcha;

import shadow.Main;
import shadow.systems.login.captcha.manager.CaptchaPoolManager;
import shadow.systems.login.captcha.manager.VerificationThreadManager;
import shadow.systems.login.captcha.manager.generator.CaptchaGenerator;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.concurrent.CompletableFuture;

import static shadow.utils.main.AlixUtils.captchaVerificationCaseSensitive;

public abstract class Captcha {

    private static final CaptchaPoolManager captchaPool = AlixUtils.requireCaptchaVerification ? new CaptchaPoolManager() : null;
    protected final String captcha;

    protected Captcha() {
        this.captcha = CaptchaGenerator.generateTextCaptcha(); //this::regenerate cannot be used because of initialization issues
    }

    public static void pregenerate() {
        VerificationThreadManager.initialize();
    }

    public static void sendInitMessage() {
        Main.logInfo(AlixUtils.isPluginLanguageEnglish ?
                "Pre-generated the captcha pool with the size " + CaptchaPoolManager.maxSize + "."
                : "Wygenerowano przedwcześnie captcha w ilości " + CaptchaPoolManager.maxSize + ".");
    }

/*    public static int currentPoolSize() {
        return captchaPool.size();
    }*/

    public void sendPackets(UnverifiedUser user) {
    }

    public void onCompletion(UnverifiedUser user) {
    }

    public final boolean isCorrect(String s) {
        return captchaVerificationCaseSensitive ? captcha.equals(s) : captcha.equalsIgnoreCase(s);
    }

    protected Captcha inject(UnverifiedUser user) {
        return this;
    }

    //private static final Object errorKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase("§cSomething went wrong");

    public static CompletableFuture<Captcha> assignCaptcha(UnverifiedUser u) {
        return captchaPool.next().thenApply(c -> c.inject(u));
    }

/*    public static void unregister() {
        registered = false;
        captchaPool.clear();
    }*/

/*    @Override
    protected final void finalize() throws Throwable {
        if (registered) captchaPool.add(copy(this));
        super.finalize();
    }*/

/*    private static Captcha copy(Captcha captcha) {
        if (captcha instanceof MapCaptcha) return new MapCaptcha((MapCaptcha) captcha);
        if (captcha instanceof SubtitleCaptcha) return new SubtitleCaptcha(captcha);
        if (captcha instanceof MessageCaptcha) return new MessageCaptcha(captcha);
        registered = false;
        throw new InternalError("Unknown captcha class type: " + captcha.getClass().getSimpleName() + "!");
    }*/

    /*    protected static int parse(String a) throws NumberFormatException {
        char[] b = a.toCharArray();
        int c = 0;
        for (char value : b) {
            int d = value - 48;
            if (d < 0 || d > 9)
                throw new NumberFormatException();
            c = c * 10 + d;
        }
        return c;
    }*/

    /*    protected static String nextTextCaptcha() {//48 - 57, 65 - 90, 97 - 122
        char[] c = new char[4];
        for (byte i = 0; i < c.length; i++) {
            if (i % 2 == 0) {                           //57 - 48
                c[i] = (char) (JavaUtils.random.nextInt(9) + 48);
                continue;
            }
            if (i % 3 == 0) {                           //90 - 65
                c[i] = (char) (JavaUtils.random.nextInt(25) + 65);
                continue;
            }                                      //122 - 97
            c[i] = (char) (JavaUtils.random.nextInt(25) + 97);
        }
        return new String(c);
    }*/

}