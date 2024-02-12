package alix.common.antibot.firewall.os;

import alix.firewall.FireWallOS;

import java.rmi.Remote;

final class OsFWDelegate {

    private final FireWallOS delegate;

    OsFWDelegate(Remote obj) {
        this.delegate = (FireWallOS) obj;
    }

    void blacklist(String ip) throws Throwable {
        this.delegate.blacklist(ip);
    }

    void pardon(String ip) throws Throwable {
        this.delegate.pardon(ip);
    }
}