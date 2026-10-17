package alix.common.connection.timestamp;

public record ClockSkewValue(double skew, ClockSkewConfidence confidence) {
}
