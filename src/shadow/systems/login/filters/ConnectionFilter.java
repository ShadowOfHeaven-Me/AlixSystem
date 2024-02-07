package shadow.systems.login.filters;

public interface ConnectionFilter {

    boolean disallowJoin(String address, String name);

    String getReason();

}