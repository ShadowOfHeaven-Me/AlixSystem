package shadow.systems.login.captcha.manager;

import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.AlixDeque;
import alix.common.utils.collections.queue.ConcurrentAlixDeque;
import org.bukkit.Bukkit;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.main.AlixUtils;

public final class CaptchaPoolManager {

    public static final int maxSize = (int) (Bukkit.getMaxPlayers() * AlixUtils.getRandom(1.1, 1.2));
    private final AlixDeque<Captcha> deque = new ConcurrentAlixDeque<>();

    public CaptchaPoolManager() {
        //pre-generating
        AlixScheduler.async(() -> {
            //generate a little bit more than the max player count in order to lower the chance of the captchas ever running out at runtime, before they have the time to be re-generated
            int size = maxSize;// + 64 + AlixUtils.random.nextInt(64);
            while (size-- != 0)
                this.add(CaptchaGenerator.generateCaptcha());
        });
    }

    public final Captcha next() {
        Captcha captcha = this.deque.pollFirst();

        return captcha != null ? captcha : CaptchaGenerator.generateCaptcha();//in case the max capacity was somehow reached
    }

    public final void add(Captcha captcha) {
        deque.offerLast(captcha);
    }

    public final void addNode(AlixDeque.Node<Captcha> node) {
        this.deque.addNodeLast(node);
    }

    public final int size() {
        return this.deque.size();
    }

    public final void clear() {
        this.deque.clear();
    }
}