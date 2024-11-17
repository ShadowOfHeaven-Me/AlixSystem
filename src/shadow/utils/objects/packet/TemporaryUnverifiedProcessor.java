package shadow.utils.objects.packet;

import alix.common.utils.collections.queue.network.AlixNetworkDeque;
import alix.common.utils.other.annotation.ScheduledForFix;
import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisguisedChat;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import shadow.utils.objects.packet.map.BlockedPacketsQueue;

import static shadow.utils.objects.packet.types.unverified.PacketBlocker.filterAllEntityPackets;

public final class TemporaryUnverifiedProcessor implements PacketProcessor {

    public final AlixNetworkDeque<Component> blockedChatMessages;
    public final BlockedPacketsQueue packetMap;
    public boolean chunkSent;

    //ByteBufs in BlockedPacketsQueue might not get released if the disconnect appears just in-between play phase switch and sync join
    @ScheduledForFix
    public TemporaryUnverifiedProcessor() {
        this.blockedChatMessages = new AlixNetworkDeque<>(10);
        this.packetMap = new BlockedPacketsQueue();
    }

    @Override
    public void onPacketReceive(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case KEEP_ALIVE:
            case PLUGIN_MESSAGE:
            case PONG:
                return;
        }
        event.setCancelled(true);
    }

    @Override
    public void onPacketSend(PacketPlaySendEvent event) {
        switch (event.getPacketType()) {
            case CHUNK_DATA:
            case CHUNK_BATCH_BEGIN:
            case CHUNK_BIOMES:
                this.chunkSent = true;
                return;
            case CHAT_MESSAGE://From the now deprecated ProtocolAccess.convertPlayerChatToSystemPacket
                WrapperPlayServerChatMessage playerChat = new WrapperPlayServerChatMessage(event);
                this.blockedChatMessages.offerLast(playerChat.getMessage().getChatContent());
                event.setCancelled(true);
                break;
            //case PLAYER_CHAT_HEADER:
            case DISGUISED_CHAT:
                this.blockedChatMessages.offerLast(new WrapperPlayServerDisguisedChat(event).getMessage());
                event.setCancelled(true);
                break;
            case SYSTEM_CHAT_MESSAGE:
                event.setCancelled(true);
                WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
                if (wrapper.isOverlay()) return;//action bar

                Component component = wrapper.getMessage();
                if (component instanceof TranslatableComponent && ((TranslatableComponent) component).key().equals("multiplayer.message_not_delivered")) {
                    return;
                }
                this.blockedChatMessages.offerLast(component);
                break;
            case CHANGE_GAME_STATE:
                if (new WrapperPlayServerChangeGameState(event).getReason() != WrapperPlayServerChangeGameState.Reason.START_LOADING_CHUNKS)
                    event.setCancelled(true);
                break;
            case PLAYER_ABILITIES:
            case DEATH_COMBAT_EVENT://fix for death
            case UPDATE_HEALTH://fix for death and damage
            case SET_EXPERIENCE:
            case EXPLOSION:
            case SPAWN_PLAYER:
            case UPDATE_ATTRIBUTES:
                //case TIME_UPDATE:
            case TITLE:
            case SET_TITLE_SUBTITLE:
            case SET_TITLE_TEXT:
            case SET_TITLE_TIMES:
            case EFFECT:
                //case SERVER_DATA:
            case WINDOW_ITEMS:
            case SET_SLOT:
            case DECLARE_COMMANDS:
                event.setCancelled(true);
                return;
            case BUNDLE:
            case SOUND_EFFECT:
            case ENTITY_SOUND_EFFECT:
            case NAMED_SOUND_EFFECT:
            case SET_PASSENGERS:
            case SPAWN_LIVING_ENTITY:
            case SPAWN_ENTITY:
            case ENTITY_RELATIVE_MOVE_AND_ROTATION:
            case ENTITY_RELATIVE_MOVE:
            case ENTITY_METADATA:
            case ENTITY_EQUIPMENT:
            case ENTITY_STATUS:
            case ENTITY_VELOCITY:
            case ENTITY_ANIMATION:
            case ENTITY_MOVEMENT:
            case ENTITY_ROTATION:
            case ENTITY_TELEPORT:
            case ENTITY_HEAD_LOOK:
            case REMOVE_ENTITY_EFFECT:
            case UPDATE_ENTITY_NBT:
            case ENTITY_EFFECT:
            case DESTROY_ENTITIES:
                if (filterAllEntityPackets) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case PLAYER_INFO:
            case PLAYER_INFO_REMOVE:
            case PLAYER_INFO_UPDATE:
            case UPDATE_SCORE:
            case RESET_SCORE:
            case SCOREBOARD_OBJECTIVE:
            case DISPLAY_SCOREBOARD:
                this.packetMap.addDynamic(event);
                event.setCancelled(true);
                break;
        }
    }
}