/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo.protocol.registry;

import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import ua.nanit.limbo.protocol.HandleMask;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.packets.configuration.*;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.*;
import ua.nanit.limbo.protocol.packets.login.disconnect.PacketLoginDisconnect;
import ua.nanit.limbo.protocol.packets.play.*;
import ua.nanit.limbo.protocol.packets.play.animation.PacketPlayInAnimation;
import ua.nanit.limbo.protocol.packets.play.animation.PacketPlayOutAnimation;
import ua.nanit.limbo.protocol.packets.play.batch.PacketPlayInChunkBatchAck;
import ua.nanit.limbo.protocol.packets.play.batch.PacketPlayOutChunkBatchEnd;
import ua.nanit.limbo.protocol.packets.play.batch.PacketPlayOutChunkBatchStart;
import ua.nanit.limbo.protocol.packets.play.blocks.PacketPlayOutBlockSectionUpdate;
import ua.nanit.limbo.protocol.packets.play.blocks.PacketPlayOutBlockUpdate;
import ua.nanit.limbo.protocol.packets.play.chat.PacketPlayInChat;
import ua.nanit.limbo.protocol.packets.play.chunk.PacketEmptyChunkData;
import ua.nanit.limbo.protocol.packets.play.chunk.PacketUnloadChunk;
import ua.nanit.limbo.protocol.packets.play.config.PacketPlayInReconfigureAck;
import ua.nanit.limbo.protocol.packets.play.config.PacketPlayOutReconfigure;
import ua.nanit.limbo.protocol.packets.play.cookie.PacketPlayInCookieResponse;
import ua.nanit.limbo.protocol.packets.play.cookie.PacketPlayOutCookieRequest;
import ua.nanit.limbo.protocol.packets.play.cookie.PacketPlayOutCookieStore;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import ua.nanit.limbo.protocol.packets.play.entity.PacketPlayOutEntityMetadata;
import ua.nanit.limbo.protocol.packets.play.entity.PacketPlayOutSpawnEntity;
import ua.nanit.limbo.protocol.packets.play.explosion.PacketPlayOutExplosion;
import ua.nanit.limbo.protocol.packets.play.held.PacketPlayInHeldSlot;
import ua.nanit.limbo.protocol.packets.play.held.PacketPlayOutHeldSlot;
import ua.nanit.limbo.protocol.packets.play.inventory.*;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketInConfigKeepAlive;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketInPlayKeepAlive;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketOutConfigKeepAlive;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketPlayOutKeepAlive;
import ua.nanit.limbo.protocol.packets.play.map.PacketPlayOutMap;
import ua.nanit.limbo.protocol.packets.play.move.PacketPlayInFlying;
import ua.nanit.limbo.protocol.packets.play.move.PacketPlayInPosition;
import ua.nanit.limbo.protocol.packets.play.move.PacketPlayInPositionAndRotation;
import ua.nanit.limbo.protocol.packets.play.move.PacketPlayInRotation;
import ua.nanit.limbo.protocol.packets.play.payload.PacketPlayInPluginMessage;
import ua.nanit.limbo.protocol.packets.play.payload.PacketPlayOutPluginMessage;
import ua.nanit.limbo.protocol.packets.play.ping.PacketPlayInPong;
import ua.nanit.limbo.protocol.packets.play.ping.PacketPlayOutPing;
import ua.nanit.limbo.protocol.packets.play.rename.PacketPlayInItemRename;
import ua.nanit.limbo.protocol.packets.play.sound.PacketPlayOutSound;
import ua.nanit.limbo.protocol.packets.play.teleport.PacketPlayInTeleportConfirm;
import ua.nanit.limbo.protocol.packets.play.tick.PacketPlayInTickEnd;
import ua.nanit.limbo.protocol.packets.play.transaction.PacketPlayInTransaction;
import ua.nanit.limbo.protocol.packets.play.transaction.PacketPlayOutTransaction;
import ua.nanit.limbo.protocol.packets.play.transfer.PacketPlayOutTransfer;
import ua.nanit.limbo.protocol.packets.play.xp.PacketPlayOutExperience;
import ua.nanit.limbo.protocol.packets.status.PacketInStatusPing;
import ua.nanit.limbo.protocol.packets.status.PacketOutStatusPing;
import ua.nanit.limbo.protocol.packets.status.PacketStatusRequest;
import ua.nanit.limbo.util.map.VersionMap;

import java.util.*;
import java.util.function.Supplier;

import static ua.nanit.limbo.protocol.registry.Version.*;

public enum State {

    HANDSHAKING {
        {
            serverBound.register(PacketHandshake::new,
                    map(0x00, Version.getMin(), Version.getMax())
            );
        }
    },
    STATUS {
        {
            serverBound.register(PacketStatusRequest::new,
                    map(0x00, Version.getMin(), Version.getMax())
            );
            serverBound.register(PacketInStatusPing::new,
                    map(0x01, Version.getMin(), Version.getMax())
            );
            /*clientBound.register(PacketStatusResponse::new,
                    map(0x00, Version.getMin(), Version.getMax())
            );*/
            clientBound.register(PacketOutStatusPing::new,
                    map(0x01, Version.getMin(), Version.getMax())
            );
        }
    },
    LOGIN {
        {
            serverBound.register(PacketLoginStart::new,
                    map(0x00, Version.getMin(), Version.getMax())
            );
            serverBound.register(PacketLoginPluginResponse::new,
                    map(0x02, Version.getMin(), Version.getMax())
            );
            serverBound.register(
                    PacketLoginAcknowledged::new,
                    map(0x03, V1_20_2, Version.getMax())
            );

            clientBound.register(PacketLoginDisconnect::new,
                    map(0x00, Version.getMin(), Version.getMax())
            );
            clientBound.register(PacketLoginSuccess::new,
                    map(0x02, Version.getMin(), Version.getMax())
            );
            clientBound.register(PacketLoginPluginRequest::new,
                    map(0x04, Version.getMin(), Version.getMax())
            );
            clientBound.registerRetrooper(PacketOutSetCompression::new, PacketType.Login.Server.SET_COMPRESSION);
        }
    },
    CONFIGURATION {
        {
            clientBound.registerRetrooper(
                    PacketConfigPluginMessage::new,
                    PacketType.Configuration.Server.PLUGIN_MESSAGE
            );
            clientBound.register(
                    PacketConfigDisconnect::new,
                    map(0x01, V1_20_2, V1_20_3),
                    map(0x02, V1_20_5, V1_21_4)
            );
            clientBound.register(
                    PacketOutFinishConfiguration::new,
                    map(0x02, V1_20_2, V1_20_3),
                    map(0x03, V1_20_5, V1_21_4)
            );
            clientBound.register(
                    PacketOutConfigKeepAlive::new,
                    map(0x03, V1_20_2, V1_20_3),
                    map(0x04, V1_20_5, V1_21_4)
            );
            clientBound.register(
                    PacketKnownPacks::new,
                    map(0x0E, V1_20_5, V1_21_4)
            );
            clientBound.register(
                    PacketUpdateTags::new,
                    map(0x0D, V1_20_5, V1_21_4)
            );
            clientBound.register(
                    PacketRegistryData::new,
                    map(0x05, V1_20_2, V1_20_3),
                    map(0x07, V1_20_5, V1_21_4)
            );

            /*serverBound.register(
                    PacketPluginMessage::new,
                    map(0x01, V1_20_2, V1_20_3),
                    map(0x02, V1_20_2, V1_21_4)
            );*/
            serverBound.register(
                    PacketInFinishConfiguration::new,
                    map(0x02, V1_20_2, V1_20_3),
                    map(0x03, V1_20_5, V1_21_4)
            );
            serverBound.register(
                    PacketInConfigKeepAlive::new,
                    map(0x03, V1_20_2, V1_20_3),
                    map(0x04, V1_20_5, V1_21_4)
            );
        }
    },
    PLAY {
        {
            serverBound.registerRetrooper(PacketInCommand::new, PacketType.Play.Client.CHAT_COMMAND);
            serverBound.registerRetrooper(PacketInCommandUnsigned::new, PacketType.Play.Client.CHAT_COMMAND_UNSIGNED);
            serverBound.registerRetrooper(PacketPlayInChat::new, PacketType.Play.Client.CHAT_MESSAGE);
            serverBound.registerRetrooper(PacketPlayInFlying::new, PacketType.Play.Client.PLAYER_FLYING);
            serverBound.registerRetrooper(PacketPlayInPosition::new, PacketType.Play.Client.PLAYER_POSITION);
            serverBound.registerRetrooper(PacketPlayInPositionAndRotation::new, PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION);
            serverBound.registerRetrooper(PacketPlayInRotation::new, PacketType.Play.Client.PLAYER_ROTATION);

            serverBound.registerRetrooper(PacketPlayInPong::new, PacketType.Play.Client.PONG);
            serverBound.registerRetrooper(PacketPlayInTickEnd::new, PacketType.Play.Client.CLIENT_TICK_END);
            serverBound.registerRetrooper(PacketPlayInPluginMessage::new, PacketType.Play.Client.PLUGIN_MESSAGE);
            serverBound.registerRetrooper(PacketPlayInTeleportConfirm::new, PacketType.Play.Client.TELEPORT_CONFIRM);
            serverBound.registerRetrooper(PacketPlayInTransaction::new, PacketType.Play.Client.WINDOW_CONFIRMATION);
            serverBound.registerRetrooper(PacketPlayInHeldSlot::new, PacketType.Play.Client.HELD_ITEM_CHANGE);
            serverBound.registerRetrooper(PacketPlayInAnimation::new, PacketType.Play.Client.ANIMATION);
            serverBound.registerRetrooper(PacketPlayInChunkBatchAck::new, PacketType.Play.Client.CHUNK_BATCH_ACK);
            serverBound.registerRetrooper(PacketPlayInCookieResponse::new, PacketType.Play.Client.COOKIE_RESPONSE);
            serverBound.registerRetrooper(PacketPlayInInventoryClose::new, PacketType.Play.Client.CLOSE_WINDOW);
            serverBound.registerRetrooper(PacketPlayInClickSlot::new, PacketType.Play.Client.CLICK_WINDOW);
            serverBound.registerRetrooper(PacketPlayInItemRename::new, PacketType.Play.Client.NAME_ITEM);
            serverBound.registerRetrooper(PacketPlayInReconfigureAck::new, PacketType.Play.Client.CONFIGURATION_ACK);

            serverBound.register(PacketInPlayKeepAlive::new,
                    map(0x00, V1_7_6, V1_8),
                    map(0x0B, V1_9, V1_11_1),
                    map(0x0C, V1_12, V1_12),
                    map(0x0B, V1_12_1, V1_12_2),
                    map(0x0E, V1_13, V1_13_2),
                    map(0x0F, V1_14, V1_15_2),
                    map(0x10, V1_16, V1_16_4),
                    map(0x0F, V1_17, V1_18_2),
                    map(0x11, V1_19, V1_19),
                    map(0x12, V1_19_1, V1_19_1),
                    map(0x11, V1_19_3, V1_19_3),
                    map(0x12, V1_19_4, V1_20),
                    map(0x14, V1_20_2, V1_20_2),
                    map(0x15, V1_20_3, V1_20_3),
                    map(0x18, V1_20_5, V1_21),
                    map(0x1A, V1_21_2, V1_21_4)
            );
            clientBound.registerRetrooper(PacketPlayOutTransaction::new, PacketType.Play.Server.WINDOW_CONFIRMATION);
            clientBound.registerRetrooper(PacketPlayOutPluginMessage::new, PacketType.Play.Server.PLUGIN_MESSAGE);
            clientBound.registerRetrooper(PacketPlayOutPing::new, PacketType.Play.Server.PING);
            clientBound.registerRetrooper(PacketPlayOutExplosion::new, PacketType.Play.Server.EXPLOSION);
            clientBound.registerRetrooper(PacketOutCommands::new, PacketType.Play.Server.DECLARE_COMMANDS);
            clientBound.registerRetrooper(PacketPlayOutKeepAlive::new, PacketType.Play.Server.KEEP_ALIVE);
            clientBound.registerRetrooper(PacketPlayOutDisconnect::new, PacketType.Play.Server.DISCONNECT);
            clientBound.registerRetrooper(PacketPlayOutReconfigure::new, PacketType.Play.Server.CONFIGURATION_START);
            clientBound.registerRetrooper(PacketPlayOutBlockSectionUpdate::new, PacketType.Play.Server.MULTI_BLOCK_CHANGE);
            clientBound.registerRetrooper(PacketPlayOutBlockUpdate::new, PacketType.Play.Server.BLOCK_CHANGE);
            clientBound.registerRetrooper(PacketPlayOutEntityMetadata::new, PacketType.Play.Server.ENTITY_METADATA);
            clientBound.registerRetrooper(PacketPlayOutSpawnEntity::new, PacketType.Play.Server.SPAWN_ENTITY);
            clientBound.registerRetrooper(PacketPlayOutInventoryOpen::new, PacketType.Play.Server.OPEN_WINDOW);
            clientBound.registerRetrooper(PacketPlayOutInventoryClose::new, PacketType.Play.Server.CLOSE_WINDOW);
            clientBound.registerRetrooper(PacketPlayOutInventoryItems::new, PacketType.Play.Server.WINDOW_ITEMS);
            clientBound.registerRetrooper(PacketPlayOutMap::new, PacketType.Play.Server.MAP_DATA);
            clientBound.registerRetrooper(PacketPlayOutSetSlot::new, PacketType.Play.Server.SET_SLOT);
            clientBound.registerRetrooper(PacketPlayOutSound::new, PacketType.Play.Server.SOUND_EFFECT);
            clientBound.registerRetrooper(PacketPlayOutHeldSlot::new, PacketType.Play.Server.HELD_ITEM_CHANGE);
            clientBound.registerRetrooper(PacketPlayOutAnimation::new, PacketType.Play.Server.ENTITY_ANIMATION);
            clientBound.registerRetrooper(PacketPlayOutChunkBatchStart::new, PacketType.Play.Server.CHUNK_BATCH_BEGIN);
            clientBound.registerRetrooper(PacketPlayOutChunkBatchEnd::new, PacketType.Play.Server.CHUNK_BATCH_END);
            clientBound.registerRetrooper(PacketPlayOutTransfer::new, PacketType.Play.Server.TRANSFER);
            clientBound.registerRetrooper(PacketPlayOutExperience::new, PacketType.Play.Server.SET_EXPERIENCE);
            clientBound.registerRetrooper(PacketPlayOutCookieStore::new, PacketType.Play.Server.STORE_COOKIE);

            clientBound.registerRetrooper(PacketPlayOutCookieRequest::new, PacketType.Play.Server.COOKIE_REQUEST);
            /*clientBound.register(PacketDeclareCommands::new,
                    map(0x11, V1_13, V1_14_4),
                    map(0x12, V1_15, V1_15_2),
                    map(0x11, V1_16, V1_16_1),
                    map(0x10, V1_16_2, V1_16_4),
                    map(0x12, V1_17, V1_18_2),
                    map(0x0F, V1_19, V1_19_1),
                    map(0x0E, V1_19_3, V1_19_3),
                    map(0x10, V1_19_4, V1_20),
                    map(0x11, V1_20_2, V1_21_2)
            );*/
            clientBound.register(PacketJoinGame::new,
                    map(0x01, V1_7_6, V1_8),
                    map(0x23, V1_9, V1_12_2),
                    map(0x25, V1_13, V1_14_4),
                    map(0x26, V1_15, V1_15_2),
                    map(0x25, V1_16, V1_16_1),
                    map(0x24, V1_16_2, V1_16_4),
                    map(0x26, V1_17, V1_18_2),
                    map(0x23, V1_19, V1_19),
                    map(0x25, V1_19_1, V1_19_1),
                    map(0x24, V1_19_3, V1_19_3),
                    map(0x28, V1_19_4, V1_20),
                    map(0x29, V1_20_2, V1_20_3),
                    map(0x2B, V1_20_5, V1_21),
                    map(0x2C, V1_21_2, V1_21_4)
            );

            clientBound.register(PacketPlayerAbilities::new,
                    map(0x39, V1_7_6, V1_8),
                    map(0x2B, V1_9, V1_12),
                    map(0x2C, V1_12_1, V1_12_2),
                    map(0x2E, V1_13, V1_13_2),
                    map(0x31, V1_14, V1_14_4),
                    map(0x32, V1_15, V1_15_2),
                    map(0x31, V1_16, V1_16_1),
                    map(0x30, V1_16_2, V1_16_4),
                    map(0x32, V1_17, V1_18_2),
                    map(0x2F, V1_19, V1_19),
                    map(0x31, V1_19_1, V1_19_1),
                    map(0x30, V1_19_3, V1_19_3),
                    map(0x34, V1_19_4, V1_20),
                    map(0x36, V1_20_2, V1_20_3),
                    map(0x38, V1_20_5, V1_21),
                    map(0x3A, V1_21_2, V1_21_4)
            );
            clientBound.register(PacketPlayerPositionAndLook::new,
                    map(0x08, V1_7_6, V1_8),
                    map(0x2E, V1_9, V1_12),
                    map(0x2F, V1_12_1, V1_12_2),
                    map(0x32, V1_13, V1_13_2),
                    map(0x35, V1_14, V1_14_4),
                    map(0x36, V1_15, V1_15_2),
                    map(0x35, V1_16, V1_16_1),
                    map(0x34, V1_16_2, V1_16_4),
                    map(0x38, V1_17, V1_18_2),
                    map(0x36, V1_19, V1_19),
                    map(0x39, V1_19_1, V1_19_1),
                    map(0x38, V1_19_3, V1_19_3),
                    map(0x3C, V1_19_4, V1_20),
                    map(0x3E, V1_20_2, V1_20_3),
                    map(0x40, V1_20_5, V1_21),
                    map(0x42, V1_21_2, V1_21_4)
            );
            clientBound.register(PacketPlayOutMessage::new,
                    map(0x02, V1_7_6, V1_8),
                    map(0x0F, V1_9, V1_12_2),
                    map(0x0E, V1_13, V1_14_4),
                    map(0x0F, V1_15, V1_15_2),
                    map(0x0E, V1_16, V1_16_4),
                    map(0x0F, V1_17, V1_18_2),
                    map(0x5F, V1_19, V1_19),
                    map(0x62, V1_19_1, V1_19_1),
                    map(0x60, V1_19_3, V1_19_3),
                    map(0x64, V1_19_4, V1_20),
                    map(0x67, V1_20_2, V1_20_2),
                    map(0x69, V1_20_3, V1_20_3),
                    map(0x6C, V1_20_5, V1_21),
                    map(0x73, V1_21_2, V1_21_4)
            );
            /*clientBound.register(PacketBossBar::new,
                    map(0x0C, V1_9, V1_14_4),
                    map(0x0D, V1_15, V1_15_2),
                    map(0x0C, V1_16, V1_16_4),
                    map(0x0D, V1_17, V1_18_2),
                    map(0x0A, V1_19, V1_19_3),
                    map(0x0B, V1_19_4, V1_20),
                    map(0x0A, V1_20_2, V1_21_4)
            );*/
            clientBound.register(PacketPlayerInfo::new,
                    map(0x38, V1_7_6, V1_8),
                    map(0x2D, V1_9, V1_12),
                    map(0x2E, V1_12_1, V1_12_2),
                    map(0x30, V1_13, V1_13_2),
                    map(0x33, V1_14, V1_14_4),
                    map(0x34, V1_15, V1_15_2),
                    map(0x33, V1_16, V1_16_1),
                    map(0x32, V1_16_2, V1_16_4),
                    map(0x36, V1_17, V1_18_2),
                    map(0x34, V1_19, V1_19),
                    map(0x37, V1_19_1, V1_19_1),
                    map(0x36, V1_19_3, V1_19_3),
                    map(0x3A, V1_19_4, V1_20),
                    map(0x3C, V1_20_2, V1_20_3),
                    map(0x3E, V1_20_5, V1_21),
                    map(0x40, V1_21_2, V1_21_4)
            );
            clientBound.register(PacketTitleLegacy::new,
                    map(0x45, V1_8, V1_11_1),
                    map(0x47, V1_12, V1_12),
                    map(0x48, V1_12_1, V1_12_2),
                    map(0x4B, V1_13, V1_13_2),
                    map(0x4F, V1_14, V1_14_4),
                    map(0x50, V1_15, V1_15_2),
                    map(0x4F, V1_16, V1_16_4)
            );
            clientBound.register(PacketTitleSetTitle::new,
                    map(0x59, V1_17, V1_17_1),
                    map(0x5A, V1_18, V1_19),
                    map(0x5D, V1_19_1, V1_19_1),
                    map(0x5B, V1_19_3, V1_19_3),
                    map(0x5F, V1_19_4, V1_20),
                    map(0x61, V1_20_2, V1_20_2),
                    map(0x63, V1_20_3, V1_20_3),
                    map(0x65, V1_20_5, V1_21),
                    map(0x6C, V1_21_2, V1_21_4)
            );
            clientBound.register(PacketTitleSetSubTitle::new,
                    map(0x57, V1_17, V1_17_1),
                    map(0x58, V1_18, V1_19),
                    map(0x5B, V1_19_1, V1_19_1),
                    map(0x59, V1_19_3, V1_19_3),
                    map(0x5D, V1_19_4, V1_20),
                    map(0x5F, V1_20_2, V1_20_2),
                    map(0x61, V1_20_3, V1_20_3),
                    map(0x63, V1_20_5, V1_21),
                    map(0x6A, V1_21_2, V1_21_4)
            );
            clientBound.register(PacketTitleTimes::new,
                    map(0x5A, V1_17, V1_17_1),
                    map(0x5B, V1_18, V1_19),
                    map(0x5E, V1_19_1, V1_19_1),
                    map(0x5C, V1_19_3, V1_19_3),
                    map(0x60, V1_19_4, V1_20),
                    map(0x62, V1_20_2, V1_20_2),
                    map(0x64, V1_20_3, V1_20_3),
                    map(0x66, V1_20_5, V1_21),
                    map(0x6D, V1_21_2, V1_21_4)
            );
            clientBound.register(PacketPlayerListHeader::new,
                    map(0x47, V1_8, V1_8),
                    map(0x48, V1_9, V1_9_2),
                    map(0x47, V1_9_4, V1_11_1),
                    map(0x49, V1_12, V1_12),
                    map(0x4A, V1_12_1, V1_12_2),
                    map(0x4E, V1_13, V1_13_2),
                    map(0x53, V1_14, V1_14_4),
                    map(0x54, V1_15, V1_15_2),
                    map(0x53, V1_16, V1_16_4),
                    map(0x5E, V1_17, V1_17_1),
                    map(0x5F, V1_18, V1_18_2),
                    map(0x60, V1_19, V1_19),
                    map(0x63, V1_19_1, V1_19_1),
                    map(0x61, V1_19_3, V1_19_3),
                    map(0x65, V1_19_4, V1_20),
                    map(0x68, V1_20_2, V1_20_2),
                    map(0x6A, V1_20_3, V1_20_3),
                    map(0x6D, V1_20_5, V1_21),
                    map(0x74, V1_21_2, V1_21_4)
            );
            clientBound.register(PacketSpawnPosition::new,
                    map(0x4C, V1_19_3, V1_19_3),
                    map(0x50, V1_19_4, V1_20),
                    map(0x52, V1_20_2, V1_20_2),
                    map(0x54, V1_20_3, V1_20_3),
                    map(0x56, V1_20_5, V1_21),
                    map(0x5B, V1_21_2, V1_21_4)
            );
            clientBound.register(PacketGameEvent::new,
                    map(0x20, V1_20_3, V1_20_3),
                    map(0x22, V1_20_5, V1_21),
                    map(0x23, V1_21_2, V1_21_4)
            );
            clientBound.registerRetrooper(PacketEmptyChunkData::new, PacketType.Play.Server.CHUNK_DATA);
            clientBound.registerRetrooper(PacketUnloadChunk::new, PacketType.Play.Server.UNLOAD_CHUNK);
        }
    };

    /*private static final Map<Integer, State> STATE_BY_ID = new HashMap<>();

    static {
        for (State registry : values()) {
            STATE_BY_ID.put(registry.stateId, registry);
        }
    }*/

    public final ProtocolMappings<PacketIn> serverBound = new ProtocolMappings<>(this);
    public final ProtocolMappings<PacketOut> clientBound = new ProtocolMappings<>(this);

    State() {
    }

    public static State getState(Packet packet) {
        //Log.error("getState: " + packet + " STATE: " + ProtocolMappings.clazzToState.get(packet));
        return ProtocolMappings.clazzToState.get(packet.getClass());
    }

    //Will not fully work, but may stay for now
    public static boolean isCompressible(State state, Class<? extends Packet> clazz) {
        switch (state) {
            case HANDSHAKING:
            case STATUS:
                return false;
            case LOGIN:
                //PacketLoginDisconnect is compressible after COMPRESSION SET
                return clazz == PacketLoginSuccess.class || clazz == PacketLoginPluginRequest.class;
            case PLAY:
            case CONFIGURATION:
                return true;
            default:
                throw new AlixError("Invalid state: " + state + ", clazz " + clazz);
        }
    }

    public static State getHandshakeStateId(int stateId) {
        switch (stateId) {
            case 1://status
                return State.STATUS;
            case 2://login
            case 3://transfer (same sequence as login)
                return State.LOGIN;
            default:
                throw new AlixException("Invalid stateId");
        }
    }

    /*public static State getById(int stateId) {
        return STATE_BY_ID.get(stateId);
    }*/

    public static final class ProtocolMappings<T extends Packet> {

        private static final Map<Class<? extends Packet>, State> clazzToState = new IdentityHashMap<>();
        //No need to use ConcurrentVersionMap here
        private final VersionMap<PacketRegistry> registry = new VersionMap<>();
        private final State state;

        private ProtocolMappings(State state) {
            this.state = state;
        }

        //private final Map<Version, PacketRegistry> registry = new EnumMap<>(Version.class);

        public PacketRegistry getRegistry(Version version) {
            return registry.getOrDefault(version, registry.get(getMin()));
        }

        private static void registerAndEnsureNoDuplicate(Class<? extends Packet> clazz, State state) {
            //Log.error("CLAZZ: " + clazz + " STATE: " + state);
            if (clazzToState.put(clazz, state) != null)
                throw new AlixException("Packet clazz duplicate! - " + clazz.getSimpleName());
        }

        private static void register0(Supplier<? extends Packet> packet, State state) {
            Class<? extends Packet> clazz = packet.get().getClass();
            registerAndEnsureNoDuplicate(clazz, state);

            if (PacketIn.class.isAssignableFrom(clazz))
                HandleMask.register(clazz);
        }

        //Gotta love packetevents ;]
        private void registerRetrooper(Supplier<T> packet, PacketTypeCommon type) {
            register0(packet, this.state);
            for (Version ver : Version.values()) {
                if (!ver.isSupported()) continue;

                int packetId = type.getId(ClientVersion.getById(ver.getProtocolNumber()));
                if (packetId < 0) continue;//check if the packet exists on the specified version

                PacketRegistry reg = registry.computeIfAbsent(ver, PacketRegistry::new);
                reg.register(packetId, packet);
            }
        }

        private void register(Supplier<T> packet, Mapping... mappings) {
            register0(packet, this.state);
            for (Mapping mapping : mappings) {
                for (Version ver : getRange(mapping)) {
                    PacketRegistry reg = registry.computeIfAbsent(ver, PacketRegistry::new);
                    reg.register(mapping.packetId, packet);
                }
            }
        }

        private Collection<Version> getRange(Mapping mapping) {
            return getRange(mapping.from, mapping.to);
        }

        private Collection<Version> getRange(Version from, Version to) {
            Version curr = to;

            if (curr == from)
                return Collections.singletonList(from);

            List<Version> versions = new LinkedList<>();

            while (curr != from) {
                versions.add(curr);
                curr = curr.getPrev();
            }

            versions.add(from);

            return versions;
        }
    }

    public static final class PacketRegistry {

        private final Version version;
        private final IntObjectMap<Supplier<? extends Packet>> packetsById = new IntObjectHashMap<>();
        private final Map<Class<?>, Integer> packetIdByClass = new IdentityHashMap<>();

        public PacketRegistry(Version version) {
            this.version = version;
        }

        public Version getVersion() {
            return version;
        }

        public Packet getPacket(int packetId) {
            Supplier<? extends Packet> supplier = packetsById.get(packetId);
            return supplier == null ? null : supplier.get();
        }

        public boolean hasPacket(Class<?> packetClass) {
            return packetIdByClass.containsKey(packetClass);
        }

        public int getPacketId(Class<?> packetClass) {
            return packetIdByClass.getOrDefault(packetClass, -1);
        }

        public void register(int packetId, Supplier<? extends Packet> supplier) {
            this.packetsById.put(packetId, supplier);
            this.packetIdByClass.put(supplier.get().getClass(), packetId);
        }
    }

    private static final class Mapping {

        private final int packetId;
        private final Version from;
        private final Version to;

        private Mapping(int packetId, Version from, Version to) {
            this.from = from;
            this.to = to;
            this.packetId = packetId;
        }
    }

    /**
     * Map packet id to version range
     *
     * @param packetId Packet id
     * @param from     Minimal version (include)
     * @param to       Last version (include)
     * @return Created mapping
     */
    private static Mapping map(int packetId, Version from, Version to) {
        return new Mapping(packetId, from, to);
    }
}
