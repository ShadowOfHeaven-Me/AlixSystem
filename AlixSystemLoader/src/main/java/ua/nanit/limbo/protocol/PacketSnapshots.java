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

import alix.common.utils.collections.queue.AlixQueue;
import alix.common.utils.collections.queue.ConcurrentAlixDeque;
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
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.data.Title;
import ua.nanit.limbo.util.NbtMessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PacketSnapshots {

    static final AlixQueue<PacketSnapshot> snapshots = new ConcurrentAlixDeque<>();
    public static final PacketSnapshot SET_COMPRESSION = PacketSnapshot.of(new PacketOutSetCompression());
    public static final int PLAYER_ENTITY_ID = 0;
    public static PacketSnapshot PACKET_LOGIN_SUCCESS;
    public static PacketSnapshot PACKET_JOIN_GAME;
    public static PacketSnapshot PACKET_SPAWN_POSITION;
    public static PacketSnapshot PACKET_CONFIG_PLUGIN_MESSAGE;
    public static PacketSnapshot PACKET_PLAY_PLUGIN_MESSAGE;
    public static PacketSnapshot PLAYER_ABILITIES_FALL;
    public static PacketSnapshot PLAYER_ABILITIES_FLY;
    public static PacketSnapshot PACKET_PLAYER_INFO;
    //public static PacketSnapshot PACKET_DECLARE_COMMANDS;
    public static PacketSnapshot PACKET_JOIN_MESSAGE;
    public static PacketSnapshot PACKET_BOSS_BAR;
    public static PacketSnapshot PACKET_HEADER_AND_FOOTER;

    public static PacketSnapshot PACKET_PLAYER_POS_AND_LOOK_LEGACY;
    public static PacketSnapshot PACKET_PLAYER_POS_AND_LOOK_LEGACY_VALID;

    public static final int TELEPORT_ID = 1;
    public static final int TELEPORT_VALID_ID = 2;

    public static final int TELEPORT_Y = 400;
    public static final int TELEPORT_VALID_Y = 64;

    //LEAVE THIS BE TO LET CaptchaBlock.BELL work properly!
    public static final double VALID_XZ = NanoLimbo.centerSpawn ? 0.5 : 0.875;

    // For 1.19 we need to spawn player outside the world to avoid stuck in terrain loading
    public static PacketSnapshot PACKET_PLAYER_POS_AND_LOOK;
    public static PacketSnapshot PACKET_PLAYER_POS_AND_LOOK_VALID;

    public static PacketSnapshot PACKET_TITLE_TITLE;
    public static PacketSnapshot PACKET_TITLE_SUBTITLE;
    public static PacketSnapshot PACKET_TITLE_TIMES;

    public static PacketSnapshot PACKET_TITLE_LEGACY_TITLE;
    public static PacketSnapshot PACKET_TITLE_LEGACY_SUBTITLE;
    public static PacketSnapshot PACKET_TITLE_LEGACY_TIMES;

    public static PacketSnapshot PACKET_REGISTRY_DATA;

    public static PacketSnapshot PACKET_KNOWN_PACKS;
    public static PacketSnapshot PACKET_UPDATE_TAGS;
    public static List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_20_5;
    public static List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_21;
    public static List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_21_2;
    public static List<PacketSnapshot> PACKETS_REGISTRY_DATA_1_21_4;

    public static PacketSnapshot PACKET_FINISH_CONFIGURATION;

    public static final PacketSnapshot MIDDLE_CHUNK = PacketSnapshot.of(new PacketEmptyChunkData());
    //public static final PacketSnapshot UNLOAD_CHUNK = PacketSnapshot.of(new PacketUnloadChunk());
    public static final List<PacketSnapshot> PACKETS_EMPTY_CHUNKS = new ArrayList<>();
    public static PacketSnapshot PACKET_START_WAITING_CHUNKS;

    private PacketSnapshots() {
    }

    public static void releaseAll() {
        snapshots.forEach(PacketSnapshot::release);
        snapshots.clear();
    }

    public static void initPackets(LimboServer server) {
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
        joinGame.setSecureProfile(true);
        joinGame.setDimensionRegistry(server.getDimensionRegistry());
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

        PACKET_LOGIN_SUCCESS = PacketSnapshot.of(new PacketLoginSuccess().setUsername("Sex").setUUID(new UUID(0, 0)));
        PACKET_PLAYER_POS_AND_LOOK_LEGACY = PacketSnapshot.of(new PacketPlayerPositionAndLook(VALID_XZ, TELEPORT_Y, VALID_XZ, 0.1f, 0, TELEPORT_ID));
        PACKET_PLAYER_POS_AND_LOOK_LEGACY_VALID = PacketSnapshot.of(new PacketPlayerPositionAndLook(VALID_XZ, TELEPORT_VALID_Y, VALID_XZ, 0, 0, TELEPORT_VALID_ID));
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
        }

        if (server.getConfig().isUseBrandName()) {
            PacketConfigPluginMessage pluginMessage = new PacketConfigPluginMessage();
            pluginMessage.setChannel(LimboConstants.BRAND_CHANNEL);
            pluginMessage.setMessage(server.getConfig().getBrandName());
            PACKET_CONFIG_PLUGIN_MESSAGE = PacketSnapshot.of(pluginMessage);


            PacketPlayPluginMessage pluginMessagePlay = new PacketPlayPluginMessage();
            pluginMessagePlay.setChannel(LimboConstants.BRAND_CHANNEL);
            pluginMessagePlay.setMessage(server.getConfig().getBrandName());
            PACKET_PLAY_PLUGIN_MESSAGE = PacketSnapshot.of(pluginMessagePlay);
        }

        if (server.getConfig().isUseJoinMessage()) {
            PacketChatMessage joinMessage = new PacketChatMessage();
            joinMessage.setMessage(NbtMessageUtil.create(server.getConfig().getJoinMessage()));
            joinMessage.setPosition(PacketChatMessage.PositionLegacy.SYSTEM_MESSAGE);
            PACKET_JOIN_MESSAGE = PacketSnapshot.of(joinMessage);
        }

        if (server.getConfig().isUseBossBar()) {
            /*PacketBossBar bossBar = new PacketBossBar();
            bossBar.setBossBar(server.getConfig().getBossBar());
            bossBar.setUuid(UUID.randomUUID());*/
            PACKET_BOSS_BAR = null;
        }

        if (server.getConfig().isUseTitle()) {
            Title title = server.getConfig().getTitle();

            PacketTitleSetTitle packetTitle = new PacketTitleSetTitle();
            PacketTitleSetSubTitle packetSubtitle = new PacketTitleSetSubTitle();
            PacketTitleTimes packetTimes = new PacketTitleTimes();

            PacketTitleLegacy legacyTitle = new PacketTitleLegacy();
            PacketTitleLegacy legacySubtitle = new PacketTitleLegacy();
            PacketTitleLegacy legacyTimes = new PacketTitleLegacy();

            packetTitle.setTitle(title.getTitle());
            packetSubtitle.setSubtitle(title.getSubtitle());
            packetTimes.setFadeIn(title.getFadeIn());
            packetTimes.setStay(title.getStay());
            packetTimes.setFadeOut(title.getFadeOut());

            legacyTitle.setTitle(title);
            legacyTitle.setAction(PacketTitleLegacy.Action.SET_TITLE);

            legacySubtitle.setTitle(title);
            legacySubtitle.setAction(PacketTitleLegacy.Action.SET_SUBTITLE);

            legacyTimes.setTitle(title);
            legacyTimes.setAction(PacketTitleLegacy.Action.SET_TIMES_AND_DISPLAY);

            PACKET_TITLE_TITLE = PacketSnapshot.of(packetTitle);
            PACKET_TITLE_SUBTITLE = PacketSnapshot.of(packetSubtitle);
            PACKET_TITLE_TIMES = PacketSnapshot.of(packetTimes);

            PACKET_TITLE_LEGACY_TITLE = PacketSnapshot.of(legacyTitle);
            PACKET_TITLE_LEGACY_SUBTITLE = PacketSnapshot.of(legacySubtitle);
            PACKET_TITLE_LEGACY_TIMES = PacketSnapshot.of(legacyTimes);
        }

        PacketKnownPacks packetKnownPacks = new PacketKnownPacks();
        PACKET_KNOWN_PACKS = PacketSnapshot.of(packetKnownPacks);

        PacketUpdateTags packetUpdateTags = new PacketUpdateTags();
        packetUpdateTags.setTags(server.getDimensionRegistry().getTags_1_20_5());

        PACKET_UPDATE_TAGS = PacketSnapshot.of(packetUpdateTags);

        PacketRegistryData packetRegistryData = new PacketRegistryData();
        packetRegistryData.setDimensionRegistry(server.getDimensionRegistry());

        PACKET_REGISTRY_DATA = PacketSnapshot.of(packetRegistryData);

        PACKETS_REGISTRY_DATA_1_20_5 = createRegistryData(server, server.getDimensionRegistry().getCodec_1_20_5());
        PACKETS_REGISTRY_DATA_1_21 = createRegistryData(server, server.getDimensionRegistry().getCodec_1_21());
        PACKETS_REGISTRY_DATA_1_21_2 = createRegistryData(server, server.getDimensionRegistry().getCodec_1_21_2());
        PACKETS_REGISTRY_DATA_1_21_4 = createRegistryData(server, server.getDimensionRegistry().getCodec_1_21_4());

        PACKET_FINISH_CONFIGURATION = PacketSnapshot.of(new PacketOutFinishConfiguration());

        PacketGameEvent packetGameEvent = new PacketGameEvent();
        packetGameEvent.setType((byte) 13); // Waiting for chunks type
        packetGameEvent.setValue(0);
        PACKET_START_WAITING_CHUNKS = PacketSnapshot.of(packetGameEvent);

        int chunkXOffset = (int) 0 >> 4; // Default x position is 0
        int chunkZOffset = (int) 0 >> 4; // Default z position is 0
        int chunkEdgeSize = 1;//1; // TODO Make configurable?

        List<PacketSnapshot> emptyChunks = new ArrayList<>();
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
        PACKETS_EMPTY_CHUNKS.addAll(emptyChunks);
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
