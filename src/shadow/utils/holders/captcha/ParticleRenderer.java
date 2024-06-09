package shadow.utils.holders.captcha;

import alix.common.antibot.captcha.ColorGenerator;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import io.netty.buffer.ByteBuf;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.VisibleForTesting;
import shadow.utils.holders.packet.custom.ParticleHashCompressor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.world.AlixWorld;
import shadow.utils.world.ConstLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public final class ParticleRenderer {

    private static final ConstLocation CENTER = new ConstLocation(AlixWorld.TELEPORT_LOCATION.asModifiableCopy().add(0, -4, -4));
    private static final Quaternion ROTATION = new Quaternion(new Vec3f(1, 0, 0),45);
    private static final Vector3f OFFSET = new Vector3f(0, 0, 0);

    //Source code: https://github.com/whileSam/bukkit-image-renderer/blob/master/src/main/java/me/trysam/imagerenderer/particle/ImageRenderer.java

    public static ByteBuf[] captchaRenderingBuffers(BufferedImage image) {
        //The scaling factor determines how dense the particles should be together (the higher the denominator, the less the space between the particles/pixels)
        float scalingFactor = 1f / 5.5f;//było 1/8f
        boolean far = false;
        ParticleHashCompressor compressor = new ParticleHashCompressor();

        //Iterates through all the pixels
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {

                float px = ((float) x * scalingFactor) - ((128 * scalingFactor) / 2);
                float py = 0;
                float pz = ((float) y * scalingFactor) - ((128 * scalingFactor) / 2);

                //If there is some rotation assigned, rotate the px, py, and pz coordinates by the given quaternion.
                if (ROTATION != null) {
                    Quaternion point = new Quaternion(0, px, py, pz);
                    Quaternion rotated = ROTATION.multiplied(point).multiplied(ROTATION.getInverse());
                    px = rotated.getX();
                    py = rotated.getY();
                    pz = rotated.getZ();
                }

                //Set the renderer's location to the center + the calculated pixel coordinates.
                //TODO: Threadsafe location modification.
                Vector3d rendererLoc = new Vector3d(CENTER.getX() + px, CENTER.getY() + py, CENTER.getZ() + pz);

                Color color = new Color(image.getRGB(x, y), true);

                //If there is some transparency in the pixel, go to the next pixel if there is some.
                if (color.getAlpha() < 255 || !ColorGenerator.PARTICLE_COLOR_LIST.contains(color)) continue;

                //Set the color of the particle param to the pixel color

                Particle particle = new Particle(ParticleTypes.DUST, new ParticleDustData(1.75f, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f));
                WrapperPlayServerParticle wrapper = new WrapperPlayServerParticle(particle, far, rendererLoc, OFFSET, 0f, 0);
                compressor.tryAdd(wrapper);
            }
        }
        return compressor.buffers();
    }

    @VisibleForTesting
    public static ByteBuf[] list(BufferedImage image) {
        //The scaling factor determines how dense the particles should be together (the higher the denominator, the less the space between the particles/pixels)
        float scalingFactor = 1f / 8f;
        boolean far = false;
        List<ByteBuf> list = new ArrayList<>(1024);

        //Iterates through all the pixels
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {

                float px = ((float) x * scalingFactor) - ((128 * scalingFactor) / 2);
                float py = 0;
                float pz = ((float) y * scalingFactor) - ((128 * scalingFactor) / 2);

                //If there is some rotation assigned, rotate the px, py, and pz coordinates by the given quaternion.
                if (ROTATION != null) {
                    Quaternion point = new Quaternion(0, px, py, pz);
                    Quaternion rotated = ROTATION.multiplied(point).multiplied(ROTATION.getInverse());
                    px = rotated.getX();
                    py = rotated.getY();
                    pz = rotated.getZ();
                }

                //Set the renderer's location to the center + the calculated pixel coordinates.
                //TODO: Threadsafe location modification.
                Vector3d rendererLoc = new Vector3d(CENTER.getX() + px, CENTER.getY() + py, CENTER.getZ() + pz);

                Color color = new Color(image.getRGB(x, y), true);

                //If there is some transparency in the pixel, go to the next pixel if there is some.
                if (color.getAlpha() < 255 || !ColorGenerator.PARTICLE_COLOR_LIST.contains(color)) continue;

                //Set the color of the particle param to the pixel color

                Particle particle = new Particle(ParticleTypes.DUST, new ParticleDustData(1.5f, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f));
                WrapperPlayServerParticle wrapper = new WrapperPlayServerParticle(particle, far, rendererLoc, OFFSET, 0f, 0);
                list.add(NettyUtils.createBuffer(wrapper));
            }
        }
        return list.toArray(new ByteBuf[0]);
    }

    private static final class Vec3f {

        private final float x;
        private final float y;
        private final float z;

        private Vec3f(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param number The number to be multiplied with.
         * @return The product of this vector and the given parameter.
         */
        private Vec3f multiplied(float number) {
            return new Vec3f(x * number, y * number, z * number);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @return The unit vector of this vector. The length of the new vector is 1.
         */
        private Vec3f normalized() {
            float length = getLength();
            return new Vec3f(x / length, y / length, z / length);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The dot product of this vector and the given parameter.
         */
        private float dot(Vec3f vec) {
            return x * vec.x + y * vec.y + z * vec.z;
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The cross product of this vector and the given parameter.
         */
        private Vec3f cross(Vec3f vec) {
            float newX = y * vec.z - z * vec.y;
            float newY = z * vec.x - x * vec.z;
            float newZ = x * vec.y - y * vec.x;
            return new Vec3f(newX, newY, newZ);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The sum of this vector and the given parameter.
         */
        private Vec3f added(Vec3f vec) {
            return new Vec3f(x + vec.x, y + vec.y, z + vec.z);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @return The length of this vector.
         */
        private float getLength() {
            return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        }

        private float getX() {
            return x;
        }

        private float getY() {
            return y;
        }

        private float getZ() {
            return z;
        }
    }

    private static final class Vec3d {

        double x;
        double y;
        double z;

        private Vec3d(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }


        private static Vec3d fromLocation(Location location) {
            return new Vec3d(location.getX(), location.getY(), location.getZ());
        }


        private static Vec3d fromBukkitVector(Vector vector) {
            return new Vec3d(vector.getX(), vector.getY(), vector.getZ());
        }

        private double distanceTo(Vec3d vec) {
            return Math.sqrt(Math.pow(x - vec.x, 2) + Math.pow(y - vec.y, 2) + Math.pow(z - vec.z, 2));
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param number The number to be multiplied with.
         * @return The product of this vector and the given parameter.
         */
        private Vec3d multiplied(float number) {
            return new Vec3d(x * number, y * number, z * number);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @return The unit vector of this vector. The length of the new vector is 1.
         */
        private Vec3d normalized() {
            double length = getLength();
            return new Vec3d(x / length, y / length, z / length);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The dot product of this vector and the given parameter.
         */
        private double dot(Vec3d vec) {
            return x * vec.x + y * vec.y + z * vec.z;
        }


        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The cross product of this vector and the given parameter.
         */
        private Vec3d cross(Vec3d vec) {
            double newX = y * vec.z - z * vec.y;
            double newY = z * vec.x - x * vec.z;
            double newZ = x * vec.y - y * vec.x;
            return new Vec3d(newX, newY, newZ);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The sum of this vector and the given parameter.
         */
        private Vec3d added(Vec3d vec) {
            return new Vec3d(x + vec.x, y + vec.y, z + vec.z);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @return The length of this vector.
         */
        private double getLength() {
            return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        }

        private void setX(double x) {
            this.x = x;
        }

        private void setY(double y) {
            this.y = y;
        }

        private void setZ(double z) {
            this.z = z;
        }

        private double getX() {
            return x;
        }

        private double getY() {
            return y;
        }

        private double getZ() {
            return z;
        }
    }

    private static final class Quaternion {

        private final Vec3f n;
        private final float w;
        private final float x;
        private final float y;
        private final float z;

        /**
         * Used to create a quaternion representing a certain axis in a 3-dimensional, cartesian coordinate system.
         *
         * @param n             The rotation axis
         * @param theta_degrees Rotation in degrees.
         */
        private Quaternion(Vec3f n, double theta_degrees) {
            Vec3f n2 = n.normalized();
            float theta = (float) toRadians(theta_degrees);
            Vec3f n_multiplied = n2.multiplied((float) sin(theta / 2));
            w = (float) cos(theta / 2);
            x = n_multiplied.getX();
            y = n_multiplied.getY();
            z = n_multiplied.getZ();
            this.n = new Vec3f(x, y, z);
        }

        /**
         * Used to create a point in 3-dimensional space.
         *
         * @param w The angle of the quaternion (needs to be the cosine of the half of an angle in radians). If used to create a point, set this to 0.
         * @param x The x variable of the quaternion.
         * @param y The y variable of the quaternion.
         * @param z The z variable of the quaternion.
         */
        private Quaternion(float w, float x, float y, float z) {
            n = new Vec3f(x, y, z);
            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Doesn't change anything to this quaternion.
         *
         * @return The inverse of the current quaternion.
         */
        private Quaternion getInverse() {
            Vec3f inverse = n.multiplied(-1);
            return new Quaternion(w, inverse.getX(), inverse.getY(), inverse.getZ());
        }

        /**
         * Doesn't change anything to this quaternion.
         *
         * @param quat The other quaternion to be calculated with.
         * @return The quaternion product of this quaternion and the given parameter
         */
        private Quaternion multiplied(Quaternion quat) {
            float newW = w * quat.w - n.dot(quat.n);
            Vec3f newN = n.multiplied(quat.w).added(quat.n.multiplied(w)).added(n.cross(quat.n));
            return new Quaternion(newW, newN.getX(), newN.getY(), newN.getZ());
        }


        private Vec3f getN() {
            return n;
        }

        private float getW() {
            return w;
        }

        private float getX() {
            return x;
        }

        private float getY() {
            return y;
        }

        private float getZ() {
            return z;
        }
    }
    
}