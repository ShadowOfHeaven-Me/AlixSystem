package alix.common.antibot.firewall;

public final class AlixOSFireWall {

    //Disabled for now
    public static final AlixOSFireWall INSTANCE = null;
    public static final boolean isOsFireWallInUse = INSTANCE != null;
    //private final FireWallOS delegate;

    /*static {
        FireWallOS delegate = null;
        try {
            delegate = (FireWallOS) Naming.lookup("rmi://localhost:1099/alix_firewall");
        } catch (NotBoundException e) {
            *//*AlixCommonMain.logInfo("NAME NOT BOUND");
            e.printStackTrace();*//*
        } catch (Exception e) {
            //e.printStackTrace();
        }
        INSTANCE = delegate != null ? new AlixOSFireWall(delegate) : null;
        isOsFireWallInUse = INSTANCE != null;
    }*/

    /*private AlixOSFireWall(FireWallOS delegate) {
        this.delegate = delegate;
    }*/

    void blacklist(String ip) throws Throwable {
        //this.delegate.blacklist(ip);
    }

    void pardon(String ip) throws Throwable {
        //this.delegate.pardon(ip);
    }
}