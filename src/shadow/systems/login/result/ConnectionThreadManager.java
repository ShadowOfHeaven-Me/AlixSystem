package shadow.systems.login.result;

import alix.common.antibot.algorithms.connection.ConnectionAlgorithm;
import alix.common.antibot.algorithms.connection.types.JoinCounterAlgorithm;
import alix.common.antibot.algorithms.connection.types.Name2IPAlgorithm;
import alix.common.antibot.algorithms.ping.PingRequestAlgorithm;
import alix.common.antibot.algorithms.ping.types.TotalCounterPingAlgorithm;
import alix.common.scheduler.AlixScheduler;
import shadow.utils.main.AlixUtils;

import java.net.InetAddress;

public final class ConnectionThreadManager {

    private static final ConnectionThreadRunnable runnable = new ConnectionThreadRunnable();
    //private static long nextCollection;

    static {
        if (AlixUtils.antibotService) AlixScheduler.newAlixThread(runnable, 3000, "Connection Thread");
    }

    public static void onJoinAttempt(String name, InetAddress address) {
        for (ConnectionAlgorithm algorithm : runnable.connectionAlgorithms) algorithm.onJoinAttempt(name, address);
    }

    public static void onPingRequest(InetAddress address) {
        for (PingRequestAlgorithm algorithm : runnable.pingRequestAlgorithms) algorithm.onPingRequest(address);
    }

    //runnable.joins.offerLast(new JoinAttempt(name, address));
    //for (ConnectionAlgorithm a : runnable.algorithms) a.onJoinAttempt(name, address);


    private static final class ConnectionThreadRunnable implements Runnable {


        private final ConnectionAlgorithm[] connectionAlgorithms;
        private final PingRequestAlgorithm[] pingRequestAlgorithms;
        //private final AlixDeque<JoinAttempt> joins = new ConcurrentAlixDeque<>();
        //private final ConnectionListManager list = new ConnectionListManager();


        @Override
        public void run() {
            for (ConnectionAlgorithm a : connectionAlgorithms) a.onThreadRepeat();
            for (PingRequestAlgorithm a : pingRequestAlgorithms) a.onThreadRepeat();
        }

            /*if (!joins.isEmpty()) {
                AlixDeque.Node<JoinAttempt> first = joins.firstNode();
                joins.clear();
                AlixDeque.forEach(this::algorithmsJoinAttempt, first);
            }*/
        /*long now = System.currentTimeMillis();
            if (now < nextCollection) return;

            Map<String, LoginInfo> map = LoginVerdictManager.map;
            map.forEach((n, l) -> map.compute(n, (name, login) -> login.removalTime < now ? null : login));

            nextCollection = now + 70000;*/

        /*private void algorithmsJoinAttempt(JoinAttempt attempt) {
            for (ConnectionAlgorithm algorithm : algorithms) algorithm.onJoinAttempt(attempt.name, attempt.ip);
        }*/

        private ConnectionThreadRunnable() {
            this.connectionAlgorithms = new ConnectionAlgorithm[]{
                    //new RequestAmountAlgorithm(),
                    new Name2IPAlgorithm(),
                    new JoinCounterAlgorithm()
                    //new RequestAmountAlgorithm()
            };
            this.pingRequestAlgorithms = new PingRequestAlgorithm[]{
                    new TotalCounterPingAlgorithm()
            };
        }
    }

    /*private static final class JoinAttempt {

        private final String name, ip;

        private JoinAttempt(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }
    }*/

    private ConnectionThreadManager() {
    }

/*
    private static final class ConnectionListManager {

        private volatile JoinRequest first, last;

        private void add(String address) {
            JoinRequest req = new JoinRequest(address);
            synchronized (this) {
                if (first == null) this.first = this.last = req;
                else this.last = this.last.next = req;
            }
        }

        private boolean isEmpty() {
            return first == null;
        }


        private ConnectionListManager() {
        }

    }

    private static final class JoinRequest {

        private final String address;
        private final long time;
        private volatile JoinRequest next;

        private JoinRequest(String address) {
            this.address = address;
            this.time = System.currentTimeMillis();
        }
    }*/
}