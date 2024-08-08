package shadow.systems.login.captcha.manager;

import alix.common.scheduler.runnables.futures.AlixFuture;
import alix.common.utils.collections.queue.array.LoopDeque;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.captcha.manager.generator.CaptchaGenerator;

public final class CaptchaPoolManager {

    //generate 3 captchas
    public static final int maxSize = 3;//Math.min(Math.max(Bukkit.getMaxPlayers() >> 6, 10), 20);//(int) (Bukkit.getMaxPlayers() * AlixUtils.getRandom(1.05, 1.1)) + 1;
    private final LoopDeque<AlixFuture<Captcha>> deque = LoopDeque.ofSize(maxSize);
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
        this.deque.forEachNonNull(future -> future.whenCompleted(Captcha::release));
    }

    public AlixFuture<Captcha> poll() {
        return this.deque.getNextAndReplace(CaptchaGenerator.generateCaptchaFuture());
    }

    private void addNew() {
        this.deque.offerNext(CaptchaGenerator.generateCaptchaFuture());
    }
}