package shadow.systems.login.captcha.manager;

import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.array.AlixLoopArrayDeque;
import org.bukkit.Bukkit;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.main.AlixUtils;

public final class CaptchaPoolManager {

    public static final int maxSize = (int) (Bukkit.getMaxPlayers() * AlixUtils.getRandom(1.1, 1.2));
    private final AlixLoopArrayDeque<Captcha> deque = AlixLoopArrayDeque.concurrentOfSize(maxSize);
    //private final AlixDeque<Captcha> deque = new ConcurrentAlixDeque<>();

    public CaptchaPoolManager() {
        //pre-generating
        AlixScheduler.async(() -> {
            //generate a little bit more than the max player count in order to lower the chance of the captchas ever running out at runtime, before they have the time to be re-generated
            int size = maxSize;
            while (size-- != 0)
                this.addNew();
        });
    }

    /*long nano = System.nanoTime();

    long n2 = System.nanoTime();
    Main.logError("Regenerated in: " + ((n2 - nano) / Math.pow(10, 6)) + " ms");*/

    public final Captcha next() {
        return this.deque.nextInLine();
    }

    public final void addNew() {
        this.deque.offerNext(CaptchaGenerator.generateCaptcha());
    }
}