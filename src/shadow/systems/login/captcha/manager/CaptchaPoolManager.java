package shadow.systems.login.captcha.manager;

import alix.common.utils.collections.queue.AlixDeque;
import alix.common.utils.collections.queue.ConcurrentAlixDeque;
import org.bukkit.Bukkit;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.main.AlixUtils;

public final class CaptchaPoolManager {

    private final AlixDeque<Captcha> deque = new AlixDeque<>();
    private final int size;

    public CaptchaPoolManager() {
        //generate a little bit more than the max player count in order to lower the chance of
        int size = (int) (Bukkit.getMaxPlayers() * AlixUtils.getRandom(1.1, 1.2));// + 64 + AlixUtils.random.nextInt(64);
        this.size = size;
        //pre-generating

        while (size-- != 0) {
            this.add(CaptchaGenerator.generateCaptcha());
        }
    }

/*    public final void addAll(Captcha[] captchas) {

    }*/

    //TODO:
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
        return size;
    }

    public final void clear() {
        this.deque.clear();
    }
}