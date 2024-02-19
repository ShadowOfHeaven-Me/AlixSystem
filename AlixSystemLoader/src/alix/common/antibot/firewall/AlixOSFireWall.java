package alix.common.antibot.firewall;

import alix.common.AlixCommonMain;
import alix.firewall.FireWallOS;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

final class AlixOSFireWall {

    public static final AlixOSFireWall INSTANCE;
    private final FireWallOS delegate;

    static {
        FireWallOS delegate = null;
        try {
            delegate = (FireWallOS) Naming.lookup("rmi://localhost:1099/alix_firewall");
        } catch (NotBoundException e) {
            AlixCommonMain.logInfo("NAME NOT BOUND");
            e.printStackTrace();
        } catch (MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }
        INSTANCE = delegate != null ? new AlixOSFireWall(delegate) : null;
    }

    private AlixOSFireWall(FireWallOS delegate) {
        this.delegate = delegate;
    }

    void blacklist(String ip) throws Throwable {
        this.delegate.blacklist(ip);
    }

    void pardon(String ip) throws Throwable {
        this.delegate.pardon(ip);
    }
}