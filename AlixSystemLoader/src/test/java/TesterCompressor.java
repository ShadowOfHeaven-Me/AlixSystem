import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.Deflater;

public class TesterCompressor {

    static final Deflater deflater = new Deflater(Deflater.BEST_SPEED);
    static final Deflater deflaterMax = new Deflater(Deflater.BEST_COMPRESSION);

    public static void main(String[] args) {
        //ByteBuf buf = BufUtils.createBuffer(new WrapperPlayClientSettings(null));
        Random random = new SecureRandom();
        int len = 255;

        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[i] = (byte) (i / 10);
        }

        byte[] normal = Arrays.copyOf(b, b.length);
        byte[] max = Arrays.copyOf(b, b.length);

        deflater.setInput(normal);
        deflater.finish();

        int newLen = deflater.deflate(new byte[4000]);

        deflaterMax.setInput(max);
        deflaterMax.finish();

        int newLen2 = deflaterMax.deflate(new byte[4000]);

        System.out.println(newLen + " " + newLen2);
        //deflater.deflate();
    }
}