package alix.fonts;

import alix.common.AlixCommonMain;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.other.throwable.AlixException;

import java.awt.*;

public final class AlixFontManager {

    private static final Font font;

    static {
        int fontSize = ConfigProvider.config.getInt("captcha-font-size");
        String configFontName = ConfigProvider.config.getString("captcha-font");
        Font font0;

        if (AlixCommonUtils.isGraphicEnvironmentHeadless) {//this class should not have been loaded
            throw new AlixException(new AssertionError(new HeadlessException("Report this as an error immediately!")));
            /*switch (configFontName) {
                case "Dialog":
                case "DialogInput":
                case "SansSerif":
            }
            font0 = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);*/

            /*Font.createFont(
            //throw new AssertionError(new HeadlessException("Report this as an error immediately!"));
            URL url = CommonAlixMain.plugin.getClass().getClassLoader().getResource("alix/fonts/arial/arial.ttf");
            File file = new File(url.getFile());
            Font font = new Font(file, );
            Font.createFont(*/
            //FontManagerFactory.getInstance().createFont2D(
        } else {
            boolean valid = configFontName.equals("Arial");

            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

            for (Font font : env.getAllFonts()) {
                if (font.getName().equalsIgnoreCase(configFontName)) {
                    configFontName = font.getName();
                    valid = true;
                    break;
                }
            }

            if (!valid) {
                AlixCommonMain.logWarning("Your set font " + configFontName + " is not supported! Arial will be used instead.");
                configFontName = "Arial"; //the default
            }

            font0 = new Font(configFontName, Font.PLAIN, fontSize);
        }
        font = font0;
    }

    public static Font getPluginFont() {
        return font;
    }

    private AlixFontManager() {
    }
}