package alix.common.login.prelogin;

public enum PreLoginVerdict {

    ALLOWED,
    DISALLOWED_INVALID_NAME,
    DISALLOWED_PREVENT_FIRST_JOIN,
    DISALLOWED_MAX_ACCOUNTS_REACHED,
    DISALLOWED_VPN_DETECTED

}