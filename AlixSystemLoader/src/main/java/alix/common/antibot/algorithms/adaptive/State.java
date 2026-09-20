package alix.common.antibot.algorithms.adaptive;

public enum State {

    NORMAL, ELEVATED, ATTACK;

    public boolean isWorseThan(State state) {
        return this.ordinal() > state.ordinal();
    }

    public State worst(State state) {
        return this.ordinal() > state.ordinal() ? this : state;
    }
}