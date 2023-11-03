/*
package shadow.utils.holders.captcha;

import alix.bukkit.BukkitAlixMain;
import org.bukkit.map.MapFont;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class MapImages extends MapFont {

    private static final BufferedImage[] digits;

    public static BufferedImage getDigitImage(int digit) {
        return digits[digit];
    }

    static {
        digits = new BufferedImage[10];

        for (int i = 0; i < 10; i++) {
            URL url = BukkitAlixMain.class.getClassLoader().getResource("alix.fonts.fancy_map_font/fancy_digit_" + i + ".png");
            try {
                digits[i] = ImageIO.read(url);
            } catch (IOException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }
}*/
