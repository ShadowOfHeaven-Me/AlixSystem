package ua.nanit.limbo.connection.login.packets;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetExperience;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.xp.PacketPlayOutExperience;

import static alix.common.utils.config.ConfigParams.emailVerificationTime;
import static alix.common.utils.config.ConfigParams.maxLoginTime;

public final class ExperiencePackets {

    public static final int EXPERIENCE_UPDATES_PER_SECOND = 2;
    public static final long UPDATE_PERIOD_MILLI = 1000 / EXPERIENCE_UPDATES_PER_SECOND;
    public static final int PACKET_COUNT = maxLoginTime * EXPERIENCE_UPDATES_PER_SECOND;
    public static final PacketSnapshot[] PACKETS = build(PACKET_COUNT);

    //A separate precomputed set for LimboCountdown's "waiting on an email verification code" mode (see
    //LoginState#handleRegisterCommandWithEmail() and ConfigParams#emailVerificationTime) - built the same
    //way as PACKETS above, just sized to the independently-configurable 'email-verification-time' duration
    //instead of 'max-login-time'.
    public static final int EMAIL_VERIFICATION_PACKET_COUNT = emailVerificationTime * EXPERIENCE_UPDATES_PER_SECOND;
    public static final PacketSnapshot[] EMAIL_VERIFICATION_PACKETS = build(EMAIL_VERIFICATION_PACKET_COUNT);

    private static PacketSnapshot[] build(int count) {
        PacketSnapshot[] packets = new PacketSnapshot[count];
        for (int i = 0; i < count; i++) {

            float xpBar = ((float) i) / count;
            int lvl = i / EXPERIENCE_UPDATES_PER_SECOND;
            int totalExp = lvl <= 16 ? lvl * lvl + 6 * lvl : lvl <= 30 ? 5 * lvl - 38 : 9 * lvl - 158; //from the minecraft wiki

            packets[i] = snapshot(new WrapperPlayServerSetExperience(xpBar, lvl, totalExp));
        }
        return packets;
    }

    private static PacketSnapshot snapshot(WrapperPlayServerSetExperience wrapper) {
        return new PacketPlayOutExperience(wrapper).toSnapshot();
    }

    public static void init() {
    }
}