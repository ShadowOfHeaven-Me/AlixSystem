//package alix.common.utils.formatter;
//
//import shadow.utils.main.JavaUtils;
//import shadow.utils.objects.savable.data.PersistentUserData;
//
//import java.util.Date;
//
//public class UserDataFormatter {
//
//    private final String passwordFormat, ipFormat, mutedFormat;
//
//    protected UserDataFormatter(PersistentUserData data) {
//        passwordFormat = !data.getPassword().isSet() ? "Not set yet." : data.getHashedPassword();
//        ipFormat = data.getSavedIP().equals("0") ? "No information yet." : data.getSavedIP();
//        mutedFormat = data.getMutedUntil() < System.currentTimeMillis() ? "No" : "until " + JavaUtils.getFormattedDate(new Date(data.getMutedUntil()));
//    }
//
//    public final String getPasswordFormat() {
//        return passwordFormat;
//    }
//
//    public final String getIPFormat() {
//        return ipFormat;
//    }
//
//    public final String getMutedFormat() {
//        return mutedFormat;
//    }
//}