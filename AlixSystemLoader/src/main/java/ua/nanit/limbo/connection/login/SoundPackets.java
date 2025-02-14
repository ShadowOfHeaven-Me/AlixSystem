package ua.nanit.limbo.connection.login;

import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.sound.PacketPlayOutSound;

public final class SoundPackets {

    private static final Vector3i POS = new Vector3i(0, 64, 0);

    public static final PacketSnapshot
            VILLAGER_NO = create(Sounds.ENTITY_VILLAGER_NO),
            PLAYER_LEVELUP = create(Sounds.ENTITY_PLAYER_LEVELUP),
            ITEM_BREAK = create(Sounds.ENTITY_ITEM_BREAK),
            NOTE_BLOCK_SNARE = create(Sounds.BLOCK_NOTE_BLOCK_SNARE),
            NOTE_BLOCK_HARP = create(Sounds.BLOCK_NOTE_BLOCK_HARP);

    private static PacketSnapshot create(Sound sound) {
        return new PacketPlayOutSound(new WrapperPlayServerSoundEffect(sound, SoundCategory.MASTER, POS, 100f, 1f, 0L)).toSnapshot();
    }
}