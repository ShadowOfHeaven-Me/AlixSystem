package shadow.systems.login.captcha.manager;

import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.array.AlixLoopDeque;
import org.bukkit.Bukkit;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.captcha.manager.generator.CaptchaGenerator;
import shadow.utils.main.AlixUtils;

public final class CaptchaPoolManager {

    //generate a little bit more than max players to lessen the chance of a captcha ever repeating because of
    public static final int maxSize = (int) (Bukkit.getMaxPlayers() * AlixUtils.getRandom(1.05, 1.1)) + 1;
    private final AlixLoopDeque<Captcha> deque = AlixLoopDeque.concurrentOfSize(maxSize);
    //private final AlixDeque<Captcha> deque = new ConcurrentAlixDeque<>();

    public CaptchaPoolManager() {
        //pre-generating
        AlixScheduler.async(() -> {
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