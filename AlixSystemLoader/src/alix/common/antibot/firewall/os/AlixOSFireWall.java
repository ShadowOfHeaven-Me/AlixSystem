package alix.common.antibot.firewall.os;

import alix.common.AlixCommonMain;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public final class AlixOSFireWall {

    public static final AlixOSFireWall INSTANCE;
    private final OsFWDelegate delegate;

    static {
        AlixOSFireWall i = new AlixOSFireWall();
        if (i.delegate == null) i = null;
        INSTANCE = i;
    }

    private AlixOSFireWall() {
        OsFWDelegate delegate = null;
        try {
            Remote obj = Naming.lookup("rmi://localhost:1099/alix_firewall");
            delegate = new OsFWDelegate(obj);
        } catch (NotBoundException e) {
            AlixCommonMain.logInfo("NAME NOT BOUND");
            //e.printStackTrace();
        } catch (MalformedURLException | RemoteException e) {
            //e.printStackTrace();
        }
        this.delegate = delegate;
    }

    public void blacklist(String ip) throws Throwable {
        this.delegate.blacklist(ip);
    }

    public void pardon(String ip) throws Throwable {
        this.delegate.pardon(ip);
    }
}