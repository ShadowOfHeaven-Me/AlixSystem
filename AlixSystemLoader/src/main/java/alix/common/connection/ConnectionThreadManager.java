package alix.common.connection;

import alix.common.antibot.algorithms.any.ConnectRequestAlgoImpl;
import alix.common.antibot.algorithms.connection.ConnectionAlgorithm;
import alix.common.antibot.algorithms.connection.types.JoinCounterAlgorithm;
import alix.common.antibot.algorithms.connection.types.Name2IPAlgorithm;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.config.ConfigProvider;

import java.net.InetAddress;

public final class ConnectionThreadManager {

    public static final long TICK_MILLIS_DELAY = 3000L;
    private static final ConnectionThreadRunnable runnable = new ConnectionThreadRunnable();
    //private static long nextCollection;

    static {
        if (ConfigProvider.config.getBoolean("antibot-service")) AlixScheduler.newAlixThread(runnable, TICK_MILLIS_DELAY, "Connection Thread");
    }

    //Algorithm IDs:

    //B2 - Too many (different) names from the same ip tried to connect in a relatively short amount of time
    // (counted by violations, which decrease with time, mainly aimed at fast or moderately fast attacks)

    //C1 - More than 12 (different) names tried to connect from the same ip,
    // with each new name increasing the amount of time the whole entry will be removed in
    // (mainly aimed to sum up the entirety of the recent activity from the suspicious ip
    // works for attacks of any speed, but exists for the purpose of stopping slow attacks)

    //D2 - More than 200 pings occurred, with 20 pings being removed every 3 seconds

    //E1 - An invalid name was sent (either too long or even the buffer itself threw an exception when reading)

    //Decoder Exception: An exception occurred during join, and was caused by an invalid packet from the client

    public static void onJoinAttempt(String name, InetAddress address) {
        for (ConnectionAlgorithm algorithm : runnable.connectionAlgorithms) algorithm.onJoinAttempt(name, address);
    }

    public static void onConnection(InetAddress address) {
        ConnectRequestAlgoImpl.onConnection(address);
        //for (ConnectionRequestAlgorithm algorithm : runnable.pingRequestAlgorithms) algorithm.onConnection(address);
    }

    //runnable.joins.offerLast(new JoinAttempt(name, address));
    //for (ConnectionAlgorithm a : runnable.algorithms) a.onJoinAttempt(name, address);


    private static final class ConnectionThreadRunnable implements Runnable {

        private final ConnectionAlgorithm[] connectionAlgorithms;
        //private final ConnectionRequestAlgorithm[] pingRequestAlgorithms;
        //private final AlixDeque<JoinAttempt> joins = new ConcurrentAlixDeque<>();
        //private final ConnectionListManager list = new ConnectionListManager();


        @Override
        public void run() {
            for (ConnectionAlgorithm a : connectionAlgorithms) a.onThreadRepeat();
            //for (ConnectionRequestAlgorithm a : pingRequestAlgorithms) a.onThreadRepeat();
            //if (ConnectionManager.isEnabled) ConnectionManager.tick();
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
            /*this.pingRequestAlgorithms = new ConnectionRequestAlgorithm[]{
                    //new TotalCounterPingAlgorithm()
            };*/
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