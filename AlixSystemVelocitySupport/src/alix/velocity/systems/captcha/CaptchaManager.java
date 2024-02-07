package alix.velocity.systems.captcha;

import alix.common.utils.collections.queue.AlixDeque;
import alix.common.utils.collections.queue.ConcurrentAlixDeque;
import alix.velocity.systems.captcha.impl.MapCaptcha;

import java.util.Random;

public final class CaptchaManager {

    private final AlixDeque<Captcha> pool;
    private final char[] generationChars;
    private final Random random;
    private final int captchaLength;
    private final CaptchaGenerator generator;
    private final CaptchaThread captchaThread;

    public CaptchaManager() {
        this.pool = new ConcurrentAlixDeque<>();
        this.generationChars = "0123456789@ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghjkmnopqrstuvwxy".toCharArray();//no I, i, l cuz they're fucking unrecognizable
        this.random = new Random();
        this.captchaLength = 5;
        this.generator = constructGenerator0();
        this.captchaThread = new CaptchaThread(this.pool, this);
        for (int i = 0; i < 40; i++) generateToPool();
    }

    private String randomCaptchaAnswer() {
        char[] c = new char[captchaLength];
        for (int i = 0; i < captchaLength; i++) c[i] = generationChars[random.nextInt(generationChars.length)];
        return new String(c);
    }

    public void generateToPool() {
        this.pool.offerLast(this.generateCaptcha());
    }

    Captcha generateCaptcha() {
        return this.generator.generate(randomCaptchaAnswer());
    }

    public Captcha pollNextCaptcha() {
        this.captchaThread.incrementCaptchasToRegen();
        return this.pool.pollFirst();
    }

    private interface CaptchaGenerator {

        Captcha generate(String answer);

    }

    private static final class MapCaptchaGenerator implements CaptchaGenerator {

        @Override
        public Captcha generate(String answer) {
            return new MapCaptcha(answer);
        }
    }

    private static CaptchaGenerator constructGenerator0() {
        return new MapCaptchaGenerator();
    }
}