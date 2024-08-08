package shadow.utils.misc.captcha.D3;

import com.github.retrooper.packetevents.util.Vector3d;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class ModelRenderer3d {

    //Source code: https://github.com/fishydarwin/CraftEffect3D/blob/main/src/main/java/me/darwj1/crafteffect3d/bukkit/commands/DebugCommand.java

    public static List<Vector> renderingRelativePoints(Vector center) {
        try (InputStream stream = ModelRenderer3d.class.getResourceAsStream("Pointer3.obj")) {
            ObjShape shape = new ObjShape(stream);
            float precision = 0.8f;
            float scale = 0.8f;

            shape.xyzScaleAroundPoint(center.clone(), scale, scale, scale);
            //shape.yRotateAroundPoint(center.clone(), (float) Math.toRadians(180));
            return shape.surfacePoints(precision);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Vector3d> fromBukkit(List<Vector> bukkit) {
        List<Vector3d> retroop = new ArrayList<>(bukkit.size());
        for (Vector v : bukkit) retroop.add(new Vector3d(v.getX(), v.getY(), v.getZ()));
        return retroop;
    }


/*    public static void render() {
        try (InputStream stream = ParticleRenderer3d.class.getResourceAsStream("Pointer.obj")) {
            Location loc = new Location(Bukkit.getWorld("world"), 1000, 10, 1000, 20, 0);
            ObjShape shape = new ObjShape(stream);
            float precision = 0.25f;
            float scale = 1f;

            shape.xyzScaleAroundPoint(new Vector(0, 0, 0), scale, scale, scale);
            shape.yRotateAroundPoint(new Vector(0, 0, 0),
                    (float) Math.toRadians(loc.getYaw()));
            List<Vector> points = shape.surfacePoints(precision);
            AlixScheduler.repeatAsync(() -> {
                Optional<? extends Player> opt = Bukkit.getOnlinePlayers().stream().findFirst();
                if (!opt.isPresent()) return;
                Player player = opt.get();
                for (Vector point : points) {
                    player.spawnParticle(
                            Particle.REDSTONE,
                            player.getLocation().add(point.toLocation(player.getWorld())),
                            1, new Particle.DustOptions(Color.AQUA, 1));
                }
                Bukkit.broadcastMessage("POINTS " + points.size());
            }, 300, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    private static final class ObjShape {

        private List<Vector> vertices;
        private List<List<Integer>> faces;

        private ObjShape(InputStream in) throws IOException {
            vertices = new ArrayList<>();
            readVertices(new BufferedReader(new InputStreamReader(in)));
        }

        private List<Triangle> surfaceTriangles() {
            List<Triangle> triangles = new ArrayList<>();

            for (List<Integer> face : faces) {
                Vector vertex1 = vertices.get(face.get(0));
                Vector vertex2 = vertices.get(face.get(1));
                Vector vertex3 = vertices.get(face.get(2));
                triangles.add(new Triangle(vertex1, vertex2, vertex3));
            }

            return triangles;
        }

        private List<Vector> surfacePoints(float precision) {
            List<Vector> points = new ArrayList<>();

            for (List<Integer> face : faces) {
                Vector vertex1 = vertices.get(face.get(0));
                Vector vertex2 = vertices.get(face.get(1));
                Vector vertex3 = vertices.get(face.get(2));
                Triangle t = new Triangle(vertex1, vertex2, vertex3);
                points.addAll(t.surfacePoints(precision));
            }

            return points;
        }

        private void xRotateAroundPoint(Vector point, float angleRad) {
            for (Vector vertex : vertices) {
                VectorMatrix.xRotateAroundPoint(vertex, point, angleRad);
            }
        }

        private void yRotateAroundPoint(Vector point, float angleRad) {
            for (Vector vertex : vertices) {
                VectorMatrix.yRotateAroundPoint(vertex, point, angleRad);
            }
        }

        private void zRotateAroundPoint(Vector point, float angleRad) {
            for (Vector vertex : vertices) {
                VectorMatrix.zRotateAroundPoint(vertex, point, angleRad);
            }
        }

        private void xyzScaleAroundPoint(Vector point, float x, float y, float z) {
            for (Vector vertex : vertices) {
                VectorMatrix.xyzScaleAroundPoint(vertex, point, x, y, z);
            }
        }

        private List<Vector> readVertices(BufferedReader reader) throws IOException {
            vertices = new ArrayList<>();
            faces = new ArrayList<>();

            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("v ")) {

                    String[] split = line.trim().replaceAll(" +", " ").split(" ");
                    float x = Float.parseFloat(split[1]);
                    float y = Float.parseFloat(split[2]);
                    float z = Float.parseFloat(split[3]);
                    vertices.add(new Vector(x, y, z));

                } else if (line.startsWith("f ")) {

                    line.replace("//", "/");
                    String[] split = line.trim().replaceAll(" +", " ").split(" ");
                    int v1i = Integer.parseInt(split[1].split(Pattern.quote("/"))[0]);
                    int v2i = Integer.parseInt(split[2].split(Pattern.quote("/"))[0]);
                    int v3i = Integer.parseInt(split[3].split(Pattern.quote("/"))[0]);

                    List<Integer> face = new ArrayList<>();
                    face.add(v1i - 1);
                    face.add(v2i - 1);
                    face.add(v3i - 1);
                    faces.add(face);
                }
                line = reader.readLine();
            }

            return vertices;
        }
    }

    private static final class VectorLine {

        private final Vector P1;
        private final Vector P2;
        private Vector line;
        private final double lineLength;

        private VectorLine(Location L1, Location L2) {
            P1 = L1.toVector();
            P2 = L2.toVector();
            line = P2.clone().subtract(P1);
            lineLength = line.length();
        }

        private VectorLine(Vector V1, Vector V2) {
            P1 = V1;
            P2 = V2;
            line = P2.clone().subtract(P1);
            lineLength = line.length();
        }

        private final double length() {
            return lineLength;
        }

        private List<Vector> pointsBetween(float precision) {
            List<Vector> points = new ArrayList<>();

            Vector segment = line.clone().normalize().multiply(precision);
            Vector currentSegment = P1.clone();

            for (float i = 0; i <= lineLength; i += precision) {
                points.add(currentSegment.clone());
                currentSegment.add(segment);
            }

            return points;
        }

        private void xRotateAroundPoint(Vector point, float angleRad) {
            VectorMatrix.xRotateAroundPoint(P1, point, angleRad);
            VectorMatrix.xRotateAroundPoint(P2, point, angleRad);
            line = P2.subtract(P1);
        }

        private void yRotateAroundPoint(Vector point, float angleRad) {
            VectorMatrix.yRotateAroundPoint(P1, point, angleRad);
            VectorMatrix.yRotateAroundPoint(P2, point, angleRad);
            line = P2.subtract(P1);
        }

        private void zRotateAroundPoint(Vector point, float angleRad) {
            VectorMatrix.zRotateAroundPoint(P1, point, angleRad);
            VectorMatrix.zRotateAroundPoint(P2, point, angleRad);
            line = P2.subtract(P1);
        }

        private void xyzScaleAroundPoint(Vector point, float x, float y, float z) {
            VectorMatrix.xyzScaleAroundPoint(P1, point, x, y, z);
            VectorMatrix.xyzScaleAroundPoint(P2, point, x, y, z);
            line = P2.subtract(P1);
        }

    }

    private static final class Triangle {

        private final Vector point1;
        private final Vector point2;
        private final Vector point3;

        private Triangle(Vector point1, Vector point2, Vector point3) {
            this.point1 = point1;
            this.point2 = point2;
            this.point3 = point3;
            recalculate();
        }

        private VectorLine vl12;
        private VectorLine vl13;

        private void recalculate() {
            vl12 = new VectorLine(point1, point2);
            vl13 = new VectorLine(point1, point3);
        }

        private List<Vector> surfacePoints(float precision) {
            List<Vector> points = new ArrayList<>();

            float newPrecision;
            int pointsSize;

            List<Vector> vl12Points;
            List<Vector> vl13Points;
            if (vl12.length() > vl13.length()) {
                vl12Points = vl12.pointsBetween(precision);
                pointsSize = vl12Points.size();
                newPrecision = (float) vl13.length() / pointsSize;
                vl13Points = vl13.pointsBetween(newPrecision);
            } else {
                vl13Points = vl13.pointsBetween(precision);
                pointsSize = vl13Points.size();
                newPrecision = (float) vl12.length() / pointsSize;
                vl12Points = vl12.pointsBetween(newPrecision);
            }

            for (int i = 1; i < pointsSize; i++) {
                VectorLine surfaceLine = new VectorLine(vl12Points.get(i), vl13Points.get(i));
                points.addAll(surfaceLine.pointsBetween(precision));
            }

            return points;
        }

        private void xRotateAroundPoint(Vector point, float angleRad) {
            VectorMatrix.xRotateAroundPoint(point1, point, angleRad);
            VectorMatrix.xRotateAroundPoint(point2, point, angleRad);
            VectorMatrix.xRotateAroundPoint(point3, point, angleRad);
            recalculate();
        }

        private void yRotateAroundPoint(Vector point, float angleRad) {
            VectorMatrix.yRotateAroundPoint(point1, point, angleRad);
            VectorMatrix.yRotateAroundPoint(point2, point, angleRad);
            VectorMatrix.yRotateAroundPoint(point3, point, angleRad);
            recalculate();
        }

        private void zRotateAroundPoint(Vector point, float angleRad) {
            VectorMatrix.zRotateAroundPoint(point1, point, angleRad);
            VectorMatrix.zRotateAroundPoint(point2, point, angleRad);
            VectorMatrix.zRotateAroundPoint(point3, point, angleRad);
            recalculate();
        }

        private void xyzScaleAroundPoint(Vector point, float x, float y, float z) {
            VectorMatrix.xyzScaleAroundPoint(point1, point, x, y, z);
            VectorMatrix.xyzScaleAroundPoint(point2, point, x, y, z);
            VectorMatrix.xyzScaleAroundPoint(point3, point, x, y, z);
            recalculate();
        }

    }

    private static final class VectorMatrix {

        private static void transform(Vector vector, Matrix3f matrix) {
            Matrix3f matrix_v = new Matrix3f(new Float[]{
                    (float) vector.getX(), 0f, 0f,
                    (float) vector.getY(), 0f, 0f,
                    (float) vector.getZ(), 0f, 0f
            });
            matrix.mul(matrix_v);

            vector.setX(matrix.value()[0]);
            vector.setY(matrix.value()[3]);
            vector.setZ(matrix.value()[6]);
        }

        private static Matrix3f xRotationMatrix(float angleRad) {
            return new Matrix3f(new Float[]{
                    1f, 0f, 0f,
                    0f, (float) Math.cos(angleRad), (float) -Math.sin(angleRad),
                    0f, (float) Math.sin(angleRad), (float) Math.cos(angleRad)
            });
        }

        private static Matrix3f yRotationMatrix(float angleRad) {
            return new Matrix3f(new Float[]{
                    (float) Math.cos(angleRad), 0f, (float) -Math.sin(angleRad),
                    0f, 1f, 0f,
                    (float) Math.sin(angleRad), 0f, (float) Math.cos(angleRad)
            });
        }

        private static Matrix3f zRotationMatrix(float angleRad) {
            return new Matrix3f(new Float[]{
                    (float) Math.cos(angleRad), (float) -Math.sin(angleRad), 0f,
                    (float) Math.sin(angleRad), (float) Math.cos(angleRad), 0f,
                    0f, 0f, 1f
            });
        }

        private static void xRotateAroundPoint(Vector who, Vector point, float angleRad) {
            Vector diff = who.clone().subtract(point);
            transform(diff, xRotationMatrix(angleRad));
            who.copy(diff.add(point));
        }

        private static void yRotateAroundPoint(Vector who, Vector point, float angleRad) {
            Vector diff = who.clone().subtract(point);
            transform(diff, yRotationMatrix(angleRad));
            who.copy(diff.add(point));
        }

        private static void zRotateAroundPoint(Vector who, Vector point, float angleRad) {
            Vector diff = who.clone().subtract(point);
            transform(diff, zRotationMatrix(angleRad));
            who.copy(diff.add(point));
        }

        private static Matrix3f xyzScaleMatrix(float x, float y, float z) {
            return new Matrix3f(new Float[]{
                    x, 0f, 0f,
                    0f, y, 0f,
                    0f, 0f, z
            });
        }

        private static void xyzScaleAroundPoint(Vector who, Vector point, float x, float y, float z) {
            Vector diff = who.clone().subtract(point);
            transform(diff, xyzScaleMatrix(x, y, z));
            who.copy(diff.add(point));
        }
    }

    private static final class Matrix3f {

        private Float[] matrixArray =
                {0f, 0f, 0f,
                        0f, 0f, 0f,
                        0f, 0f, 0f};

        private Matrix3f() {
        }

        private Matrix3f(Float[] initialMatrixArray) {
            matrixArray = initialMatrixArray;
        }

        private static final Matrix3f IDENTITY = new Matrix3f(
                new Float[]
                        {1f, 0f, 0f,
                                0f, 1f, 0f,
                                0f, 0f, 1f});

        private Float[] value() {
            return matrixArray;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Matrix3f)) {
                return false;
            }
            Matrix3f m = (Matrix3f) o;
            for (int i = 0; i < 9; i++) {
                if (!m.value()[i].equals(matrixArray[i])) {
                    return false;
                }
            }
            return true;
        }

        private void add(Matrix3f matrix) {
            for (int i = 0; i < 9; i++) {
                matrixArray[i] += matrix.value()[i];
            }
        }

        private void mul(Float scalar) {
            for (int i = 0; i < 9; i++) {
                matrixArray[i] *= scalar;
            }
        }

        private void mul(Matrix3f matrix) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    matrixArray[3 * row + col] =
                            matrixArray[3 * row] * matrix.value()[col] +
                                    matrixArray[3 * row + 1] * matrix.value()[3 + col] +
                                    matrixArray[3 * row + 2] * matrix.value()[6 + col];
                }
            }
        }

        private Float determinant() {
            return matrixArray[0] * matrixArray[4] * matrixArray[8] +
                    matrixArray[1] * matrixArray[5] * matrixArray[6] +
                    matrixArray[2] * matrixArray[3] * matrixArray[7] -
                    matrixArray[2] * matrixArray[4] * matrixArray[6] -
                    matrixArray[1] * matrixArray[3] * matrixArray[8] -
                    matrixArray[0] * matrixArray[5] * matrixArray[7];
        }
    }
}