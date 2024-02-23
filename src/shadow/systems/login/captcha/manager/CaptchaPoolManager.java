package shadow.systems.login.captcha.manager;

import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.array.LoopDeque;
import org.bukkit.Bukkit;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.captcha.manager.generator.CaptchaGenerator;

import java.util.concurrent.CompletableFuture;

public final class CaptchaPoolManager {

    //generate a little bit more than max players to lessen the chance of a captcha ever repeating because of
    // /\ Deprecated description
    public static final int maxSize = Math.max(Bukkit.getMaxPlayers() >> 5, 10);//(int) (Bukkit.getMaxPlayers() * AlixUtils.getRandom(1.05, 1.1)) + 1;
    private final LoopDeque<CompletableFuture<Captcha>> deque = LoopDeque.concurrentOfSize(maxSize);
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

    public final CompletableFuture<Captcha> next() {
        return this.deque.getAndReplaceWith(CaptchaGenerator.generateCaptchaFuture());
    }

    public final void addNew() {
        this.deque.offerNext(CaptchaGenerator.generateCaptchaFuture());
    }
}