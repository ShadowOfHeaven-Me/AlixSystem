package shadow.utils.main;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.config.ConfigParams;
import alix.common.utils.file.AlixFileManager;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.i18n.HttpsHandler;
import alix.common.utils.multiengine.ban.BukkitBanList;
import alix.common.utils.other.annotation.AlixIntrinsified;
import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import shadow.Main;
import shadow.systems.commands.ExecutableCommandList;
import shadow.systems.login.captcha.types.CaptchaType;
import shadow.systems.login.captcha.types.CaptchaVisualType;
import shadow.utils.misc.ReflectionUtils;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.misc.skull.SkullSupplier;
import shadow.utils.objects.AlixConsoleFilterHolder;
import shadow.utils.world.AlixWorldHolder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class AlixUtils {

    public static final Random random;
    //public static final PotionEffect vanishInvisibilityEffect;
    public static final Pattern ipPattern;
    //public static final JsonObject alexSkinTexture;
    public static final SimpleDateFormat dateFormatter, timeFormatter;
    //public static final String[] invalidNicknamesStart;
    //public static final Language pluginLanguage;
    public static final ExecutableCommandList registerCommandList, loginCommandList, autoLoginCommandList, autoRegisterCommandList;
    public static final String operatorCommandPassword, chatFormat;//, banFormat;
    public static final CaptchaType captchaVerificationType;
    public static final CaptchaVisualType captchaVerificationVisualType;
    public static final float doubledDefaultWalkSpeed, doubledDefaultFlySpeed;
    //public static final long verificationReminderDelay;
    public static final long autoLoginExpiry;
    public static final int maximumTotalAccounts, maxCaptchaTime, maxLoginTime, maxLoginAttempts, maxCaptchaAttempts;//, lowestTeleportableYLevel;
    public static final byte captchaLength;
    public static final boolean isOperatorCommandRestricted, isPluginLanguageEnglish, isOfflineExecutorRegistered, requireCaptchaVerification,
            captchaVerificationCaseSensitive, isDebugEnabled, userDataAutoSave, interveneInChatFormat, isOnlineModeEnabled,
            requirePingCheckVerification, forcefullyDisableIpAutoLogin, //repeatedVerificationReminderMessages,
            anvilPasswordGui, hideFailedJoinAttempts, alixJoinLog, overrideExistingCommands, antibotService,
            requirePasswordRepeatInRegister;//renderFancyCaptchaDigits

    private static final String
            tooLongMessage = Messages.getWithPrefix("password-invalid-too-long"),
            tooShortMessage = Messages.getWithPrefix("password-invalid-too-short"),
            invalidCharacterMessage = Messages.getWithPrefix("password-invalid-character-invalid");

    static {
        FileConfiguration config = Main.config;
        String language = config.getString("language").toLowerCase();
        switch (language) {
            case "en":
            case "pl":
                break;
            default:
                Main.logWarning("Invalid language type in config! Available: en & pl, but instead got '" +
                        language + "'! Switching to english, as default");
                break;
        }
/*        String restrictBase = config.getString("login-restrict-base", "packet").toLowerCase();
        switch (restrictBase) {
            case "packet":
                break;
            case "teleport":
                Main.logWarning("'teleport' in 'login-restrict-base' is for now disabled. Switching to 'packet'!");
                restrictBase = "packet";
                break;
            default:
                Main.logWarning("Invalid 'login-restrict-base' parameter type set in config! Available: packet & teleport (although 'teleport' is for now disabled), but instead got '" +
                        restrictBase + "'! Switching to 'packet' for safety reasons.");
                break;
        }*/
        String captchaVisualType = config.getString("captcha-visual-type").toLowerCase();
        switch (captchaVisualType) {
            case "smooth":
                if (AlixCommonUtils.isGraphicEnvironmentHeadless) {
                    Main.logWarning("The option 'smooth' in 'captcha-visual-type' is only available in a headful graphic environment," +
                            " with this being a headless one. Switching to 'subtitle', as default!");
                    captchaVisualType = "subtitle";
                }
                break;
            case "particle":
                if (AlixCommonUtils.isGraphicEnvironmentHeadless) {
                    Main.logWarning("The option 'particle' in 'captcha-visual-type' is only available in a headful graphic environment," +
                            " with this being a headless one. Switching to 'subtitle', as default!");
                    captchaVisualType = "subtitle";
                    break;
                }


                Main.logWarning("The option 'particle' in 'captcha-visual-type' is now outdated! " +
                        "The 'smooth' type will be used in it's stead, as its better equivalent.");
                captchaVisualType = "smooth";
                break;
            case "map":
                /*if (ReflectionUtils.bukkitVersion <= 13) {
                    Main.logWarning("The option 'map' in 'captcha-visual-type' is only available for versions 1.14+, and your version is "
                            + ReflectionUtils.bukkitVersion + ". Switching to 'subtitle', as default! ");
                    captchaVisualType = "subtitle";
                }*/

                if (AlixCommonUtils.isGraphicEnvironmentHeadless) {
                    Main.logWarning("The option 'map' in 'captcha-visual-type' is only available in a heedful graphic environment," +
                            " with this being a headless one. Switching to 'subtitle', as default!");
                    captchaVisualType = "subtitle";
                    break;
                }

                if (captchaVisualType.equals("map")) {
                    Main.logWarning("The option 'map' in 'captcha-visual-type' is currently disabled. " +
                            "Switching to 'smooth', as it's visual equivalent.");
                    captchaVisualType = "smooth";
                }

                break;
            case "subtitle":
            case "message":
                break;
            default:
                String best = AlixCommonUtils.isGraphicEnvironmentHeadless ? "subtitle" : "smooth";
                Main.logWarning("Invalid 'captcha-visual-type' parameter set in config! Available: particle, map, subtitle & message, but instead got '" +
                        captchaVisualType + "! Switching to '" + best + "', as default.");
                captchaVisualType = best;
                break;
        }
        String captchaType = config.getString("captcha-type").toLowerCase();
        switch (captchaType) {
            case "numeric":
            case "text":
                break;
            default:
                Main.logWarning("Invalid 'captcha-type' parameter set in config! Available: numeric & text, but instead got '" +
                        captchaType + "! Switching to 'text', as default.");
                break;
        }
        //pluginLanguage = getPluginLanguage(language););
        isPluginLanguageEnglish = !language.equals("pl");
        anvilPasswordGui = ConfigParams.defaultLoginType == LoginType.ANVIL;
        autoLoginExpiry = getProcessedTime(config.getString("autologin-expiry"));
        //Main.debug("EXPIRY: " + autoLoginExpiry);
        captchaVerificationType = CaptchaType.from(captchaType.toUpperCase());
        captchaVerificationVisualType = CaptchaVisualType.from(captchaVisualType.toUpperCase());
        //renderFancyCaptchaDigits = config.getBoolean("fancy-digits") && captchaVerificationType == CaptchaType.NUMERIC;
        byte captchaLength0 = (byte) Math.min(Math.max((byte) config.getInt("captcha-length"), 1), 10);
/*        if (renderFancyCaptchaDigits) {
            if (captchaLength0 > 8) {
                Main.logWarning("'fancy-digits' requires the captcha length to be less than 9! 8 will be used instead, as default");
                captchaLength0 = 8;
            }
        } else {*/
        if (captchaLength0 > 5) {
            Main.logWarning("The default captcha length must be less than 6! 5 will be used instead, as default.");
            captchaLength0 = 5;
        }
        captchaLength = captchaLength0;
        //}
        registerCommandList = new ExecutableCommandList(config.getStringList("after-register-commands"));
        loginCommandList = new ExecutableCommandList(config.getStringList("after-login-commands"));
        autoRegisterCommandList = new ExecutableCommandList(config.getStringList("after-auto-register-commands"));
        autoLoginCommandList = new ExecutableCommandList(config.getStringList("after-auto-login-commands"));
        forcefullyDisableIpAutoLogin = config.getBoolean("forcefully-disable-auto-login");
        captchaVerificationCaseSensitive = config.getBoolean("captcha-case-sensitive");
        //verificationReminderDelay = config.getLong("verification-reminder-message-delay");
        //repeatedVerificationReminderMessages = verificationReminderDelay > 0;
        isOnlineModeEnabled = Bukkit.getServer().getOnlineMode();
        antibotService = config.getBoolean("antibot-service");
        requirePasswordRepeatInRegister = config.getBoolean("require-password-repeat-in-register");
        isOfflineExecutorRegistered = config.getBoolean("offline-login-requirement") && !isOnlineModeEnabled;
        maxLoginAttempts = config.getInt("max-login-attempts");
        isDebugEnabled = config.getBoolean("debug");
        userDataAutoSave = config.getBoolean("auto-save");
        overrideExistingCommands = config.getBoolean("override-existing-commands");
        chatFormat = translateColors(config.getString("chat-format"));
        //banFormat = translateColors(config.getString("ban-format"));

        requireCaptchaVerification = isOfflineExecutorRegistered && config.getBoolean("captcha");
        requirePingCheckVerification = requireCaptchaVerification && config.getBoolean("ping-check");
        int maxCaptchaTime0 = config.getInt("max-captcha-time");

        if (maxCaptchaTime0 < 20 && requireCaptchaVerification) {
            Main.logWarning("'max-captcha-time' cannot be less than 20! (Got " + maxCaptchaTime0 + " - switching to 30, as default) If you wish to disable the captcha verification, then change 'captcha' to false in the config file!");
            maxCaptchaTime0 = 30;
        }

        maxCaptchaTime = maxCaptchaTime0;
        interveneInChatFormat = config.getBoolean("use-this-syntax");
        maximumTotalAccounts = config.getInt("max-total-accounts");
        isOperatorCommandRestricted = config.getBoolean("restrict-op-command");
        operatorCommandPassword = config.getString("op-command-password");
        maxCaptchaAttempts = config.getInt("max-captcha-attempts");
        hideFailedJoinAttempts = AlixConsoleFilterHolder.hideFailedJoinAttempts;
        alixJoinLog = AlixConsoleFilterHolder.alixJoinLog;
        //invalidNicknamesStart = config.getStringList("disallow-join-of").toArray(new String[0]);
        //spawn = Spawn.fromString(config.getString("spawn-location"));

        maxLoginTime = config.getInt("max-login-time");
        //lowestTeleportableYLevel = getLowestTeleportableLevel();
        /*serverVersion = getServerVersion();
        bukkitVersion = getBukkitVersion();*/
        //name - password - ip - homes
        //alexSkinTexture = parseTexture("Alex");

        //Copied from Essentials ;]
        ipPattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        //vanishInvisibilityEffect = new PotionEffect(PotionEffectType.INVISIBILITY, -1, 147, false, false, false);
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        timeFormatter = new SimpleDateFormat("HH:mm:ss");
        random = AlixCommonUtils.random;
        doubledDefaultWalkSpeed = 0.2F;
        doubledDefaultFlySpeed = 0.1F;
        if (config.getBoolean("ping-before-join")) AlixHandler.initializeServerPingManager();
        AlixHandler.updateConsoleFilter();
    }

    public static void getMethodTime(Executable method) {
        /*long ranOnce = methodTimeRun1(method);//method time + nano method offset = m + n
        long ranTwice = methodTimeRun1(method);//2 * method time + nano time offset = 2m + n
        long time = ranTwice - ranOnce;//2m + n - (m + n) = m*/
        long time = methodTimeRun1(method);
        String result = setAsClearNumber(time / Math.pow(10, 6));
        Main.logError("METHOD TIME: " + result + " (ms)");
    }

    private static long methodTimeRun1(Executable method) {
        try {
            long t = System.nanoTime();
            method.execute();
            return System.nanoTime() - t;
        } catch (Throwable e) {
            throw new AlixException(e);
        }
    }

    public static Location getFacedLocation(Player p, double distance) {
        return getFacedLocation(p.getLocation(), distance);
    }

    public static Location getFacedLocation(Location l, double distance) {
        return l.add(l.getDirection().normalize().multiply(distance));
    }

    public static boolean isFakePlayer(Player player) {
        return isFakeChannel(ProtocolManager.CHANNELS.get(player.getUniqueId()));
    }

    //From PacketEvents
    public static boolean isFakeChannel(Object channel) {
        if (channel == null) return true;
        switch (channel.getClass().getSimpleName()) {
            case "FakeChannel":
            case "SpoofedChannel":
                return true;
            default:
                return false;
        }
    }

    public static String getFields(Object o) {
        StringBuilder sb = new StringBuilder();
        Class<?> c = o.getClass();
        do {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                try {
                    Object w = f.get(o);
                    sb.append(f.getName()).append(": ").append(w.getClass().isArray() ? Arrays.toString((Object[]) w) : w).append(", ");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        } while ((c = c.getSuperclass()) != Object.class);
        return sb.toString();
    }

/*    public static String getFieldsDeep(Object o) {
        StringBuilder sb = new StringBuilder();
        Class<?> c = o.getClass();
        if (c.getPackageName().startsWith("java")) return "";
        do {
            sb.append("[");
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().getPackageName().startsWith("java")) continue;
                f.setAccessible(true);
                try {
                    sb.append(f.getName()).append(": ").append(f.get(o)).append(", ");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            sb.append("]");
        } while ((c = c.getSuperclass()) != Object.class);
        return sb.toString();
    }*/

/*    public static void change(Player player) {
        if (player.getInventory().getHelmet().getType() == Material.PLAYER_HEAD) //Checking if the player wears a head
        {
            player.getInventory().setHelmet(null); //setting the Helmet slot to empty
        }
        ItemStack head = new ItemStack(Material.PLAYER_HEAD); //Creating a new player Head
        SkullMeta skullmetaGlobe = getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI" +
                "6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODc5ZTU0Y2JlODc4NjdkMTRiMmZiZGYzZjE4NzA4OTQzNTIwNDhkZmVj" +
                "ZDk2Mjg0NmRlYTg5M2IyMTU0Yzg1In19fQ==", "Global Stats", head); // Setting the custom texture from the website
        skullmetaGlobe.setDisplayName("Iron Man"); // Setting the heads name
        head.setItemMeta(skullmetaGlobe); // Setting the texture to the player head
        player.getInventory().setHelmet(head); // Setting the player head to the helmet slot of the player
    }*/

/*    public static Image resizeImageToMapScale(Image originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate the scale factor to fit the image within the map's dimensions
        double scale = Math.min(128.0 / originalWidth, 128.0 / originalHeight);

        // Calculate the new width and height after resizing
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // Create a new BufferedImage with the resized dimensions
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        // Draw the original image onto the resized image with scaling
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, newWidth, newHeight, null);

        // Save the resized image to a new file
        File outputFile = new File("path/to/your/resized/image.png");
        ImageIO.write(resizedImage, "png", outputFile);
    }*/

/*    //bound is 2^n, with n∈N+ & N+ being {1, 2, 3, ...}
    public static int fastRandomInt(int bound) {
        return
    }*/

/*    public static void logException(Throwable e) {
        Main.logError("An AlixTask has thrown an exception - Report this immediately!");
        e.printStackTrace();
    }*/

/*    public static boolean isGuiUser(PersistentUserData data) {
        return data != null ? data.getPasswordType() == PasswordType.PIN : fancyPasswordGui || defaultPasswordType == PasswordType.PIN;
    }*/

    private static final String pinTypeInvalid = Messages.getWithPrefix("gui-pin-type-invalid");

    public static String getPasswordInvalidityReason(String password, LoginType type) {
        if (type == LoginType.PIN) //if the login type is pin, ensure the password is also a pin
            return AlixUtils.isPIN(password) ? null : pinTypeInvalid;

        return AlixUtils.getInvalidityReason(password, false);
    }

    public static String formatMillis(long millis) {
        //if (millis % 1000 == 0) return millis + " second" + (millis / 1000 > 1 ? "s" : "");
        float div = millis / 1000f;

        return div + " second" + (millis != 1000 ? "s" : "");
    }

    public static boolean equalsArrayCheck(Object[] a1, Object[] a2) {
        if (a1.length != a2.length) return false;
        for (int i = 0; i < a1.length; i++) if (!Objects.equals(a1[i], a2[i])) return false;
        return true;
    }

/*    public static boolean cancelCommandSend(UnverifiedUser user, String cmd) {
        if (user == null) return false;
        if (user.isPasswordBuilderGUIInitialized()) return true;
        //if (AlixCommandManager.isLoginCommand(split(cmd, ' ')[0])) return false;
        return !AlixCommandManager.isLoginCommand(split(cmd, ' ')[0]);//return true;
        //return user.isPinGUIInitialized() || !startsWith(cmd, "login", "loguj", "zaloguj", "register", "reg", "zarejestruj", "captcha");
    }*/

    private static final SkullSupplier skullSupplier = SkullSupplier.createImpl();

    public static ItemStack getSkull(String url) {
        return skullSupplier.createSkull(url);
    }

    public static ItemStack getSkull(String name, String url) {
        ItemStack head = skullSupplier.createSkull(url);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(AlixFormatter.translateColors(name));
        head.setItemMeta(meta);
        return head;
    }

    public static List<String> sort(Collection<String> collection) {
        List<String> list = new ArrayList<>(collection);
        Collections.sort(list);
        return list;
    }

/*    public static void checkAuthentication(Player p) {
        try {
            Object entityPlayer = p.getClass().getMethod("getHandle").invoke(p);
            GameProfile profile = (GameProfile) ReflectionUtils.getProfile.invoke(entityPlayer);
            Bukkit.broadcastMessage(profile.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    public static String convertToFormattedUUID(String id) {
        StringBuilder sb = new StringBuilder(id);
        sb.insert(20, "-");
        sb.insert(16, "-");
        sb.insert(12, "-");
        sb.insert(8, "-");
        return sb.toString();
    }

    public static boolean isAccountPremium2(String name) {
        try {
            JsonElement e = HttpsHandler.readURL("https://api.mojang.com/users/profiles/minecraft/" + name);
            String id = e.getAsJsonObject().get("id").getAsString();
            UUID uuid = UUID.fromString(convertToFormattedUUID(id));
            Player p = Bukkit.getPlayerExact(name);
            //Bukkit.broadcastMessage(uuid + " " + uuid.version() + " " + ReflectionUtils.isAuthenticatedWithMojang(p));
            return uuid.version() == 4;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isUsernamePremium(String name) {
        try {
            URL url = new URL("https://api.ashcon.app/mojang/v2/user/" + name);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            return connection.getResponseCode() == 200;//HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean commandExists(String command) {
        //return Bukkit.getPluginCommand(command) != null;
        return ReflectionUtils.commandMap.getCommand(command) != null;
    }

    public static String unslashify(String a) {
        return a.charAt(0) == '/' ? a.substring(1) : a;
    }

    public static String getAllUntilCharFoundCharIncluded(String a, char b) {
        char[] c = a.toCharArray();
        for (int i = 0; i < c.length; i++)
            if (c[i] == b)
                return a.substring(i + 1);//i + 1 is the index till the char including the char
        return null;
    }

    public static String getAllUntilCharFoundCharExcluded(String a, char b) {
        char[] c = a.toCharArray();
        for (int i = 0; i < c.length; i++)
            if (c[i] == b)
                return a.substring(i);//i is the index till the char excluding the char
        return null;
    }

    public static String getClassDirectory(Class<?> e) {
        return e.getPackage().toString().substring(8) + '.' + e.getSimpleName();
    }

    public static String nullifyIfNullEquivalent(String s) {
        return s.equals("null") ? null : s;
    }

/*    public static double getDamageReduced(Player player) {//some random guy made this at https://www.spigotmc.org/threads/get-armor-defense-points-from-a-player.153971/
        PlayerInventory inv = player.getInventory();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack boots = inv.getBoots();
        ItemStack pants = inv.getLeggings();
        double armor = 0.0;
        //
        if(helmet != null) {
            if (helmet.getType() == Material.LEATHER_HELMET) armor += 0.04;
            else if (helmet.getType() == Material.GOLDEN_HELMET) armor += 0.08;
            else if (helmet.getType() == Material.CHAINMAIL_HELMET) armor += 0.08;
            else if (helmet.getType() == Material.IRON_HELMET) armor += 0.08;
            else if (helmet.getType() == Material.DIAMOND_HELMET) armor += 0.12;
        }
        //
        if(chest != null) {
            if (chest.getType() == Material.LEATHER_CHESTPLATE)    armor += 0.12;
            else if (chest.getType() == Material.GOLDEN_CHESTPLATE)armor += 0.20;
            else if (chest.getType() == Material.CHAINMAIL_CHESTPLATE) armor += 0.20;
            else if (chest.getType() == Material.IRON_CHESTPLATE) armor += 0.24;
            else if (chest.getType() == Material.DIAMOND_CHESTPLATE) armor += 0.32;
        }
        //
        if(pants != null) {
            if (pants.getType() == Material.LEATHER_LEGGINGS) armor += 0.08;
            else if (pants.getType() == Material.GOLDEN_LEGGINGS)    armor += 0.12;
            else if (pants.getType() == Material.CHAINMAIL_LEGGINGS) armor += 0.16;
            else if (pants.getType() == Material.IRON_LEGGINGS)    armor += 0.20;
            else if (pants.getType() == Material.DIAMOND_LEGGINGS) armor += 0.24;
        }
        //
        if(boots != null) {
            if (boots.getType() == Material.LEATHER_BOOTS) armor += 0.04;
            else if (boots.getType() == Material.GOLDEN_BOOTS) armor += 0.04;
            else if (boots.getType() == Material.CHAINMAIL_BOOTS) armor += 0.04;
            else if (boots.getType() == Material.IRON_BOOTS) armor += 0.08;
            else if (boots.getType() == Material.DIAMOND_BOOTS)    armor += 0.12;
        }
        //
        return armor;
    }*/

    public static char[] getCharsUntilCharOrSelfIfNone(char[] a, char breaker) {
        for (int i = 0; i < a.length; i++)
            if (a[i] == breaker)
                return Arrays.copyOfRange(a, 0, i);
        return a;
    }

    public static Character[] toObject(char... a) {
        int l = a.length;
        Character[] b = new Character[l];
        for (int c = 0; c < l; c++) b[c] = a[c];
        return b;
    }

    public static char[] toPrimitive(Character... a) {
        int l = a.length;
        char[] b = new char[l];
        for (int c = 0; c < l; c++) b[c] = a[c];
        return b;
    }

    public static Integer[] toObject(int... a) {
        int l = a.length;
        Integer[] b = new Integer[l];
        for (int c = 0; c < l; c++) b[c] = a[c];
        return b;
    }

    public static int[] toPrimitive(Integer... a) {
        int l = a.length;
        int[] b = new int[l];
        for (int c = 0; c < l; c++) b[c] = a[c];
        return b;
    }

    public static void fill(StringBuilder builder, char from, char to) {
        if (from >= to)
            throw new RuntimeException("Invalid from " + from + " & to " + to + ": from is greater than to!");
        if (to - from > 32767)
            throw new RuntimeException("Invalid from & to: too great char difference! - " + (to - from));
        int since = 0;
        for (; ; ) {
            char newChar = (char) (since++ + from);
            builder.append(newChar);
            if (newChar == to) break;
        }
    }

    public static boolean isPIN(String password) {
        if (password.length() != 4) return false;
        char[] c = password.toCharArray();
        for (char d : c)
            if (d < 48 || d > 57) return false;
        return true;
    }

    public static String getInvalidityReason(String text, boolean canBeShort) {
        int l = text.length();
        if (l > 30) return tooLongMessage;
        if (!canBeShort && l < 5) return tooShortMessage;
        char[] b = text.toCharArray();
        for (char c : b) {
            switch (c) {
                case 58:
                case 59:
                    return AlixFormatter.format(invalidCharacterMessage, c);
                default:
                    if (c < 35 || c > AlixFileManager.HIGHEST_CHAR || c > 90 && !Character.isLetter(c))
                        return AlixFormatter.format(invalidCharacterMessage, c);
            }
        }
        return null; //The given text is valid
    }

/*    public static boolean isPasswordInvalid(String text) {
        int l = text.length();
        if (l > 30 || l < 5) return true;
        char[] b = text.toCharArray();
        for (char c : b) {
            switch (c) {
                //case 32:
                case 58:
                case 59:
                    return true;
                default:
                    if (c < 35 || c > 382 || c > 90 && !Character.isLetter(c))
                        return true;
            }
        }
        return false; //The given text is valid
    }*/

    public static <T> boolean contains(List<T> list, T value) {
        for (T v : list) if (v.equals(value)) return true;
        return false;
    }

    public static final ByteBuf
            notLoggedInUserMessagePacket = OutMessagePacketConstructor.constructConst(Messages.notLoggedInUserMessage, true, true),
            captchaNotCompletedUserMessagePacket = OutMessagePacketConstructor.constructConst(Messages.captchaNotCompletedUserMessage, true, true),
            unregisteredUserMessagePacket = OutMessagePacketConstructor.constructConst(requirePasswordRepeatInRegister ? Messages.get("unregistered-reminder-password-repeat") : Messages.get("unregistered-reminder"), true, true);

    public static ByteBuf getVerificationReminderMessagePacket(boolean isRegistered, boolean hasAccount) {
        if (isRegistered) return notLoggedInUserMessagePacket;//is registered - require login
        if (hasAccount)
            return unregisteredUserMessagePacket;//isn't registered, but has an account - the password must've been reset
        return requireCaptchaVerification ? captchaNotCompletedUserMessagePacket : unregisteredUserMessagePacket;//not registered - require captcha if it's ON
    }

    public static void sendMessage(CommandSender p, String message, Object... a) {
        /*if (p instanceof Conversable) ((Conversable) p).sendRawMessage(AlixFormatter.format(colorize(message)));
        else */
        p.sendMessage(AlixFormatter.format(colorize(message), a));
    }

    public static void sendMessage(CommandSender p, String message) {
        /*if (p instanceof Conversable) ((Conversable) p).sendRawMessage(colorize(message));
        else */
        p.sendMessage(colorize(message));
    }

    public static void setName(Player p, String name) {
        p.setCustomName(name);
        p.setDisplayName(name);
        p.setPlayerListName(name);
    }

    public static boolean canSendColoredChatMessages(Player p) {
        return p.hasPermission("alixsystem.chatcolor");
    }

    public static boolean hasChatBypass(Player p) {
        return /*p.isOp() ||*/ p.hasPermission("alixsystem.admin.chat.bypass");
    }

    public static short getMaxHomes(Player p) {
        //if (p.isOp()) return 32767;
        if (!p.hasPermission("alixsystem.home")) return 0;
        String a = getPermissionWithCertainStart(p, "alixsystem.maxhomes");
        if (a == null) return 0;
        String[] b = split(a, '.');
        if (b.length < 3) return 0;
        String c = b[2];
        int i;
        try {
            i = parseInteger(c);
        } catch (NumberFormatException e) {
            Main.logWarning(isPluginLanguageEnglish ? "Invalid permission 'alixsystem.maxhomes.<number>' - instead of a number got: '" + a + "'!" :
                    "Nieprawidłowa permisja 'alixsystem.maxhomes.<liczba>' - zamiast liczby otrzymano: '" + a + "'!");
            return 0;
        }
        if (i > 32767) return 32767;
        return (short) Math.max(i, 0);
    }

/*    public static boolean isReloadCommandHandled(String cmd) {
        switch (cmd) {
            case "rl":
            case "reload":
            case "minecraft:rl":
            case "minecraft:reload":
                JavaHandler.handleReload();
                return true;
            default:
                return false;
        }
    }

    public static void handleReloadCommand(String cmd) {
        switch (cmd) {
            case "rl":
            case "reload":
            case "minecraft:rl":
            case "minecraft:reload":
                JavaHandler.handleReload();
        }
    }*/

    public static String removeSlash(String cmd) {
        return cmd.charAt(0) == '/' ? cmd.substring(1) : cmd;
    }

    public static int getMedian(int[] a) {
        Arrays.sort(a);
        int l = a.length;
        int l2 = l / 2;
        if (l % 2 != 0) return (a[l2] + a[l2 + 1]) / 2;
        else return a[l2];
    }

    public static int[] parseArray(String[] a) {
        int l = a.length;
        int[] b = new int[l];
        for (int i = 0; i < l; i++) b[i] = Integer.parseInt(a[i]);
        return b;
    }

    public static void ban(String nameOrIp, String reason, Date date, boolean ip, String byWho) {
        reason = translateColors(reason);

        BukkitBanList.get(ip).ban(nameOrIp, reason, date, byWho);
        if (ip) return;
        Player p = Bukkit.getPlayer(nameOrIp);
        if (p != null) p.kickPlayer(reason);
    }

    public static void unban(String name, boolean ip) {
        BukkitBanList.get(ip).unban(name);
    }

    public static boolean isAlreadyOnline(String name) {
        return Bukkit.getPlayerExact(name) != null;
    }

/*    public static BookMeta getFilledBookMeta(BookMeta a, String b) {
        int c = 1;
        while (b.length() > 0) {
            a.addPage(b);
            b = b.substring(a.getPage(c++).length());
        }
        return a;
    }*/

    public static boolean checkBooleanString(String a, Player p) {
        if (a != null) {
            if (a.equalsIgnoreCase("true")) return true;
            if (a.equalsIgnoreCase("false")) return false;
            p.sendMessage(isPluginLanguageEnglish ? "Expected true/false statement" : "Oczekiwano argumentu true/false");
            return random.nextBoolean();
        }
        throw new RuntimeException("Boolean check of nullified string");
    }

    public static void setSkin(Player p, JsonObject texture) {
/*        try {
            Object nmsPlayer = ReflectionUtils.getHandle.invoke(p);
            GameProfile profile = (GameProfile) ReflectionUtils.getProfile.invoke(nmsPlayer);

            Collection<Object> playerCollection = Collections.singleton(nmsPlayer);
            Object removePlayerPacket = ReflectionUtils.packetPlayOutPlayerInfoConstructor.newMain.instance(ReflectionUtils.REMOVE_PLAYER, playerCollection);
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", texture.get("value").getAsString(), texture.get("signature").getAsString()));
            Object addPlayerPacket = ReflectionUtils.packetPlayOutPlayerInfoConstructor.newMain.instance(ReflectionUtils.ADD_PLAYER, playerCollection);

            final Player pla = p;
            Location loc = p.getLocation();
            Location newLoc = new Location(getOtherWorld(p), 0, 0, 0);

            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, () -> pla.teleport(newLoc));

            for (Player player : Bukkit.getOnlinePlayers()) {
                Object nmsP = ReflectionUtils.getHandle.invoke(player);
                Object pConnection = ReflectionUtils.playerConnectionField.get(nmsP);
                Object nManager = ReflectionUtils.networkManagerField.get(pConnection);
                Channel pChannel = (Channel) ReflectionUtils.channelField.get(nManager);
                pChannel.write(removePlayerPacket);
                pChannel.write(addPlayerPacket);
                pChannel.flush();
            }

            Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                pla.teleport(loc);
                pla.updateInventory();
            }, 1L);
        } catch (Throwable e) {
            e.printStackTrace();
        }*/
    }

    public static int getCharsCount(String a, char b) {
        int c = 0;
        char[] d = a.toCharArray();
        for (char e : d) if (e == b) c++;
        return c;
    }

    public static String getRandomMathematicalOperation() {
        String operation = getRandomMathematicalOperation(getRandom(3, 7), getRandom(3, 10), getRandom(10, 30));
        String result = split(operation, " = ")[1];
        int n = result.contains(".") ? split(result, '.')[1].length() : 0;//amount of decimal places
        if (n > 2) return getRandomMathematicalOperation();//Too complex, try again
        return operation;
    }

    public static String getRandomMathematicalOperation(int numberOfNumbers, int least, int max) {
        int[] a = new int[numberOfNumbers];
        for (int i = 0; i < numberOfNumbers; i++) a[i] = getRandom(least, max);
        char[] s = new char[numberOfNumbers - 1];
        char[] mathOperators = "+-*/^".toCharArray();
        for (int i = 0; i < s.length; i++) s[i] = mathOperators[getRandom(0, mathOperators.length - 1)];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfNumbers; i++) {
            sb.append(a[i]);
            if (i < s.length) sb.append(s[i]);
        }
        String result = setAsClearNumber(eval(sb.toString()));
        return sb.append(" = ").append(result).toString();
    }

    public static String getPermissionWithCertainStart(Player a, String b) {
        for (PermissionAttachmentInfo c : a.getEffectivePermissions()) {
            String d = c.getPermission();
            if (d.startsWith(b)) return d;
        }
        return null;
    }

    public static World getOtherWorld(Player player) {
        for (World world : Bukkit.getWorlds())
            if (!player.getWorld().equals(world))
                return world;
        throw new RuntimeException("[AlixSystem] > Other world not found!");
    }

    public static JsonObject parseTexture(String skinName) {
        try {
            JsonElement session = HttpsHandler.readURL("https://api.mojang.com/users/profiles/minecraft/" + skinName);
            if (session == null) return null;
            JsonObject sessionJson = session.getAsJsonObject();
            String id = sessionJson.get("id").getAsString();

            JsonElement texture = HttpsHandler.readURL("https://sessionserver.mojang.com/session/minecraft/profile/" + id + "?unsigned=false");
            if (texture == null) return null;
            return texture.getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
        } catch (IOException e) {
            Main.logWarning("Error in parsing texture!");
            e.printStackTrace();
        }
        return null;
    }

    @AlixIntrinsified(method = "String#split")
    public static String[] split(String text, String regex) {
        int regexLength = regex.length();
        switch (regexLength) {
            case 0:
                return new String[]{text};
            case 1:
                return split(text, regex.charAt(0));
        }
        char[] textArray = text.toCharArray();
        char[] regexArray = regex.toCharArray();
        final int txtLM1 = textArray.length - 1;
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(), secondSB = new StringBuilder();
        short j = 0;
        for (short i = 0; i < textArray.length; i++) {
            char currentTextLetter = textArray[i];
            if (currentTextLetter == regexArray[j++]) {
                if (j == regexLength) {
                    list.add(sb.toString());
                    sb = new StringBuilder();
                    secondSB = new StringBuilder();
                    j = 0;
                } else secondSB.append(currentTextLetter);
            } else {
                String secondSBText = secondSB.toString();
                if (!secondSBText.isEmpty()) sb.append(secondSBText);
                sb.append(currentTextLetter);
                secondSB = new StringBuilder();
                j = 0;
            }

            if (i == txtLM1) list.add(sb.toString());
        }
        return list.toArray(new String[0]);
    }

    //A faster String#split implementation
    //for non-complex Strings
    @AlixIntrinsified(method = "String#split")
    public static String[] split(String a, char b) {
        char[] c = a.toCharArray();
        int lM1 = c.length - 1;
        int regexes = getCharsCount(c, b);//it's usually faster to count the array's size rather than resize it
        int j = 0, k = 0, n = c.length - regexes;//'n' is the known amount of chars left
        String[] d = new String[regexes + 1];//we already know how big the array should be
        char[] e = new char[n];
        for (int i = 0; i < c.length; i++) {
            char f = c[i];
            if (f != b) e[k++] = f;//it's more common for the char to not be the regex
            else {
                d[j++] = new String(e, 0, k);
                n -= k;
                e = new char[n];
                k = 0;
            }
            if (i == lM1) d[j++] = new String(e, 0, k);//apply the remaining chars
        }
        return d;
    }

/*    @AlixIntrinsified
    public static String[] split(String a, char b, int assumedLength) {
        char[] c = a.toCharArray();
        final int lM1 = c.length - 1;
        int stringArrayIndex = 0, currentStringLength = 0, totalCharsLeft = c.length - assumedLength;
        String[] stringArray = new String[assumedLength];
        char[] charsInCurrentString = new char[totalCharsLeft];
        for (int i = 0; i < c.length; i++) {
            char f = c[i];
            if (f == b) {
                stringArray[stringArrayIndex++] = new String(charsInCurrentString, 0, currentStringLength);
                totalCharsLeft -= currentStringLength;
                charsInCurrentString = new char[totalCharsLeft];
                currentStringLength = 0;
            } else charsInCurrentString[currentStringLength++] = f;
            if (i == lM1) stringArray[stringArrayIndex++] = new String(charsInCurrentString, 0, currentStringLength);
        }
        return stringArray;
    }*/

    public static int getCharsCount(char[] a, char b) {
        int c = 0;
        for (char d : a) if (d == b) c++;
        return c;
    }

    public static String[] splitPersistentData(String data) {
        return split(data, '|');
    }

    @AlixIntrinsified(method = "String#contains")
    public static boolean contains(char[] a, String regex) {
        char[] b = regex.toCharArray();
        int i = 0;
        for (char c : a) {
            if (c == b[i++]) {
                if (i == b.length) return true;
            } else i = 0;
        }
        return false;
    }

    public static int getRandom(int from, int to) {
        //return (int) (Math.random() * (to - from)) + from;
        return random.nextInt(to - from) + from;
    }

    public static double getRandom(double from, double to) {
        return random.nextDouble() * (to - from) + from;
    }

    @AlixIntrinsified(method = "Integer#parseInteger")
    public static int parseInteger(String a) throws NumberFormatException {
        if (a.charAt(0) == 45) return -parseInteger(a.substring(1));
        char[] b = a.toCharArray();
        int c = 0;
        for (char value : b) {
            int d = value - 48;
            if (d < 0 || d > 9)
                throw new NumberFormatException("[AlixSystem] > Invalid symbol found in string '" + a + "': " + (char) d);
            c = c * 10 + d;
        }
        return c;
    }

    public static int parsePureInteger(String a) {
        if (a.charAt(0) == 45) return -parsePureInteger(a.substring(1));
        char[] b = a.toCharArray();
        int c = 0;
        for (char value : b) c = c * 10 + value - 48;
        return c;
    }

    public static boolean isConsoleButPlayerRequired(CommandSender sender) {
        boolean console = !(sender instanceof Player);
        if (console)
            Main.logInfo(isPluginLanguageEnglish ? "Console cannot use that command!" : "Konsola nie może używać tej komendy!");
        return console;
    }

    public static boolean dispatchServerCommand(String command) {
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public static Date getProcessedDate(String a) {
        return new Date(getProcessedTimePlusNow(a));
    }

    public static long getProcessedTimePlusNow(String a) {
        //The Given Time * Unit + The Current Moment
        return getProcessedTime(a) + System.currentTimeMillis();
    }

    public static long getProcessedTime(String a) {
        char[] chars = a.toCharArray();
        StringBuilder numbersOnly = new StringBuilder();
        StringBuilder lettersOnly = new StringBuilder();
        for (char d : chars) {
            if (d < 48 || d > 57) lettersOnly.append(d);
            else numbersOnly.append(d);
        }
        //The Given Time * Unit
        return parsePureLong(numbersOnly.toString()) * getTimeUnitMultiplier(lettersOnly.toString());
    }

/*    public static String getTextWithNumbersOnly(String a) {
        char[] b = a.toCharArray();
        StringBuilder c = new StringBuilder();
        for (char d : b) if (d >= 48 && d <= 57) c.append(d);
        return c.toString();
    }

    public static String getTextWithoutNumbers(String a) {
        char[] b = a.toCharArray();
        StringBuilder c = new StringBuilder();
        for (char d : b) if (d < 48 || d > 57) c.append(d);
        return c.toString();
    }*/

    public static long parsePureLong(String a) {
        if (a.charAt(0) == 45) return -parsePureLong(a.substring(1));
        char[] b = a.toCharArray();
        long c = 0L;
        for (char value : b) {
/*            int d = value - 48;
            if (check && (d < 0 || d > 9))
                throw new RuntimeException("Invalid symbol found in string '" + a + "': " + (char) d);*/
            c = c * 10 + value - 48;
        }
        return c;
    }

    public static String getFormattedDate(Date date) {
        return dateFormatter.format(date);
    }

    public static String getTime(Date date) {
        return timeFormatter.format(date);
    }

    public static boolean isValidIP(String text) {
        return ipPattern.matcher(text).matches();
    }

    public static String mergeWithSpaces(String[] a) {
        return mergeWithSpacesAndSkip(a, 0);
    }

    @AlixIntrinsified(method = "StringJoiner#merge")
//not necessarily this specific method, but rather used symbolically for all joiners
    public static String mergeWithSpacesAndSkip(String[] a, int toSkip) {
        StringBuilder sb = new StringBuilder((a.length - toSkip) << 3);//the estimated length assumes that each String is 8 chars long on average
        final int lM1 = a.length - 1;
        for (int i = toSkip; i < a.length; i++) {
            sb.append(a[i]);
            if (i != lM1) sb.append(' ');
        }
        return sb.toString();
    }

    public static String setAsOne(String[] a) {
        StringBuilder b = new StringBuilder();
        for (String c : a) b.append(c);
        return b.toString();
    }

    public static String setAsOneAndAddAfter(String[] a, String b) {
        StringBuilder c = new StringBuilder();
        int l = a.length;
        for (int d = 0; d < l; d++) {
            String e = a[d];
            if (d != l - 1) c.append(e).append(b);
            else c.append(e);
        }
        return c.toString();
    }

    //Lis \/
    public static String[] skipArray(String[] array, int toSkip) {
        int skip = Math.min(array.length, Math.max(0, toSkip));
        String[] strings = new String[array.length - skip];
        System.arraycopy(array, skip, strings, 0, strings.length);
        return strings;
    }
    //   /\

    public static float getPercentOfMemoryUsage(long[] memory) {
        float freeMemory = memory[0];
        float totalMemory = memory[2];
        return round(100 * (totalMemory - freeMemory) / totalMemory, 2);
    }

    public static GameMode getGameModeType(String name) {
        if (name.startsWith("sp")) return GameMode.SPECTATOR;
        switch (name.charAt(0)) {
            case 's':
                return GameMode.SURVIVAL;
            case 'a':
                return GameMode.ADVENTURE;
            default:
                return GameMode.CREATIVE;
        }
    }

    public static String getColorizationToMemoryUsage(float usage) {
        return usage < 35.0f ? "&a" : usage < 55.0f ? "&e" : usage < 75.0f ? "&6" : usage < 95.0f ? "&c" : "&4";
    }

    public static long[] getMemory() {
        Runtime run = Runtime.getRuntime();
        long division = 1048576L;
        return new long[]{run.freeMemory() / division, run.maxMemory() / division, run.totalMemory() / division};
    }

    public static float round(float a, int b) {
        float c = powerIntegers(10, b);
        float d = a * c;
        int e = (int) d;
        return (e + Math.round(d - (long) d)) / c;
    }

    public static int powerIntegers(int a, int b) {
        switch (b) {
            case 0:
                return 1;
            case 1:
                return a;
        }
        int c = a;
        for (int d = 1; d < b; d++) c *= a;
        return c;
    }

    public static String getListOfAllOnlinePlayers(Collection<? extends Player> onlines) {
        int l = onlines.size();
        Player[] players = onlines.toArray(new Player[l]);
        StringBuilder sb = new StringBuilder(l * 3 + 16);
        for (int d = 0; d < l; d++) {
            Player e = players[d];
            if (d != l - 1) sb.append(e.getName()).append(", ");
            else sb.append(e.getName());
        }
        return sb.toString();
    }

    public static boolean contains(String a, char b) {
        char[] c = a.toCharArray();
        for (char d : c) if (d == b) return true;
        return false;
    }

    public static String firstCharToUpperCase(String a) {
        return Character.toUpperCase(a.charAt(0)) + a.substring(1);
    }

    public static String toUpperCaseIfCharFound(String a, char b) {
        char[] c = a.toCharArray();
        int l = c.length;
        for (int d = 0; d < l; d++) {
            int e = d + 1;
            if (c[d] == b && e < l) c[e] = Character.toUpperCase(c[e]);
        }
        return new String(c);
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        return Bukkit.getOfflinePlayer(name);
    }

    public static <T> T[] getArrayWithoutNulls(T[] array, T[] newArray) {
        List<T> list = new ArrayList<>();
        for (T t : array) if (t != null) list.add(t);
        return list.toArray(newArray);
    }

    public static boolean startsWith(String a, String... b) {
        for (String c : b) if (a.startsWith(c)) return true;
        return false;
    }

    public static boolean equalsToAtLeastOne(String a, String... b) {
        for (String c : b) if (a.equals(c)) return true;
        return false;
    }

    public static List<String> getItemLore(String... lore) {
        return Arrays.asList(translateArrayColorsAndTrimEach(lore));
    }

    public static String[] translateArrayColorsAndTrimEach(String... texts) {
        for (int i = 0; i < texts.length; i++) texts[i] = translateColors(texts[i]).trim();
        return texts;
    }

    public static String translateColors(String text) {
        return AlixFormatter.translateColors(text);//The default delegation to AlixFormatter
    }

/*    public static String formatIfNeeded(String text) {
        return contains(text, '[') ? text : format(text);
    }*/

    public static String colorize(String text) {
        return AlixFormatter.appendPrefix(translateColors(text));
    }

    public static void serverLog(String info) {
        Bukkit.getConsoleSender().sendMessage(info);
        //Bukkit.getServer().getLogger().info(info);
    }

    public static void broadcastRaw(String info) {
        //Bukkit.broadcastMessage(translateColors(info));
        Bukkit.getServer().getLogger().info(info);
        sendToAllPlayers(info);
    }

    public static void broadcastColorized(String info) {
        //Bukkit.broadcastMessage(translateColors(info));
        Main.logInfo(info);
        sendToAllPlayers(colorize(info));
    }

    public static void broadcastColorizedToPermitted(String info) {
        //Bukkit.broadcast(translateColors(info), "javasystem.info");
        Main.logInfo(info);
        sendToAllPermittedPlayers(colorize(info));
    }

/*    public static void debug(Object... message) {
        debug(message, ' ');
    }*/

    public static <T> void debug(T[] message, Function<T, String> formatting, char separator) {
        StringBuilder sb = new StringBuilder();
        for (T o : message) sb.append(formatting.apply(o)).append(separator);
        Main.logInfo(sb.toString());
        //sendToAllPlayers(sb.toString());
    }

    public static <T> void debug(T[] message, Function<T, String> formatting) {
        debug(message, formatting, '\n');
    }

    public static <T> void debug(T[] message) {
        debug(message, T::toString, '\n');
    }

    public static void sendToAllPlayers(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) p.sendRawMessage(message);
    }

    public static void sendToAllPermittedPlayers(String message) {
        for (Player p : Bukkit.getOnlinePlayers())
            if (p.isOp()/* || p.hasPermission("alixsystem.info")*/) p.sendMessage(message);
    }

    public static String setAsClearNumber(Object a) {
        String b = new BigDecimal(a.toString()).toPlainString();
        String c = contains(b, '.') ? substringFromLastWhileLastCharacterFound(b, '0') : b;
        return lastChar(c) == '.' ? substringFromLast(c, 1) : c;
    }

    private static String substringFromLastWhileLastCharacterFound(String a, char b) {
        while (lastChar(a) == b) a = substringFromLast(a, 1);
        return a;
    }

    private static char lastChar(String a) {
        return a.charAt(a.length() - 1);
    }

    public static String substringFromLast(String s, int a) {
        return s.substring(0, Math.max(0, s.length() - a));
    }

    public static float clampOfOne(float n) {
        return n > 1 ? 1 : n < -1 ? -1 : n;
    }

    public static boolean isNumber(String a) {
        try {
            Double.parseDouble(a);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static double safeParse(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return -0;
        }
    }

    public static double eval(final String str) throws NumberFormatException {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) return Double.NaN;
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = safeParse(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    switch (func) {
                        case "sqrt":
                            x = Math.sqrt(x);
                            break;
                        case "cbrt":
                            x = Math.cbrt(x);
                            break;
                        case "sin":
                            x = Math.sin(x);
                            break;
                        case "cos":
                            x = Math.cos(x);
                            break;
                        case "tan":
                            x = Math.tan(x);
                            break;
                        case "atan":
                            x = Math.atan(x);
                            break;
                        case "acos":
                            x = Math.acos(x);
                            break;
                        case "toRadians":
                            x = Math.toRadians(x);
                            break;
                        case "toDegrees":
                            x = Math.toDegrees(x);
                            break;
                        case "log":
                            x = Math.log(x);
                            break;
                        case "floor":
                            x = Math.floor(x);
                            break;
                        default:
                            throw new NumberFormatException(isPluginLanguageEnglish ? "Invalid function '" + func + "'!" : "Nieprawidłowa funckja '" + func + "'!");
                    }
                } else
                    throw new NumberFormatException(isPluginLanguageEnglish ? "Invalid syntax!" : "Nieprawidłowy syntax!");

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

/*    private static Language getPluginLanguage(String lang) {
        Language l = Language.getByShortcut(lang);
        if (l != Language.UNKNOWN) {
            Main.logInfo("Using " + firstCharToUpperCase(l.name().toLowerCase()) + " (" + l.getShortcut().toUpperCase()
                    + ") as the plugin language (found by the language's shortcut).");
            return l;
        }
        Language l2 = Language.getByName(lang);
        if (l2 != Language.UNKNOWN) {
            Main.logInfo("Using " + firstCharToUpperCase(l.name().toLowerCase()) + " (" + l.getShortcut().toUpperCase()
                    + ") as the plugin language (found by the language's name).");
            return l2;
        }
        Language l3 = Language.getMostSimilar(lang);
        if (l3 != Language.UNKNOWN) {
            Main.logInfo("Using " + firstCharToUpperCase(l.name().toLowerCase()) + " (" + l.getShortcut().toUpperCase()
                    + ") as the plugin language (found as the most similar - name and shortcut were not recognized).");
            return l3;
        }
        Main.logWarning("Unable to recognize the plugin language set in the config. Using english, as default.");
        return Language.ENGLISH;
    }*/

    /*private static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23).replaceAll("_", ".").
                replaceAll("R", "").replaceAll("v", "");
    }

    private static int getBukkitVersion() {
        return parseInteger(getServerVersion().split("\\.")[1]);
    }*/

    private static int getLowestTeleportableLevel() {
        return AlixWorldHolder.getMain().getMaxHeight() >= 319 ? -66 : -2;
    }

/*    private static void implementConfig(FileConfiguration config) {
        isPluginLanguageEnglish = true;
        switch (config.getString("language").toLowerCase()) {
            case "en":
                break;
            case "pl":
                isPluginLanguageEnglish = false;
                break;
            default:
                Main.logWarning("Invalid language type in config! Available: en & pl, but instead got '" +
                        config.getString("language") + "'! Switching to english, as default.");
                break;
        }
        isLoginRestrictPacketBased = true;
        switch (config.getString("login-restrict-base").toLowerCase()) {
            case "packet":
                break;
            case "teleport":
                isLoginRestrictPacketBased = false;
                break;
            default:
                Main.logWarning("Invalid 'login-restrict-base' parameter type set in config! Available: packet & teleport, but instead got '" +
                        config.getString("login-restrict-base") + "'! Switching to 'packet' for safety reasons.");
                break;
        }
        isOfflineExecutorRegistered = config.getBoolean("offline-login-requirement") && !Main.instance.getServer().getOnlineMode();
        kickOnIncorrectPassword = config.getBoolean("kick-on-incorrect-password");
        playerIPAutoLogin = config.getBoolean("auto-login");
        timeBeforeNotLoggedInKick = (short) Math.min(Math.max((short) config.getInt("time-before-kick"), 3), 1638); //32767 / 20 = 1638.35, also 1638 % 3 = 0
        if (isLoginRestrictPacketBased) timeBeforeNotLoggedInKick *= 20;
        else timeBeforeNotLoggedInKick /= 3;
        maximumTotalAccounts = config.getInt("max-total-accounts");
        isOperatorCommandRestricted = config.getBoolean("restrict-op-command");
        operatorCommandPassword = config.getString("op-command-password");
        messagePrefix = translateColors(config.getString("prefix"));
        invalidNicknamesStart = config.getStringList("disallow-join-of").toArray(new String[0]);
        //spawn = Spawn.fromString(config.getString("spawn-location"));
        if (config.getBoolean("ping-before-join")) JavaHandler.initializeServerPingManager();
        if (config.getBoolean("hide-failed-join-attempts")) JavaHandler.addConsoleFilter();
    }*/

/*    private static ItemStack getDefaultCodingBook() {
        ItemStack a = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta b = (BookMeta) a.getItemMeta();
        b.setTitle("Coding Book");
        b.setAuthor("JavaCodeCompiler");
        b.addPage("public static void run() {\n}");
        b.setLore(Arrays.asList(translateArrayColors(isPluginLanguageEnglish ? "&7To execute the code, you" : "&7Aby wykonać kod, należy",
                isPluginLanguageEnglish ? "&7must first sign the book." : "&7najpierw podpisać książkę.")));
        a.setItemMeta(b);
        a.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 10);
        return a;
    }*/

    public static long getTimeUnitMultiplier(String unit) {
        if (unit.isEmpty()) return 1000L;
        switch (unit) {
            case "m":
            case "min":
            case "minut":
            case "minute":
            case "minutes":
                return 60000L;
            case "h":
            case "hour":
            case "hours":
            case "godzin":
                return 3600000L;
            case "d":
            case "dni":
            case "day":
            case "days":
                return 86400000L;
            case "w":
            case "week":
            case "weeks":
            case "tygodni":
            case "tydzień":
            case "tydzien":
                return 604800000L;
            case "month":
            case "months":
            case "miesięcy":
            case "miesiecy":
            case "miesiąc":
            case "miesiac":
                return 2592000000L;
            case "y":
            case "rok":
            case "year":
            case "years":
            case "lat":
                return 31536000000L;
/*            case "decade":
            case "decades":
            case "dekad":
            case "dekada":
                return 315360000000L;
            case "age":
            case "ages":
            case "wiek":
            case "wieków":
                return 3153600000000L;
            case "millennium":
            case "millenniums":
                return 31536000000000L;
            case "er":
            case "erę":
            case "era":
            case "eras":
                return 67802400000000L;*/
            default:
                return 1000L;
        }
    }

    private AlixUtils() {
    }
}