package shadow.systems.login.captcha.manager;

import alix.common.utils.AlixCommonUtils;
import alix.common.utils.collections.LoopCharIterator;
import org.bukkit.Bukkit;
import shadow.Main;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.captcha.subtypes.MapCaptcha;
import shadow.systems.login.captcha.subtypes.MessageCaptcha;
import shadow.systems.login.captcha.subtypes.SubtitleCaptcha;
import shadow.utils.main.AlixUtils;

import java.util.Arrays;
import java.util.Random;

import static shadow.utils.main.AlixUtils.captchaVerificationVisualType;

public abstract class CaptchaGenerator {

    private static final CaptchaGenerator generator = createGenerator();
    private final CaptchaTextGenerator textGenerator;

    private CaptchaGenerator() {
        this.textGenerator = getTextGenerator();
    }

    protected abstract Captcha generate();

    public static String generateTextCaptcha() {
        return generator.textGenerator.nextCaptchaText();
    }

    public static Captcha generateCaptcha() {
        return generator.generate();
    }

    private static CaptchaGenerator createGenerator() {
        switch (captchaVerificationVisualType) {
            case MAP:
                return new MapCaptchaGenerator();
            case SUBTITLE:
                return new SubtitleCaptchaGenerator();
            case MESSAGE:
                return new MessageCaptchaGenerator();
            default:
                throw new InternalError();
        }
    }

    private static CaptchaTextGenerator getTextGenerator() {
        switch (AlixUtils.captchaVerificationType) {
            case NUMERIC:
                return new CaptchaNumericGenImpl();
            case TEXT:
                return new CaptchaTextGenImpl();
            default:
                throw new InternalError();
        }
    }

    private interface CaptchaTextGenerator {

        String nextCaptchaText();

    }

    private static final class CaptchaTextGenImpl implements CaptchaTextGenerator {

        private final LoopCharIterator iterator;
        private final byte length;

        private CaptchaTextGenImpl() {
            StringBuilder builder = new StringBuilder();
            String config = Main.config.getString("text-captcha-ascii-range").replaceAll(" ", "");
            String[] ranges = AlixUtils.split(config, ',');
            for (String s : ranges) AlixUtils.fill(builder, s.charAt(0), s.charAt(2));
            char[] chars = builder.toString().toCharArray();
            this.iterator = new LoopCharIterator(AlixCommonUtils.shuffle(chars));
            this.length = AlixUtils.captchaLength;
        }

        @Override
        public String nextCaptchaText() {
            return new String(iterator.next(length));
        }
    }

    private static final class CaptchaNumericGenImpl implements CaptchaTextGenerator {

        private final Random random;
        private final int numericBoundary;
        private final byte length;

        private CaptchaNumericGenImpl() {
            this.length = AlixUtils.captchaLength;
            this.random = AlixUtils.random;
            this.numericBoundary = AlixUtils.powerIntegers(10, length);
        }

        private String format(int i) {
            String s = Integer.toString(i);
            int l = length - s.length();

            if (l == 0) return s;

            char[] c = new char[l];
            Arrays.fill(c, '0');

            return new String(c) + s; //new StringBuilder(JavaUtils.captchaLength).append(c).append(s).toString();
        }

        @Override
        public String nextCaptchaText() {
            return this.format(random.nextInt(numericBoundary)); //formatNumericCaptcha(String.valueOf(nextNumericCaptcha()));
        }
    }


    private static final class MapCaptchaGenerator extends CaptchaGenerator {

        @Override
        protected Captcha generate() {
            return new MapCaptcha();
        }
    }

    private static final class SubtitleCaptchaGenerator extends CaptchaGenerator {

        @Override
        protected Captcha generate() {
            return new SubtitleCaptcha();
        }
    }

    private static final class MessageCaptchaGenerator extends CaptchaGenerator {

        @Override
        protected Captcha generate() {
            return new MessageCaptcha();
        }
    }
}