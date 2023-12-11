package alix.common.antibot.captcha;

import alix.common.utils.other.ConcurrentRandom;
import alix.common.utils.other.ByteArrayFunction;
import alix.fonts.AlixFontManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public final class CaptchaImageGenerator {

    private static final Font font = AlixFontManager.getPluginFont();
    private static final ConcurrentRandom random = ConcurrentRandom.getInstance();

    //Source code: https://github.com/InstantlyMoist/Captcha/blob/master/src/main/java/me/kyllian/captcha/spigot/captchas/TextCaptcha.java

    public static byte[] generatePixelsToDraw(String captcha, int maxRotation, ByteArrayFunction<Image> function) {
        Image image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);

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

        int lines = random.nextInt(12) + 16;

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

        return function.getBytes(image);
    }

    private static Color getRandomColor() {
        return new Color(random.nextInt(224), random.nextInt(224), random.nextInt(224));//224 instead of 256 to not generate a white-ish colored characters which would be impossible to read
    }

    private static int getRandomCoordinate() {
        return random.nextInt(128);
    }
}