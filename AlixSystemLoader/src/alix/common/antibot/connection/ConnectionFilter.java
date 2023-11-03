package alix.common.antibot.connection;

public interface ConnectionFilter {

    boolean disallowJoin(String address, String name);

    String getReason();

}