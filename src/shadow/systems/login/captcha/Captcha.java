package shadow.systems.login.captcha;

import alix.common.utils.collections.queue.AlixDeque;
import shadow.Main;
import shadow.systems.login.captcha.manager.CaptchaGenerator;
import shadow.systems.login.captcha.manager.CaptchaPoolManager;
import shadow.systems.login.captcha.manager.CaptchaThreadManager;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.offline.UnverifiedUser;

import static shadow.utils.main.AlixUtils.captchaVerificationCaseSensitive;

public abstract class Captcha {

    private static final CaptchaPoolManager captchaPool = AlixUtils.requireCaptchaVerification ? new CaptchaPoolManager() : null;
    //private static boolean registered = true;
    protected transient UnverifiedUser user;
    protected String captcha;

    protected Captcha() {
        this.captcha = CaptchaGenerator.generateTextCaptcha(); //this::regenerate cannot be used because of initialization issues
    }

    protected Captcha(Captcha captcha) {
        this.captcha = captcha.captcha;
    }

    public static void pregenerate() {
        CaptchaThreadManager.pregenerate();
    }

    public static void sendInitMessage() {
        Main.logInfo(AlixUtils.isPluginLanguageEnglish ?
                "Pre-generated the captcha pool with the size " + captchaPool.size() + "."
                : "Wygenerowano przedwcześnie captcha w ilości " + captchaPool.size() + ".");
    }

/*    public static int currentPoolSize() {
        return captchaPool.size();
    }*/

    public void sendPackets() {
    }

    public void regenerate() {
        this.captcha = CaptchaGenerator.generateTextCaptcha();
    }

    public final boolean isCorrect(String s) {
        return captchaVerificationCaseSensitive ? captcha.equals(s) : captcha.equalsIgnoreCase(s);
    }

    protected Captcha inject(UnverifiedUser user) {
        this.user = user;
        return this;
    }

    public void uninject() {
        this.user = null;
        CaptchaThreadManager.regenerateCaptcha(this);
    }

    public static Captcha nextCaptcha(UnverifiedUser u) {
        return captchaPool.next().inject(u);
    }

    public static void addToPool(AlixDeque.Node<Captcha> node) {
        captchaPool.addNode(node);
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