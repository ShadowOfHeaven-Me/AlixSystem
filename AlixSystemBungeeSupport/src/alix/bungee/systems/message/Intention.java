package alix.bungee.systems.message;

enum Intention {

    INFORM_BACKEND_DATA,
    INFORM_PROXY_SUCCESSFUL_REGISTER,
    INFORM_PROXY_SUCCESSFUL_LOGIN;

    byte getIntentionId() {
        return (byte) this.ordinal();
    }
}