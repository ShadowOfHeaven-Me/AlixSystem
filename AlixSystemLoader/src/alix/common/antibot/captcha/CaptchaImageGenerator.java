package alix.common.antibot.captcha;

import alix.common.utils.other.ConcurrentRandom;
import alix.fonts.AlixFontManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

public final class CaptchaImageGenerator {

    private static final Font font = AlixFontManager.getPluginFont();
    private static final ConcurrentRandom random = ConcurrentRandom.getInstance();
    private static final ImageGenerator generator = new DefaultGenerator();

    //Source code: https://github.com/InstantlyMoist/Captcha/blob/master/src/main/java/me/kyllian/captcha/spigot/captchas/TextCaptcha.java

    public static byte[] generatePixelsToDraw(String captcha, int maxRotation) {
        return imageToBytes(generator.generate(captcha, maxRotation));
    }

    //Just some random stuff I found and skidded as well
    private static final class SomeGen implements ImageGenerator {

        private void drawCode(Graphics graphics, String code) {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 128, 128);


            FontMetrics fontMetrics = graphics.getFontMetrics();
            int totalWidth = fontMetrics.stringWidth(code);
            int charGap = (128 - totalWidth) / (code.length() + 1);
            int x = charGap;
            int y = (128 - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();

            Random random = new Random();

            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(getRandomColor());

                int charX = x + fontMetrics.charWidth(code.charAt(i)) / 2;
                int angle = random.nextInt(41) - 20;

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.rotate(Math.toRadians(angle), charX, y);
                Font randomFont = font;
                graphics.setFont(randomFont);
                graphics.drawString(String.valueOf(code.charAt(i)), charX, y);
                g2d.rotate(-Math.toRadians(angle), charX, y);

                x += fontMetrics.charWidth(code.charAt(i)) + charGap;
            }
        }

        /**
         * Draw captcha noise.
         *
         * @param graphics Graphics
         */
        private void drawNoise(Graphics graphics) {
            Random random = new Random();
            for (int i = 0; i < 12; i++) {
                int x1 = random.nextInt(128);
                int y1 = random.nextInt(128);
                int x2 = x1 + random.nextInt(50) - 25;
                int y2 = y1 + random.nextInt(50) - 25;
                graphics.setColor(getRandomColor());
                graphics.drawLine(x1, y1, x2, y2);
            }
        }

        @Override
        public BufferedImage generate(String captcha, int maxRotation) {
            BufferedImage captchaImage = new BufferedImage(128, 128,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = captchaImage.createGraphics();

            drawCode(graphics, captcha);
            drawNoise(graphics);
            return captchaImage;
        }
    }

    private static final class DefaultGenerator implements ImageGenerator {

        @Override
        public BufferedImage generate(String captcha, int maxRotation) {
            BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);//TYPE_INT_RGB

            Canvas canvas = new Canvas();
            canvas.setSize(128, 128);

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            canvas.paint(graphics);

            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 128, 128);

/*            try {
                File file = new File(plugin.getDataFolder(), "background.png");
                BufferedImage background = ImageIO.read(file);
                AffineTransform affineTransform = new AffineTransform();
                graphics.drawImage(background, affineTransform, null);
            } catch (Exception exception) {
                exception.printStackTrace();
            }*/

            int lines = random.nextInt(12) + 8;

            while (lines-- != 0) {
                graphics.setColor(getRandomColor());
                graphics.drawLine(getRandomCoordinate(), getRandomCoordinate(), getRandomCoordinate(), getRandomCoordinate());
            }

            graphics.setFont(font);

            String[] chars = captcha.split("");

            for (int i = 0; i != chars.length; i++) {
                AffineTransform original = graphics.getTransform();
                int rotation = random.nextInt(2 * maxRotation + 1) - maxRotation;//generates a number between -n & n
                graphics.rotate(Math.toRadians(rotation), 30 + i * 20, 64);
                graphics.setColor(getRandomColor());
                graphics.drawString(chars[i], 20 + i * 20, 70);
                graphics.setTransform(original);
            }

            graphics.dispose();
            return image;
        }

        private DefaultGenerator() {
        }
    }


    private interface ImageGenerator {

        BufferedImage generate(String captcha, int maxRotation);

    }

    private static Color getRandomColor() {
        return new Color(random.nextInt(224), random.nextInt(224), random.nextInt(224));//224 instead of 256 in order to prevent the generation of whitish-colored characters, which would be impossible to read
    }

    private static int getRandomCoordinate() {
        return random.nextInt(128);
    }

    //From MapPalette

    private static final Color[] colors = new Color[]{c(0, 0, 0), c(0, 0, 0), c(0, 0, 0), c(0, 0, 0), c(89, 125, 39), c(109, 153, 48), c(127, 178, 56), c(67, 94, 29), c(174, 164, 115), c(213, 201, 140), c(247, 233, 163), c(130, 123, 86), c(140, 140, 140), c(171, 171, 171), c(199, 199, 199), c(105, 105, 105), c(180, 0, 0), c(220, 0, 0), c(255, 0, 0), c(135, 0, 0), c(112, 112, 180), c(138, 138, 220), c(160, 160, 255), c(84, 84, 135), c(117, 117, 117), c(144, 144, 144), c(167, 167, 167), c(88, 88, 88), c(0, 87, 0), c(0, 106, 0), c(0, 124, 0), c(0, 65, 0), c(180, 180, 180), c(220, 220, 220), c(255, 255, 255), c(135, 135, 135), c(115, 118, 129), c(141, 144, 158), c(164, 168, 184), c(86, 88, 97), c(106, 76, 54), c(130, 94, 66), c(151, 109, 77), c(79, 57, 40), c(79, 79, 79), c(96, 96, 96), c(112, 112, 112), c(59, 59, 59), c(45, 45, 180), c(55, 55, 220), c(64, 64, 255), c(33, 33, 135), c(100, 84, 50), c(123, 102, 62), c(143, 119, 72), c(75, 63, 38), c(180, 177, 172), c(220, 217, 211), c(255, 252, 245), c(135, 133, 129), c(152, 89, 36), c(186, 109, 44), c(216, 127, 51), c(114, 67, 27), c(125, 53, 152), c(153, 65, 186), c(178, 76, 216), c(94, 40, 114), c(72, 108, 152), c(88, 132, 186), c(102, 153, 216), c(54, 81, 114), c(161, 161, 36), c(197, 197, 44), c(229, 229, 51), c(121, 121, 27), c(89, 144, 17), c(109, 176, 21), c(127, 204, 25), c(67, 108, 13), c(170, 89, 116), c(208, 109, 142), c(242, 127, 165), c(128, 67, 87), c(53, 53, 53), c(65, 65, 65), c(76, 76, 76), c(40, 40, 40), c(108, 108, 108), c(132, 132, 132), c(153, 153, 153), c(81, 81, 81), c(53, 89, 108), c(65, 109, 132), c(76, 127, 153), c(40, 67, 81), c(89, 44, 125), c(109, 54, 153), c(127, 63, 178), c(67, 33, 94), c(36, 53, 125), c(44, 65, 153), c(51, 76, 178), c(27, 40, 94), c(72, 53, 36), c(88, 65, 44), c(102, 76, 51), c(54, 40, 27), c(72, 89, 36), c(88, 109, 44), c(102, 127, 51), c(54, 67, 27), c(108, 36, 36), c(132, 44, 44), c(153, 51, 51), c(81, 27, 27), c(17, 17, 17), c(21, 21, 21), c(25, 25, 25), c(13, 13, 13), c(176, 168, 54), c(215, 205, 66), c(250, 238, 77), c(132, 126, 40), c(64, 154, 150), c(79, 188, 183), c(92, 219, 213), c(48, 115, 112), c(52, 90, 180), c(63, 110, 220), c(74, 128, 255), c(39, 67, 135), c(0, 153, 40), c(0, 187, 50), c(0, 217, 58), c(0, 114, 30), c(91, 60, 34), c(111, 74, 42), c(129, 86, 49), c(68, 45, 25), c(79, 1, 0), c(96, 1, 0), c(112, 2, 0), c(59, 1, 0), c(147, 124, 113), c(180, 152, 138), c(209, 177, 161), c(110, 93, 85), c(112, 57, 25), c(137, 70, 31), c(159, 82, 36), c(84, 43, 19), c(105, 61, 76), c(128, 75, 93), c(149, 87, 108), c(78, 46, 57), c(79, 76, 97), c(96, 93, 119), c(112, 108, 138), c(59, 57, 73), c(131, 93, 25), c(160, 114, 31), c(186, 133, 36), c(98, 70, 19), c(72, 82, 37), c(88, 100, 45), c(103, 117, 53), c(54, 61, 28), c(112, 54, 55), c(138, 66, 67), c(160, 77, 78), c(84, 40, 41), c(40, 28, 24), c(49, 35, 30), c(57, 41, 35), c(30, 21, 18), c(95, 75, 69), c(116, 92, 84), c(135, 107, 98), c(71, 56, 51), c(61, 64, 64), c(75, 79, 79), c(87, 92, 92), c(46, 48, 48), c(86, 51, 62), c(105, 62, 75), c(122, 73, 88), c(64, 38, 46), c(53, 43, 64), c(65, 53, 79), c(76, 62, 92), c(40, 32, 48), c(53, 35, 24), c(65, 43, 30), c(76, 50, 35), c(40, 26, 18), c(53, 57, 29), c(65, 70, 36), c(76, 82, 42), c(40, 43, 22), c(100, 42, 32), c(122, 51, 39), c(142, 60, 46), c(75, 31, 24), c(26, 15, 11), c(31, 18, 13), c(37, 22, 16), c(19, 11, 8)};

    @NotNull
    private static Color c(int r, int g, int b) {
        return new Color(r, g, b);
    }

    public static byte[] imageToBytes(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        byte[] result = new byte[image.getWidth() * image.getHeight()];

        for (int i = 0; i < pixels.length; ++i) result[i] = matchColor(new Color(pixels[i], true));

        return result;
    }

    public static byte matchColor(Color color) {
        if (color.getAlpha() < 128) {
            return 0;
        } else {
            int index = 0;
            double best = -1.0D;

            for (int i = 4; i < colors.length; ++i) {
                double distance = getDistance(color, colors[i]);
                if (distance < best || best == -1.0D) {
                    best = distance;
                    index = i;
                }
            }

            return (byte) (index < 128 ? index : -129 + (index - 127));
        }
    }

    private static double getDistance(@NotNull Color c1, @NotNull Color c2) {
        double rMean = (double) (c1.getRed() + c2.getRed()) / 2.0D;
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        double weightR = 2.0D + rMean / 256.0D;
        double weightG = 4.0D;
        double weightB = 2.0D + (255.0D - rMean) / 256.0D;
        return weightR * r * r + weightG * g * g + weightB * (double) b * (double) b;
    }

    private CaptchaImageGenerator() {
    }
}