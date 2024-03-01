package shadow.systems.login.captcha.manager.generator;

import alix.common.scheduler.AlixScheduler;
import alix.common.scheduler.runnables.futures.AlixFuture;
import alix.common.utils.other.ConcurrentRandom;
import shadow.Main;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.captcha.subtypes.MapCaptcha;
import shadow.systems.login.captcha.subtypes.MessageCaptcha;
import shadow.systems.login.captcha.subtypes.SubtitleCaptcha;
import shadow.utils.main.AlixUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static shadow.utils.main.AlixUtils.captchaVerificationType;
import static shadow.utils.main.AlixUtils.captchaVerificationVisualType;

public abstract class CaptchaGenerator {

    private static final CaptchaGenerator generator = createGenerator();
    private static final Supplier<Captcha> SUPPLIER = generator::generate;
    private final CaptchaTextGenerator textGenerator;

    private CaptchaGenerator() {
        this.textGenerator = createTextGenerator();
    }

    abstract Captcha generate();

    public static String generateTextCaptcha() {
        return generator.textGenerator.generateTextCaptcha();
    }

    public static AlixFuture<Captcha> generateCaptchaFuture() {
        return AlixScheduler.singleAlixFuture(SUPPLIER);
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
                throw new InternalError(captchaVerificationVisualType.name());
        }
    }

    private static CaptchaTextGenerator createTextGenerator() {
        switch (captchaVerificationType) {
            case NUMERIC:
                return new CaptchaNumericGenImpl();
            case TEXT:
                return new CaptchaTextGenImpl();
            default:
                throw new InternalError(captchaVerificationType.name());
        }
    }

    private interface CaptchaTextGenerator {

        String generateTextCaptcha();

    }

    private static final class CaptchaTextGenImpl implements CaptchaTextGenerator {

        //private final LoopCharIterator iterator;
        private final ConcurrentRandom random;
        private final char[] chars;
        private final byte length;

        private CaptchaTextGenImpl() {
            StringBuilder builder = new StringBuilder();
            String config = Main.config.getString("captcha-text-ascii-range").replaceAll(" ", "");

            String[] configArgs = config.split(";e=");
            String[] ranges = AlixUtils.split(configArgs[0], ',');
            StringBuilder excludedChars = new StringBuilder();
            if (configArgs.length == 2) {
                String[] excluded = configArgs[1].split(",");
                for (String ex : excluded) excludedChars.append(ex);
            }
            for (String s : ranges) AlixUtils.fill(builder, s.charAt(0), s.charAt(2));
            char[] excluded = excludedChars.toString().toCharArray();
            char[] generalIncluded = builder.toString().toCharArray();

            StringBuilder chars = new StringBuilder();

            z:
            for (char c : generalIncluded) {
                for (char e : excluded) if (c == e) continue z;
                chars.append(c);
            }
            this.chars = chars.toString().toCharArray();

            //probably the longest single-line statement I've ever written
            //this.chars = AlixUtils.toPrimitive(Stream.of(AlixUtils.toObject(generalIncluded)).filter(c -> excluded.anyMatch(c2 -> c == c2)).toList().toArray(new Character[0]));

            //this.iterator = new LoopCharIterator(AlixCommonUtils.shuffle(chars));
            this.random = ConcurrentRandom.getInstance();
            this.length = AlixUtils.captchaLength;
        }

        @Override
        public String generateTextCaptcha() {
            char[] c = new char[length];
            for (int i = 0; i < length; i++) c[i] = chars[random.nextInt(chars.length)];
            return new String(c);
            //return new String(iterator.next(length));
        }
    }

    private static final class CaptchaNumericGenImpl implements CaptchaTextGenerator {

        private final ConcurrentRandom random;
        private final int numericBoundary;
        private final byte length;

        private CaptchaNumericGenImpl() {
            this.random = ConcurrentRandom.getInstance();
            this.length = AlixUtils.captchaLength;
            this.numericBoundary = AlixUtils.powerIntegers(10, length);
        }

        private String format(int i) {
            String s = Integer.toString(i);
            int d = this.length - s.length();

            if (d == 0) return s;

            char[] c = new char[d];
            Arrays.fill(c, '0');

            return new String(c) + s; //new StringBuilder(JavaUtils.captchaLength).append(c).append(s).toString();
        }

        @Override
        public String generateTextCaptcha() {
            return this.format(random.nextInt(numericBoundary)); //formatNumericCaptcha(String.valueOf(nextNumericCaptcha()));
        }
    }


    private static final class MapCaptchaGenerator extends CaptchaGenerator {

        private MapCaptchaGenerator() {
        }

        @Override
        Captcha generate() {
            return new MapCaptcha();
        }
    }

    private static final class SubtitleCaptchaGenerator extends CaptchaGenerator {

        private SubtitleCaptchaGenerator() {
        }

        @Override
        Captcha generate() {
            return new SubtitleCaptcha();
        }
    }

    private static final class MessageCaptchaGenerator extends CaptchaGenerator {

        private MessageCaptchaGenerator() {
        }

        @Override
        Captcha generate() {
            return new MessageCaptcha();
        }
    }
}