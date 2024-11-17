package shadow.systems.prelogin;

public enum PreLoginVerdict {

    ALLOWED_CHECK_PREMIUM,//allowed, but a premium check needs to be performed
    ALLOWED_CRACKED,//allowed, cracked
    DISALLOWED//disallowed, already closed the connection

}