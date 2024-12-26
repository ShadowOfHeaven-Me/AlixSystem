package ua.nanit.limbo.connection.captcha.blocks;

import alix.common.utils.AlixCommonUtils;
import alix.libs.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import alix.libs.com.github.retrooper.packetevents.protocol.world.states.enums.Attachment;
import alix.libs.com.github.retrooper.packetevents.protocol.world.states.enums.Tilt;
import alix.libs.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import alix.libs.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ua.nanit.limbo.protocol.registry.Version;

import java.util.function.Consumer;
import java.util.function.Function;

public enum CaptchaBlock {
    //https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/fallback/protocol/block/BlockType.java#L32

    //1.7+
    ENCHANTMENT_TABLE(StateTypes.ENCHANTING_TABLE, ver -> 0.75),
    TRAPDOOR(StateTypes.OAK_TRAPDOOR, ver -> 0.1875),
    END_PORTAL_FRAME(StateTypes.END_PORTAL_FRAME, ver -> 0.8125),
    DAYLIGHT_SENSOR(StateTypes.DAYLIGHT_DETECTOR, ver -> 0.375),
    COBBLESTONE_WALL(StateTypes.COBBLESTONE_WALL, ver -> 1.5),//HIGHEST
    STONE_SLAB(StateTypes.STONE_SLAB, ver -> 0.5),
    WHITE_CARPET(StateTypes.WHITE_CARPET, ver -> ver.less(Version.V1_8) ? 0 : 0.0625),
    FARMLAND(StateTypes.FARMLAND, ver -> 0.9375),
    //1.13+
    SEA_PICKLES(StateTypes.SEA_PICKLE, ver -> 0.4375, state -> state.setPickles(4)), //0.4375 when 4 pickles, otherwise 0.375
    //1.14+
    //0.8125 on the lower level (0.9375 on the higher hitbox level, 0.375 on lowest) when attached by single_wall or double_wall, 1.0 otherwise
    BELL(StateTypes.BELL, ver -> 0.8125, state -> state.setAttachment(Attachment.DOUBLE_WALL)),
    LECTERN(StateTypes.LECTERN, ver -> 0.875),
    //1.17+
    DRIPLEAF_TILT_UNSTABLE(StateTypes.BIG_DRIPLEAF, ver -> 0.9375, state -> state.setTilt(Tilt.UNSTABLE)),
    DRIPLEAF_TILT_PARTIAL(StateTypes.BIG_DRIPLEAF, ver -> 0.8125, state -> state.setTilt(Tilt.PARTIAL));//,
    //SMALL_AMETHYST(StateTypes.SMALL_AMETHYST_BUD, ver -> 0.1875);//too small of a hitbox (probably)

    public static final int FULL_LEN = values().length;
    public static final int LEN_TILL_v1_13 = SEA_PICKLES.ordinal();
    public static final int LEN_TILL_v1_14 = BELL.ordinal();
    public static final int LEN_TILL_v1_17 = DRIPLEAF_TILT_UNSTABLE.ordinal();
    //public static final double GREATEST_HEIGHT = 1.5;
    private final StateType blockType;
    private final Function<Version, Double> heightFunction;
    private final Consumer<WrappedBlockState> transformer;

    CaptchaBlock(StateType blockType, Function<Version, Double> heightFunction, Consumer<WrappedBlockState> transformer) {
        this.blockType = blockType;
        this.heightFunction = heightFunction;
        this.transformer = transformer;
    }

    CaptchaBlock(StateType blockType, Function<Version, Double> heightFunction) {
        this(blockType, heightFunction, AlixCommonUtils.EMPTY_CONSUMER);
    }

    public Consumer<WrappedBlockState> getTransformer() {
        return transformer;
    }

    public StateType getBlockType() {
        return blockType;
    }

    public double getHeight(Version version) {
        return this.heightFunction.apply(version);
    }
}