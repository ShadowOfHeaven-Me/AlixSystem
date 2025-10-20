package shadow.systems.login.captcha.manager;

import alix.common.scheduler.runnables.futures.AlixFuture;
import alix.common.utils.collections.queue.ConcurrentAlixDeque;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.captcha.manager.generator.CaptchaGenerator;

import java.util.concurrent.atomic.LongAdder;

public final class CaptchaPoolManager {

    //generate 3 captchas
    public static final int maxSize = 3;//Math.min(Math.max(Bukkit.getMaxPlayers() >> 6, 10), 20);//(int) (Bukkit.getMaxPlayers() * AlixUtils.getRandom(1.05, 1.1)) + 1;
    //private final LoopDeque<AlixFuture<Captcha>> deque = LoopDeque.ofSize(maxSize);
    private final ConcurrentAlixDeque<AlixFuture<Captcha>> deque = new ConcurrentAlixDeque<>();
    private final LongAdder size = new LongAdder();
    //private final AlixDeque<Captcha> deque = new ConcurrentAlixDeque<>();

    public CaptchaPoolManager() {
        //pre-generating
        int size = maxSize;
        while (size-- != 0)
            this.addNew();
    }

    /*long nano = System.nanoTime();

    long n2 = System.nanoTime();
    Main.logError("Regenerated in: " + ((n2 - nano) / Math.pow(10, 6)) + " ms");*/

    public void uninjectAll() {
        this.deque.forEach(future -> future.whenCompleted(Captcha::release));
    }

    public AlixFuture<Captcha> poll() {
        //return this.deque.getNextAndReplace(CaptchaGenerator.generateCaptchaFuture());
        int size = (int) this.size.sum();
        if (size <= maxSize) this.deque.offerLast(CaptchaGenerator.generateCaptchaFuture());//generate a captcha future, to always keep maxSize or more futures
        else this.size.decrement();
        return this.deque.pollFirst();
    }

    public void recycle(AlixFuture<Captcha> captchaFuture) {
        captchaFuture.unassign();
        this.add0(captchaFuture);
    }

    private void addNew() {
        this.add0(CaptchaGenerator.generateCaptchaFuture());
    }

    private void add0(AlixFuture<Captcha> future) {
        this.deque.offerLast(future);
        this.size.increment();
    }
}