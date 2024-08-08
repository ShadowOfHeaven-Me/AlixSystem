package shadow.utils.math;

public final class CircleFastMath {

    public static final CircleFastMath circleFastMath = new CircleFastMath();
    private final double[] arrayOfXes, arrayOfZs;

    private CircleFastMath() {
        this.arrayOfXes = new double[360];
        this.arrayOfZs = new double[360];
        for (short degree = 0; degree < 360; degree++) {

            double radians = Math.toRadians(degree);

            double x = Math.cos(radians);
            double z = Math.sin(radians);

            this.arrayOfXes[degree] = x;
            this.arrayOfZs[degree] = z;
        }
    }

    public double[] getArrayOfXes() {
        return arrayOfXes;
    }

    public double[] getArrayOfZs() {
        return arrayOfZs;
    }

}