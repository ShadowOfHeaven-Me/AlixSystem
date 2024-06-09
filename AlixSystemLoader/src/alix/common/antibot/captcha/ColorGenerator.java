package alix.common.antibot.captcha;

import alix.common.utils.config.ConfigParams;
import alix.common.utils.other.ConcurrentRandom;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.List;


public abstract class ColorGenerator {

    public static final ColorGenerator IMPL = ConfigParams.isCaptchaParticle ? new ParticleColorGenerator() : new MapColorGenerator();
    private static final ConcurrentRandom random = ConcurrentRandom.getInstance();
    public static final List<Color> PARTICLE_COLOR_LIST = Arrays.asList(_PAColors.particleColors);

    public abstract Color nextDifferentColor(Color previous);

    private static final class MapColorGenerator extends ColorGenerator {

        private static final int MIN_RGB = 25;
        private static final int RBG_BOUND = 224 - MIN_RGB;//224 instead of 256, in order to prevent the generation of whitish-colored characters, which would be impossible to read

        @Override
        public Color nextDifferentColor(Color previous) {
            Color color;
            do {
                color = randomColor();
            } while (previous != null && isSimilar(color, previous));
            return color;
        }

        private Color randomColor() {
            return new Color(random.nextInt(RBG_BOUND) + MIN_RGB, random.nextInt(RBG_BOUND) + MIN_RGB, random.nextInt(RBG_BOUND) + MIN_RGB);
        }

        private static boolean isSimilar(Color c1, Color c2) {
            return similar(c1.getRed(), c2.getRed()) && similar(c1.getBlue(), c2.getBlue()) && similar(c1.getGreen(), c2.getGreen());
        }

        private static boolean similar(int c1, int c2) {
            return Math.abs(c1 - c2) <= 25;
        }
    }

    private static final class ParticleColorGenerator extends ColorGenerator {

        @Override
        public Color nextDifferentColor(Color previous) {
            int r = random.nextInt(PARTICLE_COLOR_LIST.size());

            if (previous == null) return PARTICLE_COLOR_LIST.get(r);//nothing to compare to

            int p = PARTICLE_COLOR_LIST.indexOf(previous);
            int distance = Math.abs(r - p);
            int i2 = distance <= 1 ? (r + 2) % PARTICLE_COLOR_LIST.size() : r;//the color is too similar - try again - or if not, use it
            return PARTICLE_COLOR_LIST.get(i2);
        }
    }

    private static final class _PAColors {
        private static final Color[] particleColors = {
                c(255, 255, 0),     // Yellow
                c(255, 215, 0),     // Gold
                c(255, 165, 0),     // Orange
                c(255, 140, 26),    // Lighter Orange
                c(255, 127, 80),    // Coral
                c(255, 105, 180),   // Hot Pink
                c(255, 20, 147),    // Deep Pink
                c(255, 0, 255),     // Magenta
                c(255, 0, 0),       // Red
                c(127, 255, 0),     // Chartreuse
                c(0, 255, 0),       // Lime
                c(0, 255, 255),     // Cyan
        };

        @NotNull
        private static Color c(int r, int g, int b) {
            return new Color(r, g, b);
        }
    }
}