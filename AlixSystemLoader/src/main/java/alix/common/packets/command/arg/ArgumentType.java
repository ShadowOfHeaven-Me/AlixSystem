package alix.common.packets.command.arg;

import com.github.retrooper.packetevents.protocol.chat.Parsers;

public enum ArgumentType {

    NBT_COMPOUND_TAG(Parsers.NBT_COMPOUND_TAG),
    NBT_TAG(Parsers.NBT_TAG),
    NBT_PATH(Parsers.NBT_PATH),
    OBJECTIVE(Parsers.OBJECTIVE),
    OBJECTIVE_CRITERIA(Parsers.OBJECTIVE_CRITERIA),
    OPERATION(Parsers.OPERATION),
    PARTICLE(Parsers.PARTICLE),
    ANGLE(Parsers.ANGLE),
    ROTATION(Parsers.ROTATION),
    SCOREBOARD_SLOT(Parsers.SCOREBOARD_SLOT),
    SCORE_HOLDER(Parsers.SCORE_HOLDER),
    SWIZZLE(Parsers.SWIZZLE),
    TEAM(Parsers.TEAM),
    ITEM_SLOT(Parsers.ITEM_SLOT),
    ITEM_SLOTS(Parsers.ITEM_SLOTS),
    RESOURCE_LOCATION(Parsers.RESOURCE_LOCATION),
    MOB_EFFECT(Parsers.MOB_EFFECT),
    FUNCTION(Parsers.FUNCTION),
    ENTITY_ANCHOR(Parsers.ENTITY_ANCHOR),
    INT_RANGE(Parsers.INT_RANGE),
    FLOAT_RANGE(Parsers.FLOAT_RANGE),
    ITEM_ENCHANTMENT(Parsers.ITEM_ENCHANTMENT),
    ENTITY_SUMMON(Parsers.ENTITY_SUMMON),
    DIMENSION(Parsers.DIMENSION),
    GAMEMODE(Parsers.GAMEMODE),
    TIME(Parsers.TIME),
    RESOURCE_OR_TAG(Parsers.RESOURCE_OR_TAG),
    RESOURCE_OR_TAG_KEY(Parsers.RESOURCE_OR_TAG_KEY),
    RESOURCE(Parsers.RESOURCE),
    RESOURCE_KEY(Parsers.RESOURCE_KEY),
    TEMPLATE_MIRROR(Parsers.TEMPLATE_MIRROR),
    TEMPLATE_ROTATION(Parsers.TEMPLATE_ROTATION),
    HEIGHTMAP(Parsers.HEIGHTMAP),
    LOOT_TABLE(Parsers.LOOT_TABLE),
    LOOT_PREDICATE(Parsers.LOOT_PREDICATE),
    LOOT_MODIFIER(Parsers.LOOT_MODIFIER),
    UUID(Parsers.UUID);

    <T extends TypedArgument> ArgumentType(Parsers.Parser parser) {

    }
}