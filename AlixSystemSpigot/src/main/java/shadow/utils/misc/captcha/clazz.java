/*    private static final class FancyNumericRendererImpl extends CaptchaMapRenderer {

        private static final int pixelsBetween = 0;
        private final BufferedImage[] toRender;
        private final int[] startPosX;
        private final int startPosY;

        private FancyNumericRendererImpl(String text) {
            char[] chars = text.toCharArray();
            this.toRender = new BufferedImage[chars.length];//TODO: fix this

            List<Integer> list = new ArrayList<>(1);

            int totalWidth = 0;

            for (int i = 0; i < chars.length; i++) {
                this.toRender[i] = MapImages.getDigitImage(chars[i] - 48);

                int toRenderNowWidth = this.toRender[i].getWidth() + pixelsBetween;

                if (totalWidth + toRenderNowWidth - 128 > 0) {
                    list.add((128 - totalWidth) / 2);
                    totalWidth = toRenderNowWidth;//reset and update to the current toRenderWidth
                } else totalWidth += toRenderNowWidth;
            }

            if (totalWidth != 0) list.add((128 - totalWidth) / 2);

            this.startPosX = AlixUtils.toPrimitive(list.toArray(new Integer[0]));


            //All characters are 30 pixels in height
            this.startPosY = 79 - startPosX.length * 15; //After simplification
            //+30 because the characters are rendered from the bottom

*//*            int totalWidth = toRenderWidth;

            this.startPosX = startPosX; = (128 - totalWidth) / 2;*//*


        }

        private static boolean isWhiteSimilar(byte b) {
            Color c;

            try {
                c = MapPalette.getColor(b);
            } catch (IndexOutOfBoundsException e) {
                return true;
            }

            int maxOffset = 10;

            int threshold = 255 - maxOffset;

            return c.getRed() >= threshold && c.getBlue() >= threshold && c.getGreen() >= threshold;
        }

        @Override
        protected void renderCaptcha(MapCanvas canvas) {
            int posX = this.startPosX[0];

            //The starting y coordinate \/
            int posY = this.startPosY;

            int posXIndex = 0;

            int totalWidth = 0;

            for (BufferedImage image : this.toRender) {

                int toRenderNowWidth = image.getWidth() + pixelsBetween;

                if (totalWidth + toRenderNowWidth - 128 > 0) {
                    posX = this.startPosX[posXIndex++];
                    posY -= image.getHeight() + 1;
                    totalWidth = toRenderNowWidth;
                } else totalWidth += toRenderNowWidth;

                canvas.drawImage(posX, posY, image);
                posX += toRenderNowWidth;
            }

            byte colorBase = MapPalette.matchColor(0, 0, 0);

            for (int x = 0; x < 128; x++) {
                for (int y = 0; y < 128; y++) {
                    if (isWhiteSimilar(canvas.getPixel(x, y))) canvas.setPixel(x, y, colorBase);
                }
            }

            //canvas.drawText(55, 10, MinecraftFont.Font, "CAPTCHA");
        }
    }*/

/*    private static final class TextRendererImpl extends CaptchaMapRenderer {

        private final String text;

        public TextRendererImpl(String text) {
            this.text = text;
        }

        @Override
        protected void renderCaptcha(MapCanvas canvas) {
            canvas.drawText(55, 55, MinecraftFont.Font, text);
        }
    }*/