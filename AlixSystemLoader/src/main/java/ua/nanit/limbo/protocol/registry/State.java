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
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import ua.nanit.limbo.connection.ClientConnection;
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

import static ua.nanit.limbo.protocol.registry.Version.getMin;

public enum State {

    HANDSHAKING {
        {
            serverBound.registerRetrooper(PacketHandshake::new, PacketType.Handshaking.Client.HANDSHAKE);
        }
    },
    STATUS {
        {
            serverBound.registerRetrooper(() -> PacketStatusRequest.INSTANCE, PacketType.Status.Client.REQUEST);
            serverBound.registerRetrooper(PacketInStatusPing::new, PacketType.Status.Client.PING);
            clientBound.registerRetrooper(PacketOutStatusPing::new, PacketType.Status.Server.PONG);
        }
    },
    LOGIN {
        {
            serverBound.registerRetrooper(PacketLoginStart::new, PacketType.Login.Client.LOGIN_START);
            serverBound.registerRetrooper(PacketLoginPluginResponse::new, PacketType.Login.Client.LOGIN_PLUGIN_RESPONSE);
            serverBound.registerRetrooper(() -> PacketLoginAcknowledged.INSTANCE, PacketType.Login.Client.LOGIN_SUCCESS_ACK);

            clientBound.registerRetrooper(PacketLoginDisconnect::new, PacketType.Login.Server.DISCONNECT);
            clientBound.registerRetrooper(PacketLoginSuccess::new, PacketType.Login.Server.LOGIN_SUCCESS);
            clientBound.registerRetrooper(PacketLoginPluginRequest::new, PacketType.Login.Server.LOGIN_PLUGIN_REQUEST);
            clientBound.registerRetrooper(PacketOutSetCompression::new, PacketType.Login.Server.SET_COMPRESSION);
        }
    },
    CONFIGURATION {
        {
            clientBound.registerRetrooper(PacketConfigPluginMessage::new, PacketType.Configuration.Server.PLUGIN_MESSAGE);
            clientBound.registerRetrooper(PacketConfigDisconnect::new, PacketType.Configuration.Server.DISCONNECT);
            clientBound.registerRetrooper(() -> PacketOutFinishConfiguration.INSTANCE, PacketType.Configuration.Server.CONFIGURATION_END);
            clientBound.registerRetrooper(PacketOutConfigKeepAlive::new, PacketType.Configuration.Server.KEEP_ALIVE);
            clientBound.registerRetrooper(PacketKnownPacks::new, PacketType.Configuration.Server.SELECT_KNOWN_PACKS);
            clientBound.registerRetrooper(PacketUpdateTags::new, PacketType.Configuration.Server.UPDATE_TAGS);
            clientBound.registerRetrooper(PacketRegistryData::new, PacketType.Configuration.Server.REGISTRY_DATA);

            serverBound.registerRetrooper(() -> PacketInFinishConfiguration.INSTANCE, PacketType.Configuration.Client.CONFIGURATION_END_ACK);
            serverBound.registerRetrooper(PacketInConfigKeepAlive::new, PacketType.Configuration.Client.KEEP_ALIVE);
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
            serverBound.registerRetrooper(() -> PacketPlayInTickEnd.INSTANCE, PacketType.Play.Client.CLIENT_TICK_END);
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
            serverBound.registerRetrooper(PacketInPlayKeepAlive::new, PacketType.Play.Client.KEEP_ALIVE);

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
            clientBound.registerRetrooper(PacketJoinGame::new, PacketType.Play.Server.JOIN_GAME);
            clientBound.registerRetrooper(PacketPlayerAbilities::new, PacketType.Play.Server.PLAYER_ABILITIES);
            clientBound.registerRetrooper(PacketPlayerPositionAndLook::new, PacketType.Play.Server.PLAYER_POSITION_AND_LOOK);
            clientBound.registerRetrooper(PacketPlayOutMessage::new, PacketType.Play.Server.CHAT_MESSAGE);
            clientBound.registerRetrooper(PacketPlayerInfo::new, PacketType.Play.Server.PLAYER_INFO);
            clientBound.registerRetrooper(PacketTitleLegacy::new, PacketType.Play.Server.TITLE);
            clientBound.registerRetrooper(PacketTitleSetTitle::new, PacketType.Play.Server.SET_TITLE_TEXT);
            clientBound.registerRetrooper(PacketTitleSetSubTitle::new, PacketType.Play.Server.SET_TITLE_SUBTITLE);
            clientBound.registerRetrooper(PacketTitleTimes::new, PacketType.Play.Server.SET_TITLE_TIMES);
            clientBound.registerRetrooper(PacketPlayerListHeader::new, PacketType.Play.Server.PLAYER_LIST_HEADER_AND_FOOTER);
            clientBound.registerRetrooper(PacketSpawnPosition::new, PacketType.Play.Server.SPAWN_POSITION);
            clientBound.registerRetrooper(PacketGameEvent::new, PacketType.Play.Server.CHANGE_GAME_STATE);
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

    public static Class<? extends PacketWrapper<?>> getWrapperClazz(Packet packet) {
        return ProtocolMappings.clazzToWrapper.get(packet.getClass());
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
        private static final Map<Class<? extends Packet>, Class<? extends PacketWrapper<?>>> clazzToWrapper = new IdentityHashMap<>();
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
                throw new AlixError("Packet clazz duplicate! - " + clazz.getSimpleName());
        }

        private static void register0(Supplier<? extends Packet> packet, State state, PacketTypeCommon type) {
            Class<? extends Packet> clazz = packet.get().getClass();
            registerAndEnsureNoDuplicate(clazz, state);

            clazzToWrapper.put(clazz, type.getWrapperClass());
            /*if (PacketIn.class.isAssignableFrom(clazz))
                HandleMask.register(clazz);*/
        }

        //private static final Set<PacketTypeCommon> packets = new HashSet<>();

        //Gotta love packetevents ;]
        private void registerRetrooper(Supplier<T> packet, PacketTypeCommon type) {
            register0(packet, this.state, type);

            /*if (!packets.add(type))
                throw new AlixException("PacketTypeCommon duplicate! - " + type);*/

            for (Version ver : Version.values()) {
                if (!ver.isSupported()) continue;

                int packetId = type.getId(ver.getClientVersion());
                /*if(ver == V1_21_7 && this.state == CONFIGURATION) {
                    Log.error("PACKET ID=" + packetId);
                }*/
                if (packetId < 0) continue;//check if the packet exists on the specified version

                PacketRegistry reg = registry.computeIfAbsent(ver, PacketRegistry::new);
                reg.register(packetId, packet);
            }
        }

        /*private void register(Supplier<T> packet, Mapping... mappings) {
            register0(packet, this.state);
            for (Mapping mapping : mappings) {
                for (Version ver : getRange(mapping)) {
                    PacketRegistry reg = registry.computeIfAbsent(ver, PacketRegistry::new);
                    reg.register(mapping.packetId, packet);
                }
            }
        }*/

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
        private final IntObjectMap<PacketFactory> packetsById = new IntObjectHashMap<>();
        private final Map<Class<?>, Integer> packetIdByClass = new IdentityHashMap<>();

        public PacketRegistry(Version version) {
            this.version = version;
        }

        public Version getVersion() {
            return version;
        }

        public PacketFactory getFactory(int packetId) {
            return packetsById.get(packetId);
        }

        public boolean hasPacket(Class<?> packetClass) {
            return packetIdByClass.containsKey(packetClass);
        }

        public int getPacketId(Class<?> packetClass) {
            return packetIdByClass.getOrDefault(packetClass, -1);
        }

        public void register(int packetId, Supplier<? extends Packet> supplier) {
            this.packetsById.put(packetId, new PacketFactory(supplier));
            this.packetIdByClass.put(supplier.get().getClass(), packetId);
        }
    }

    public static final class PacketFactory {
        private final Supplier<? extends Packet> supplier;
        private final Packet cached;//empty, used only for isSkippable
        private final boolean skippable;

        private PacketFactory(Supplier<? extends Packet> supplier) {
            this.supplier = supplier;

            var cached = supplier.get();
            this.skippable = HandleMask.isSkippable0(cached.getClass());
            //let the instance be gc'ed if not needed
            this.cached = this.skippable ? null : cached;
        }

        public boolean isPacketSkippable(ClientConnection connection) {
            return this.skippable || this.cached.isSkippable(connection);
        }

        public Packet newPacket() {
            return this.supplier.get();
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