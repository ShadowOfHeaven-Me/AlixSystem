package ua.nanit.limbo.protocol.snapshot;

public enum SnapshotEncodeStrategy {

    PREGENERATE,//pre-generate all
    RUNTIME_CACHE,//generate when needed and cache from now on
    NO_CACHE//never cache anything - encode all at runtime

}