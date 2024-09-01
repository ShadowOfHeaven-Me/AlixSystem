package shadow.utils.objects;

import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.annotation.RemotelyInvoked;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;
import shadow.Main;

public final class AlixConsoleFilterHolder implements Filter {

    public static final AlixConsoleFilterHolder INSTANCE = new AlixConsoleFilterHolder();
    public static final boolean
            alixJoinLog = Main.config.getBoolean("custom-join-log"),
            hideFailedJoinAttempts = Main.config.getBoolean("hide-failed-join-attempts"),
            filterFastLoginDebug = Main.config.getBoolean("hide-failed-join-attempts");
    private final ConsoleFilter delegate;

    private AlixConsoleFilterHolder() {
        this.delegate = new ConsoleFilter();
    }

    @RemotelyInvoked
    public void makeObsolete() {
        this.delegate.obsolete = true;
    }

    public void startFilteringStdErr() {
        this.delegate.filterStdErr = true;
    }

    public void stopFilteringStdErr() {
        this.delegate.filterStdErr = false;
    }
/*    public void removeInstance() {
        this.delegate = null;
    }*/

    private static final class ConsoleFilter {

        private final char[]
                regex = "logged in with entity id".toCharArray();//,
        //regex2 = "lost connection".toCharArray();
        private boolean obsolete, filterStdErr;

        private Result filter(LogEvent e) {
            if (obsolete) return Result.NEUTRAL;
            if (filterStdErr && e.getLevel() == Level.ERROR && e.getMessage().getFormattedMessage().startsWith("[STDERR]")) {
                //Main.logInfo("OUTPUT: '" + e.getMessage().getFormattedMessage() + "'");
                return Result.DENY;
            }
            //if (e.getLevel() == Level.WARN) Main.logInfo("WARNNNNNN: " + e.getThrownProxy() + " " + e.getSource() + " " + e.getMessage().getFormattedMessage() + " " + e.getContextData());
            String message = e.getMessage().getFormattedMessage();
            if (message.isEmpty() || e.getLevel() != Level.INFO) return Result.NEUTRAL;

            if (filterFastLoginDebug && message.startsWith("[FastLogin]")) return Result.DENY;

            //Make sure not to use any
            if (hideFailedJoinAttempts && (AlixCommonUtils.startsWith(message, "UUID of player",
                    "com.mojang.authlib.GameProfile", "Disconnecting", "handleDisconnection()"))) //|| this.isLostCon0(msgChars = message.toCharArray())))
                return Result.DENY;
            String[] lostCon0 = message.split(" ", 3);
            if (lostCon0.length >= 2) {
                //Main.logError("LOST CON " + Arrays.toString(lostCon0));
                if (lostCon0[1].startsWith("lost connection")) return Result.DENY;
                if (lostCon0.length == 3 && lostCon0[2].startsWith("lost connection"))
                    return Result.DENY;
            }

            if (alixJoinLog && Thread.currentThread() == Main.mainServerThread) {
                char[] msg = message.toCharArray();
                //AlixScheduler.async(() -> Main.logError("mmmmm '" + new String(msg) + "' - " + Main.mainServerThread.getName()));
                for (int i = 0; i < msg.length; i++)
                    if (msg[i] == ' ') return isRegexPresent(msg, regex, i) ? Result.DENY : Result.NEUTRAL;
            }
            return Result.NEUTRAL;
        }

        //private static final boolean waitFor2ndSpace = ServerEnvironment.isPaper();//for some reason Paper just added a space there

        //Checks for the connection lost messages
        //_ShadowOfHeaven_ (/IP) lost connection: We're analysing your connection. You may now join the server.

        //[18:30:53] [Server thread/INFO]: /177.244.29.74:54974 lost connection: Internal Exception: io.netty.handler.codec.DecoderException: java.lang.IndexOutOfBoundsException: readerIndex(18) + length(8) exceeds writerIndex(19): PooledUnsafeDirectByteBuf(ridx: 18, widx: 19, cap: 256)
        /*private boolean isLostCon0(char[] chars) {
            boolean wait = chars[0] != '/' && waitFor2ndSpace;
            for (int i = 0; i < chars.length; i++)
                if (chars[i] == ' ') {
                    if (wait) {
                        wait = false;
                        continue;
                    }
                    return isRegexPresent(chars, regex2, i);
                }
            return false;
        }*/

        //"i" here is the last index in the char iteration, right before
        //the suspected regex. For example: "I like Pie" with the regex "Pie"
        //would be the index space index, here     ^
        //right before regex's start
        private boolean isRegexPresent(char[] msg, char[] regex, int i) {
            if (msg.length - i < regex.length)
                return false;//the message left is shorter than the regex - abort
            i++;//adding 1 to the message index to get the possible start of the regex, since the current index is the character before the regex
            for (int j = 0; j < regex.length; j++)
                if (msg[i + j] != regex[j])
                    return false;//at least one of the characters was different that the regex - abort
            return true;//all of the characters were the same as the regex
        }

        private ConsoleFilter() {
        }
    }

    @Override
    public Result getOnMismatch() {
        return Result.NEUTRAL;
    }

    @Override
    public Result getOnMatch() {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object... objects) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object o, Throwable throwable) {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable t) {
        //Main.logWarning("SEXXXXXXXX: " + t.getClass().getSimpleName() + " " + t.getMessage());
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent e) {
        return this.delegate.filter(e);
    }

    @Override
    public State getState() {
        return State.STARTED;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public boolean isStopped() {
        return false;
    }
}