package shadow.systems.login.result;

import alix.common.antibot.connection.algorithms.ConnectionAlgorithm;
import alix.common.antibot.connection.algorithms.types.NameToIPAlgorithm;
import alix.common.antibot.connection.algorithms.types.RequestAmountAlgorithm;
import alix.common.scheduler.impl.AlixScheduler;

import java.util.Map;

public final class ConnectionThreadManager {

    private static final ConnectionThreadRunnable runnable = new ConnectionThreadRunnable();
    private static long nextCollection;

    static {
        AlixScheduler.newAlixThread(runnable, 3000, "Connection Thread");
    }

    public static void addJoinAttempt(String name, String address) {
        for (ConnectionAlgorithm a : runnable.algorithms) a.onJoinAttempt(name, address);
    }

    private static final class ConnectionThreadRunnable implements Runnable {

        private final ConnectionAlgorithm[] algorithms;
        //private final ConnectionListManager list = new ConnectionListManager();

        @Override
        public void run() {
            for (ConnectionAlgorithm a : algorithms) a.onThreadRepeat();

            long now = System.currentTimeMillis();
            if (now < nextCollection) return;

            Map<String, LoginInfo> map = LoginVerdictManager.map;
            map.forEach((n, l) -> map.compute(n, (name, login) -> login.removalTime < now ? null : login));

            nextCollection = now + 70000;
        }

        private ConnectionThreadRunnable() {
            this.algorithms = new ConnectionAlgorithm[]{
                    new RequestAmountAlgorithm(),
                    new NameToIPAlgorithm()
            };
        }
    }

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