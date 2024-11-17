package alix.velocity.systems.captcha;

import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.ConcurrentAlixDeque;

import java.util.concurrent.atomic.AtomicInteger;

final class CaptchaThread implements Runnable {

    private final ConcurrentAlixDeque<Captcha> pool;
    private final CaptchaManager captchaManager;
    private final AtomicInteger captchasToRegen;

    CaptchaThread(ConcurrentAlixDeque<Captcha> pool, CaptchaManager captchaManager) {
        this.pool = pool;
        this.captchaManager = captchaManager;
        this.captchasToRegen = new AtomicInteger();
        AlixScheduler.newAlixThread(this, 100, "Captcha Thread");
    }

    @Override
    public void run() {
        if (this.captchasToRegen.get() != 0) {
            int toRegen = captchasToRegen.getAndSet(0);
            while (toRegen-- != 0) pool.offerLast(captchaManager.generateCaptcha());
        }
    }

    void incrementCaptchasToRegen() {
        this.captchasToRegen.getAndIncrement();
    }
}