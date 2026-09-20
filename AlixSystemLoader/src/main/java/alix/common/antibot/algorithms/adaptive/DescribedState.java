package alix.common.antibot.algorithms.adaptive;

record DescribedState(State state, String description) {

    public DescribedState worst(DescribedState state) {
        return this.state.isWorseThan(state.state) ? this : state;
    }
}