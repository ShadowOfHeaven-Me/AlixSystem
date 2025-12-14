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

package ua.nanit.limbo.protocol;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import ua.nanit.limbo.LimboConstants;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.protocol.packets.configuration.PacketKnownPacks;
import ua.nanit.limbo.protocol.packets.configuration.PacketOutFinishConfiguration;
import ua.nanit.limbo.protocol.packets.configuration.PacketRegistryData;
import ua.nanit.limbo.protocol.packets.configuration.PacketUpdateTags;
import ua.nanit.limbo.protocol.packets.login.PacketLoginSuccess;
import ua.nanit.limbo.protocol.packets.login.PacketOutSetCompression;
import ua.nanit.limbo.protocol.packets.play.*;
import ua.nanit.limbo.protocol.packets.play.chunk.PacketEmptyChunkData;
import ua.nanit.limbo.protocol.packets.play.config.PacketPlayOutReconfigure;
import ua.nanit.limbo.protocol.packets.play.payload.PacketPlayOutPluginMessage;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.data.Title;
import ua.nanit.limbo.server.data.TitlePacketSnapshot;
import ua.nanit.limbo.util.NbtMessageUtil;
import ua.nanit.limbo.util.map.VersionMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ua.nanit.limbo.protocol.PacketSnapshot.snapshots;

public final class PacketSnapshots {

    public static final PacketSnapshot SET_COMPRESSION = PacketSnapshot.of(new PacketOutSetCompression());
    public static final PacketSnapshot RECONFIGURE = PacketSnapshot.of(new PacketPlayOutReconfigure());
    public static final int PLAYER_ENTITY_ID = 0;
    public static final UUID PLAYER_UUID = new UUID(0, 0);
    public static final PacketSnapshot PACKET_LOGIN_SUCCESS;
    public static final PacketSnapshot PACKET_JOIN_GAME;
    public static final PacketSnapshot PACKET_SPAWN_POSITION;
    public static final PacketSnapshot PACKET_CONFIG_PLUGIN_MESSAGE;
    public static final PacketSnapshot PACKET_PLAY_PLUGIN_MESSAGE;
    public static final PacketSnapshot PLAYER_ABILITIES_FALL;
    public static final PacketSnapshot PLAYER_ABILITIES_FLY;
    public static final PacketSnapshot PACKET_PLAYER_INFO;
    //public static final PacketSnapshot PACKET_DECLARE_COMMANDS;
    public static final PacketSnapshot PACKET_JOIN_MESSAGE;
    public static final PacketSnapshot PACKET_BOSS_BAR;
    public static final PacketSnapshot PACKET_HEADER_AND_FOOTER;

    //public static final PacketSnapshot PACKET_PLAYER_POS_AND_LOOK_LEGACY;
    public static final PacketSnapshot PACKET_PLAYER_POS_AND_LOOK_LEGACY;

    public static final int TELEPORT_ID = 1;
    public static final int TELEPORT_VALID_ID = 2;

    public static final int TELEPORT_Y = 400;
    public static final int TELEPORT_VALID_Y = 64;

    //LEAVE THIS BE TO LET CaptchaBlock.BELL work properly!
    public static final double VALID_XZ = NanoLimbo.centerSpawn ? 0.5 : 0.875;

    // For 1.19 we need to spawn player outside the world to avoid stuck in terrain loading
    public static final PacketSnapshot PACKET_PLAYER_POS_AND_LOOK;
    public static final PacketSnapshot PACKET_PLAYER_POS_AND_LOOK_VALID;

    public static final TitlePacketSnapshot LOGIN_TITLE, REGISTER_TITLE, EMPTY_TITLE;

    public static final PacketSnapshot PACKET_REGISTRY_DATA;
    //public static final List<PacketSnapshot> PACKETS_REGISTRY_DATA;

    public static final PacketSnapshot PACKET_KNOWN_PACKS;
    /*public static final PacketSnapshot PACKET_UPDATE_TAGS_1_20_5;
    public static final PacketSnapshot PACKET_UPDATE_TAGS_1_21;
    public static final PacketSnapshot PACKET_UPDATE_TAGS_1_21_2;
    public static final PacketSnapshot PACKET_UPDATE_TAGS_1_21_4;
    public static final PacketSnapshot PACKET_UPDATE_TAGS_1_21_5;
    public static final PacketSnapshot PACKET_UPDATE_TAGS_1_21_6;
    public static final PacketSnapshot PACKET_UPDATE_TAGS_1_21_7;
    public static final PacketSnapshot PACKET_UPDATE_TAGS_1_21_9;
    public static final List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_20_5;
    public static final List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_21;
    public static final List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_21_2;
    public static final List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_21_4;
    public static final List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_21_5;
    public static final List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_21_6;
    public static final List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_21_7;
    public static final List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_21_9;*/

    public static final VersionMap<PacketSnapshot> UPDATE_TAGS = new VersionMap<>();
    public static final VersionMap<List<PacketSnapshot>> REGISTRY_DATA = new VersionMap<>();

    public static final PacketSnapshot PACKET_FINISH_CONFIGURATION;

    public static final PacketSnapshot MIDDLE_CHUNK = PacketSnapshot.of(new PacketEmptyChunkData());
    //public static final PacketSnapshot UNLOAD_CHUNK = PacketSnapshot.of(new PacketUnloadChunk());
    //public static final List<PacketSnapshot> PACKETS_EMPTY_CHUNKS = new ArrayList<>();
    public static final PacketSnapshot PACKET_START_WAITING_CHUNKS;

    private PacketSnapshots() {
    }

    public static void releaseAll() {
        snapshots.forEach(PacketSnapshot::release);
        snapshots.clear();
    }

    public static void init() {
    }

    static {
        var server = NanoLimbo.LIMBO;
        var registry = server.getDimensionRegistry();
        /*final String username = server.getConfig().getPingData().getVersion();
        final UUID uuid = UuidUtil.getOfflineModeUuid(username);

        PacketLoginSuccess loginSuccess = new PacketLoginSuccess();
        loginSuccess.setUsername(username);
        loginSuccess.setUUID(uuid);*/

        PacketJoinGame joinGame = new PacketJoinGame();
        String worldName = "minecraft:" + server.getConfig().getDimensionType().toLowerCase();
        joinGame.setEntityId(PLAYER_ENTITY_ID);
        joinGame.setViewDistance(0);
        joinGame.setHashedSeed(0);
        joinGame.setPreviousGameMode(-1);
        joinGame.setEnableRespawnScreen(false);
        joinGame.setFlat(false);
        joinGame.setHardcore(false);
        joinGame.setGameMode(server.getConfig().getGameMode());
        joinGame.setMaxPlayers(server.getConfig().getMaxPlayers());
        joinGame.setReducedDebugInfo(true);
        joinGame.setEnableRespawnScreen(false);
        joinGame.setDebug(false);
        joinGame.setWorldName(worldName);
        joinGame.setWorldNames(worldName);
        joinGame.setLimitedCrafting(false);
        joinGame.setSecureProfile(true);
        joinGame.setDimensionRegistry(registry);
        PACKET_JOIN_GAME = PacketSnapshot.of(joinGame);

        PacketPlayerAbilities abilities = new PacketPlayerAbilities();
        abilities.wrapper().setFlySpeed(NanoLimbo.allowFreeMovement ? 0.05F : 0.0F);
        abilities.wrapper().setFlying(NanoLimbo.allowFreeMovement);//todo
        abilities.wrapper().setFOVModifier(0.1F);

        PLAYER_ABILITIES_FALL = PacketSnapshot.of(abilities);
        abilities.wrapper().setFlying(true);
        PLAYER_ABILITIES_FLY = PacketSnapshot.of(abilities);

        PacketSpawnPosition packetSpawnPosition = new PacketSpawnPosition(0, 400, 0);

        //PacketDeclareCommands declareCommands = new PacketDeclareCommands();
        //declareCommands.setCommands(Collections.emptyList());

        PacketPlayerInfo info = new PacketPlayerInfo();
        info.setUsername(server.getConfig().getPlayerListUsername());
        info.setGameMode(server.getConfig().getGameMode());
        info.setUuid(UUID.randomUUID());

        //float validYaw = 0;
        PACKET_LOGIN_SUCCESS = PacketSnapshot.of(new PacketLoginSuccess().setUsername("Sex").setUUID(PLAYER_UUID));
        //PACKET_PLAYER_POS_AND_LOOK_LEGACY = PacketSnapshot.of(new PacketPlayerPositionAndLook(VALID_XZ, TELEPORT_Y, VALID_XZ, 0.1f, 0, TELEPORT_ID));
        PACKET_PLAYER_POS_AND_LOOK_LEGACY = PacketSnapshot.of(new PacketPlayerPositionAndLook(VALID_XZ, TELEPORT_VALID_Y, VALID_XZ, 0.1f, 0, TELEPORT_VALID_ID));
        PACKET_PLAYER_POS_AND_LOOK = PacketSnapshot.of(new PacketPlayerPositionAndLook(VALID_XZ, TELEPORT_Y, VALID_XZ, 0.1f, 0, TELEPORT_ID));
        PACKET_PLAYER_POS_AND_LOOK_VALID = PacketSnapshot.of(new PacketPlayerPositionAndLook(VALID_XZ, TELEPORT_VALID_Y, VALID_XZ, 0f, 0, TELEPORT_VALID_ID));
        PACKET_SPAWN_POSITION = PacketSnapshot.of(packetSpawnPosition);
        PACKET_PLAYER_INFO = PacketSnapshot.of(info);

        //PACKET_DECLARE_COMMANDS = PacketSnapshot.of(declareCommands);

        if (server.getConfig().isUseHeaderAndFooter()) {
            PacketPlayerListHeader header = new PacketPlayerListHeader();
            header.setHeader(NbtMessageUtil.create(server.getConfig().getPlayerListHeader()));
            header.setFooter(NbtMessageUtil.create(server.getConfig().getPlayerListFooter()));
            PACKET_HEADER_AND_FOOTER = PacketSnapshot.of(header);
        } else {
            PACKET_HEADER_AND_FOOTER = null;
        }

        if (server.getConfig().isUseBrandName()) {
            PacketConfigPluginMessage pluginMessage = new PacketConfigPluginMessage();
            pluginMessage.setChannel(LimboConstants.BRAND_CHANNEL);
            pluginMessage.setMessage(server.getConfig().getBrandName());
            PACKET_CONFIG_PLUGIN_MESSAGE = PacketSnapshot.of(pluginMessage);

            PacketPlayOutPluginMessage pluginMessagePlay = new PacketPlayOutPluginMessage();
            pluginMessagePlay.setChannel(LimboConstants.BRAND_CHANNEL);
            pluginMessagePlay.setMessage(server.getConfig().getBrandName());
            PACKET_PLAY_PLUGIN_MESSAGE = PacketSnapshot.of(pluginMessagePlay);
        } else {
            PACKET_CONFIG_PLUGIN_MESSAGE = null;
            PACKET_PLAY_PLUGIN_MESSAGE = null;
        }

        if (server.getConfig().isUseJoinMessage()) {
            PacketPlayOutMessage joinMessage = new PacketPlayOutMessage();
            joinMessage.setMessage(server.getConfig().getJoinMessage());
            PACKET_JOIN_MESSAGE = PacketSnapshot.of(joinMessage);
        } else {
            PACKET_JOIN_MESSAGE = null;
        }

        //noinspection IfStatementWithIdenticalBranches
        if (server.getConfig().isUseBossBar()) {
            /*PacketBossBar bossBar = new PacketBossBar();
            bossBar.setBossBar(server.getConfig().getBossBar());
            bossBar.setUuid(UUID.randomUUID());*/
            PACKET_BOSS_BAR = null;
        } else {
            PACKET_BOSS_BAR = null;
        }

        LOGIN_TITLE = new TitlePacketSnapshot(server.getConfig().getLoginTitle());
        REGISTER_TITLE = new TitlePacketSnapshot(server.getConfig().getRegisterTitle());
        EMPTY_TITLE = new TitlePacketSnapshot(new Title().setTitle("").setSubtitle("").setStay(999999999));

        PacketKnownPacks packetKnownPacks = new PacketKnownPacks();
        PACKET_KNOWN_PACKS = PacketSnapshot.of(packetKnownPacks);

        var PACKET_UPDATE_TAGS_1_20_5 = PacketSnapshot.of(new PacketUpdateTags(registry.getTags_1_20_5()));
        var PACKET_UPDATE_TAGS_1_21 = PacketSnapshot.of(new PacketUpdateTags(registry.getTags_1_21()));
        var PACKET_UPDATE_TAGS_1_21_2 = PacketSnapshot.of(new PacketUpdateTags(registry.getTags_1_21_2()));
        var PACKET_UPDATE_TAGS_1_21_4 = PacketSnapshot.of(new PacketUpdateTags(registry.getTags_1_21_4()));
        var PACKET_UPDATE_TAGS_1_21_5 = PacketSnapshot.of(new PacketUpdateTags(registry.getTags_1_21_5()));
        var PACKET_UPDATE_TAGS_1_21_6 = PacketSnapshot.of(new PacketUpdateTags(registry.getTags_1_21_6()));
        var PACKET_UPDATE_TAGS_1_21_7 = PacketSnapshot.of(new PacketUpdateTags(registry.getTags_1_21_7()));
        var PACKET_UPDATE_TAGS_1_21_9 = PacketSnapshot.of(new PacketUpdateTags(registry.getTags_1_21_9()));
        var PACKET_UPDATE_TAGS_1_21_11 = PacketSnapshot.of(new PacketUpdateTags(registry.getTags_1_21_11()));

        PacketRegistryData packetRegistryData = new PacketRegistryData();
        packetRegistryData.setDimensionRegistry(registry);

        PACKET_REGISTRY_DATA = PacketSnapshot.of(packetRegistryData);

        /*Dimension dimension1_21 = registry.getDimension_1_21();
        List<PacketSnapshot> packetRegistries = new ArrayList<>();
        CompoundBinaryTag dimensionTag = dimension1_21.getData();
        for (String registryType : dimensionTag.keySet()) {
            CompoundBinaryTag compoundRegistryType = dimensionTag.getCompound(registryType);

            PacketRegistryData registryData = new PacketRegistryData();
            registryData.setDimensionRegistry(registry);

            ListBinaryTag values = compoundRegistryType.getList("value");
            registryData.setMetadataWriter((message, version) -> {
                message.writeString(registryType);

                message.writeVarInt(values.size());
                for (BinaryTag entry : values) {
                    CompoundBinaryTag entryTag = (CompoundBinaryTag) entry;

                    String name = entryTag.getString("name");
                    CompoundBinaryTag element = entryTag.getCompound("element");

                    message.writeString(name);
                    message.writeBoolean(true);
                    message.writeNamelessCompoundTag(element);
                }
            });

            packetRegistries.add(PacketSnapshot.of(registryData));
        }

        PACKETS_REGISTRY_DATA = packetRegistries;*/

        var  PACKETS_REGISTRY_DATA_1_20_5 = createRegistryData(server, registry.getCodec_1_20_5());
        var PACKETS_REGISTRY_DATA_1_21 = createRegistryData(server, registry.getCodec_1_21());
        var PACKETS_REGISTRY_DATA_1_21_2 = createRegistryData(server, registry.getCodec_1_21_2());
        var PACKETS_REGISTRY_DATA_1_21_4 = createRegistryData(server, registry.getCodec_1_21_4());
        var PACKETS_REGISTRY_DATA_1_21_5 = createRegistryData(server, registry.getCodec_1_21_5());
        var PACKETS_REGISTRY_DATA_1_21_6 = createRegistryData(server, registry.getCodec_1_21_6());
        var PACKETS_REGISTRY_DATA_1_21_7 = createRegistryData(server, registry.getCodec_1_21_7());
        var PACKETS_REGISTRY_DATA_1_21_9 = createRegistryData(server, registry.getCodec_1_21_9());
        var PACKETS_REGISTRY_DATA_1_21_11 = createRegistryData(server, registry.getCodec_1_21_11());

        PACKET_FINISH_CONFIGURATION = PacketSnapshot.of(new PacketOutFinishConfiguration());

        PacketGameEvent packetGameEvent = new PacketGameEvent();
        packetGameEvent.setType((byte) 13); // Waiting for chunks type
        packetGameEvent.setValue(0);
        PACKET_START_WAITING_CHUNKS = PacketSnapshot.of(packetGameEvent);


        UPDATE_TAGS.put(Version.V1_20_5, PACKET_UPDATE_TAGS_1_20_5);
        UPDATE_TAGS.put(Version.V1_21,   PACKET_UPDATE_TAGS_1_21);
        UPDATE_TAGS.put(Version.V1_21_2, PACKET_UPDATE_TAGS_1_21_2);
        UPDATE_TAGS.put(Version.V1_21_4, PACKET_UPDATE_TAGS_1_21_4);
        UPDATE_TAGS.put(Version.V1_21_5, PACKET_UPDATE_TAGS_1_21_5);
        UPDATE_TAGS.put(Version.V1_21_6, PACKET_UPDATE_TAGS_1_21_6);
        UPDATE_TAGS.put(Version.V1_21_7, PACKET_UPDATE_TAGS_1_21_7);
        UPDATE_TAGS.put(Version.V1_21_9, PACKET_UPDATE_TAGS_1_21_9);
        UPDATE_TAGS.put(Version.V1_21_11, PACKET_UPDATE_TAGS_1_21_11);

        REGISTRY_DATA.put(Version.V1_20_5, PACKETS_REGISTRY_DATA_1_20_5);
        REGISTRY_DATA.put(Version.V1_21,   PACKETS_REGISTRY_DATA_1_21);
        REGISTRY_DATA.put(Version.V1_21_2, PACKETS_REGISTRY_DATA_1_21_2);
        REGISTRY_DATA.put(Version.V1_21_4, PACKETS_REGISTRY_DATA_1_21_4);
        REGISTRY_DATA.put(Version.V1_21_5, PACKETS_REGISTRY_DATA_1_21_5);
        REGISTRY_DATA.put(Version.V1_21_6, PACKETS_REGISTRY_DATA_1_21_6);
        REGISTRY_DATA.put(Version.V1_21_7, PACKETS_REGISTRY_DATA_1_21_7);
        REGISTRY_DATA.put(Version.V1_21_9, PACKETS_REGISTRY_DATA_1_21_9);
        REGISTRY_DATA.put(Version.V1_21_11, PACKETS_REGISTRY_DATA_1_21_11);

        /*int chunkXOffset = (int) 0 >> 4; // Default x position is 0
        int chunkZOffset = (int) 0 >> 4; // Default z position is 0
        int chunkEdgeSize = 1;//1; // TODO Make configurable?*/

        /*List<PacketSnapshot> emptyChunks = new ArrayList<>();
        // Make multiple chunks for edges
        for (int chunkX = chunkXOffset - chunkEdgeSize; chunkX <= chunkXOffset + chunkEdgeSize; ++chunkX) {
            for (int chunkZ = chunkZOffset - chunkEdgeSize; chunkZ <= chunkZOffset + chunkEdgeSize; ++chunkZ) {
                if (chunkX == 0 && chunkZ == 0) {
                    PACKETS_EMPTY_CHUNKS.add(MIDDLE_CHUNK);
                    continue;
                }
                PacketEmptyChunkData packetEmptyChunk = new PacketEmptyChunkData();
                packetEmptyChunk.setX(chunkX);
                packetEmptyChunk.setZ(chunkZ);

                emptyChunks.add(PacketSnapshot.of(packetEmptyChunk));
            }
        }
        PACKETS_EMPTY_CHUNKS.addAll(emptyChunks);*/
        //Log.error("CHUNKS LEN: " + PACKETS_EMPTY_CHUNKS);
    }


    private static List<PacketSnapshot> createRegistryData(LimboServer server, CompoundBinaryTag dimensionTag) {
        List<PacketSnapshot> packetRegistries = new ArrayList<>();
        for (String registryType : dimensionTag.keySet()) {
            CompoundBinaryTag compoundRegistryType = dimensionTag.getCompound(registryType);

            PacketRegistryData registryData = new PacketRegistryData();
            registryData.setDimensionRegistry(server.getDimensionRegistry());

            ListBinaryTag values = compoundRegistryType.getList("value");
            registryData.setMetadataWriter((message, version) -> {
                message.writeString(registryType);

                message.writeVarInt(values.size());
                for (BinaryTag entry : values) {
                    CompoundBinaryTag entryTag = (CompoundBinaryTag) entry;

                    String name = entryTag.getString("name");
                    CompoundBinaryTag element = entryTag.getCompound("element", null);

                    message.writeString(name);
                    if (element != null) {
                        message.writeBoolean(true);
                        message.writeNamelessCompoundTag(element);
                    } else {
                        message.writeBoolean(false);
                    }
                }
            });

            packetRegistries.add(PacketSnapshot.of(registryData));
        }

        return packetRegistries;
    }
}
