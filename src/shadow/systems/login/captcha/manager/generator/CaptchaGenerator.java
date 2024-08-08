package shadow.systems.login.captcha.manager.generator;

import alix.common.scheduler.AlixScheduler;
import alix.common.scheduler.runnables.futures.AlixFuture;
import alix.common.utils.other.ConcurrentRandom;
import alix.common.utils.other.throwable.AlixError;
import shadow.Main;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.captcha.subtypes.*;
import shadow.utils.main.AlixUtils;

import java.util.Random;
import java.util.function.Supplier;

import static shadow.utils.main.AlixUtils.captchaVerificationType;
import static shadow.utils.main.AlixUtils.captchaVerificationVisualType;

public final class CaptchaGenerator {

    private static final Supplier<Captcha> SUPPLIER = createSupplier();
    private static final CaptchaTextGenerator textGenerator = createTextGenerator();

    private CaptchaGenerator() {
    }

    public static String generateTextCaptcha() {
        return textGenerator.generateTextCaptcha0();
    }

    public static AlixFuture<Captcha> generateCaptchaFuture() {
        return AlixScheduler.singleAlixFuture(SUPPLIER);
    }

    private static Supplier<Captcha> createSupplier() {
        switch (captchaVerificationVisualType) {
            case SMOOTH:
                return SmoothCaptcha::new;
            case PARTICLE:
                return ParticleCaptcha::new;
            case MAP:
                return MapCaptcha::new;
            case SUBTITLE:
                return SubtitleCaptcha::new;
            case MESSAGE:
                return MessageCaptcha::new;
            default:
                throw new AlixError(captchaVerificationVisualType.name());
        }
    }

    private static CaptchaTextGenerator createTextGenerator() {
        switch (captchaVerificationType) {
            case NUMERIC:
                return new CaptchaNumericGenImpl();
            case TEXT:
                return new CaptchaTextGenImpl();
            default:
                throw new AlixError(captchaVerificationType.name());
        }
    }

    private interface CaptchaTextGenerator {

        String generateTextCaptcha0();//IntelliJ often got confused, so I added the little 0 at the end

    }

    private static final class CaptchaTextGenImpl implements CaptchaTextGenerator {

        //private final LoopCharIterator iterator;
        private final Random random;
        private final char[] chars;
        private final byte length;

        private CaptchaTextGenImpl() {
            StringBuilder builder = new StringBuilder();
            String config = Main.config.getString("captcha-text-chars-range").replaceAll(" ", "");

            String[] configArgs = config.split(";e=");
            String[] ranges = AlixUtils.split(configArgs[0], ',');
            StringBuilder excludedChars = new StringBuilder("WM");//always exclude 'W' and 'M', since they're too big
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
            //Nvm it doesn't work
            //this.chars = AlixUtils.toPrimitive(Stream.of(AlixUtils.toObject(generalIncluded)).filter(c -> excluded.anyMatch(c2 -> c == c2)).toList().toArray(new Character[0]));

            //this.iterator = new LoopCharIterator(AlixCommonUtils.shuffle(chars));
            this.random = AlixUtils.random;
            this.length = AlixUtils.captchaLength;
        }

        private char nextChar() {
            return this.chars[this.random.nextInt(this.chars.length)];
        }

        @Override
        public String generateTextCaptcha0() {
            char[] a = new char[length];
            for (byte i = 0; i < length; i++) a[i] = this.nextChar();
            /*if (i == length - 1 && (c == 'w' || c == 'W')) {//do not let W be the last character, since it won't fit
                do {
                    c = this.nextChar();
                } while (c == 'w' || c == 'W');
            }*/
            return new String(a);
            //return new String(iterator.next(length));
        }
    }

    private static final class CaptchaNumericGenImpl implements CaptchaTextGenerator {

        private final ConcurrentRandom random;
        //private final int numericBoundary;
        private final byte length;

        private CaptchaNumericGenImpl() {
            this.random = ConcurrentRandom.getInstance();
            this.length = AlixUtils.captchaLength;
            //this.numericBoundary = AlixUtils.powerIntegers(10, length);
        }

        /*private String format(int i) {
            String s = Integer.toString(i);
            int d = this.length - s.length();

            if (d == 0) return s;

            char[] c = new char[d];
            Arrays.fill(c, '0');

            return new String(c) + s; //new StringBuilder(JavaUtils.captchaLength).append(c).append(s).toString();
        }*/

        @Override
        public String generateTextCaptcha0() {
            char[] c = new char[length];
            for (byte i = 0; i < length; i++) c[i] = (char) (random.nextInt(10) + 48);
            return new String(c);
            //return this.format(random.nextInt(numericBoundary)); //formatNumericCaptcha(String.valueOf(nextNumericCaptcha()));
        }
    }
}