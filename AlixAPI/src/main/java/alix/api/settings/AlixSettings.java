package alix.api.settings;

public final class AlixSettings {

    private boolean invokeStdEventListeners = true;

    public boolean invokeStdEventListeners() {
        return invokeStdEventListeners;
    }

    public void setInvokeStdEventListeners(boolean invokeStdEventListeners) {
        this.invokeStdEventListeners = invokeStdEventListeners;
    }
}