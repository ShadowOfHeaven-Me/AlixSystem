package alix.common.connection.filters;

import java.net.InetAddress;

public interface ConnectionFilter {

    boolean disallowJoin(InetAddress ip, String strAddress, String name);

    String getReason();

}