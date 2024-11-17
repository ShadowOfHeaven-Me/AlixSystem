package shadow.utils.misc.packet.custom;

import alix.libs.com.github.retrooper.packetevents.util.Vector3d;

import java.util.HashSet;
import java.util.Set;

public final class PositionHashCompressor {

    private static final int INITIAL_CAPACITY = 1 << 10;//1024 (there's usually 500-1000 buffers generated)
    private final Set<Long> set = new HashSet<>(INITIAL_CAPACITY);//capacity needs to be a power of 2

    //Returns true if can add, false if can't
    public boolean add(Vector3d v) {
        long hash = hash(v.x, v.y, v.z);
        return this.set.add(hash);
    }

    //My own hashing method
    private static long hash(double x, double y, double z) {
        double multiplier = 5;//10 changes nothing, while 5 removes about half
        long iX = (long) (x * multiplier);
        long iY = (long) (y * multiplier);
        long iZ = (long) (z * multiplier);
        return (iX << 32) + (iY << 16) + iZ;
    }

    //Thanks ImIllusion ^^

    ///**
    // * Hashes the given coordinates into a long. The hash is "lossy", and uses its own loss to determine if two coordinates are close enough.
    // * Allocate more bits to xIndex, yIndex and zIndex if you want more decimal precision, but keep in mind that the hash is only 64 bits long.
    // *
    // * @param x X coordinate
    // * @param y Y coordinate
    // * @param z Z coordinate
    // * @return Hashed coordinates
    //  */
/*    private static long hash(double x, double y, double z) {
        int xPos = (int) x;
        int yPos = (int) y;
        int zPos = (int) z;

        int shift = 0xF;//15

        int xIndex = (int) ((x - xPos) * shift);
        int yIndex = (int) ((y - yPos) * shift);
        int zIndex = (int) ((z - zPos) * shift);

        // 22 bits for xPos, zPos
        // 8 bits for yPos
        // 4 bits for xIndex
        // 4 bits for zIndex
        // 4 bits for yIndex

        long hash = 0;

        hash |= xPos & 0x3FFFFF;
        hash |= (long) (zPos & 0x3FFFFF) << 22;
        hash |= (long) (yPos & 0xFF) << 44;
        hash |= (long) (xIndex & shift) << 52;
        hash |= (long) (zIndex & shift) << 56;
        hash |= (long) (yIndex & shift) << 60;

        return hash;
    }*/
}