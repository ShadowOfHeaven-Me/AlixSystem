package ua.nanit.limbo.connection.pipeline.compression;

import java.util.zip.Deflater;

enum CompressionLevel {

    DEFAULT(-1, -1),
    BEST_SPEED(Deflater.BEST_SPEED, 1),
    BEST_COMPRESSION(Deflater.BEST_COMPRESSION, 12);

    private final int javaLevel;
    private final int velocityLevel;

    CompressionLevel(int javaLevel, int velocityLevel) {
        this.javaLevel = javaLevel;
        this.velocityLevel = velocityLevel;
    }

    int getJavaLevel() {
        return javaLevel;
    }

    int getVelocityLevel() {
        return velocityLevel;
    }
}