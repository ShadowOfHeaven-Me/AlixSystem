package ua.nanit.limbo.connection.login.packets;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetExperience;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.xp.PacketPlayOutExperience;

import static alix.common.utils.config.ConfigParams.maxLoginTime;

public final class ExperiencePackets {

    public static final int EXPERIENCE_UPDATES_PER_SECOND = 2;
    public static final long UPDATE_PERIOD_MILLI = 1000 / EXPERIENCE_UPDATES_PER_SECOND;
    public static final int PACKET_COUNT = maxLoginTime * EXPERIENCE_UPDATES_PER_SECOND;
    public static final PacketSnapshot[] PACKETS = new PacketSnapshot[PACKET_COUNT];

    static {
        for (int i = 0; i < PACKET_COUNT; i++) {

            float xpBar = ((float) i) / PACKET_COUNT;
            int lvl = i / EXPERIENCE_UPDATES_PER_SECOND;
            int totalExp = lvl <= 16 ? lvl * lvl + 6 * lvl : lvl <= 30 ? 5 * lvl - 38 : 9 * lvl - 158; //from the minecraft wiki

            PACKETS[i] = snapshot(new WrapperPlayServerSetExperience(xpBar, lvl, totalExp));
        }
    }

    private static PacketSnapshot snapshot(WrapperPlayServerSetExperience wrapper) {
        return new PacketPlayOutExperience(wrapper).toSnapshot();
    }

    public static void init() {
    }
}