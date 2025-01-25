package shadow.utils.misc.effect;

import alix.common.utils.collections.map.InvisibleStorageMap;
import alix.common.utils.other.throwable.AlixException;
import shadow.utils.misc.ReflectionUtils;
import alix.common.utils.other.AlixUnsafe;
import shadow.utils.users.types.UnverifiedUser;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Map;

import static shadow.utils.misc.ReflectionUtils.nmsClazzOrNull;

final class UnsafeEffectHandler extends AbstractEffectHandler {

    private static final Unsafe UNSAFE;
    private static final long MOB_MAP_OFFSET;
    private final Object entityPlayer;
    private final InvisibleStorageMap temporaryMobEffectMap;

    UnsafeEffectHandler(UnverifiedUser user) {
        this.entityPlayer = ReflectionUtils.getHandle(user.getPlayer());

        Map potionEffectsMapReference = (Map) UNSAFE.getObject(this.entityPlayer, MOB_MAP_OFFSET);//this reference is constant, unless overridden by reflection - in which case this class' implementation won't work
        this.temporaryMobEffectMap = new InvisibleStorageMap(potionEffectsMapReference);
    }

    @Override
    public void resetEffects() {
        UNSAFE.putObject(this.entityPlayer, MOB_MAP_OFFSET, this.temporaryMobEffectMap);
    }

    @Override
    public void returnEffects() {
        UNSAFE.putObject(this.entityPlayer, MOB_MAP_OFFSET, this.temporaryMobEffectMap.getStorage());
    }

    static {
        UNSAFE = AlixUnsafe.getUnsafe();

        //Player player;
        //player.addPotionEffect();

        Field mobEffectMapField = ReflectionUtils.getFieldAccessible(ReflectionUtils.entityLivingClass, "effects", "activeEffects");

        if (mobEffectMapField == null) {
            Class<?> mobEffectClass = nmsClazzOrNull("net.minecraft.server.%s.MobEffect", "net.minecraft.world.effect.MobEffect");
            Class<?> mobEffectListClass = nmsClazzOrNull("net.minecraft.server.%s.MobEffectList", "net.minecraft.world.effect.MobEffectList");

            if (mobEffectClass != null && mobEffectListClass != null) {
                mobEffectMapField = ReflectionUtils.getFieldByTypeAndParamsOrNull(ReflectionUtils.entityLivingClass, Map.class,
                        mobEffectListClass, mobEffectClass);
            }


            if (mobEffectMapField == null) {
                Class<?> holderClass = ReflectionUtils.nms2("core.Holder");
                Class<?> mobEffectInstanceClass = ReflectionUtils.nms2("world.effect.MobEffectInstance");
                mobEffectMapField = ReflectionUtils.getFieldByTypeAndParamsOrNull(ReflectionUtils.entityLivingClass, Map.class, holderClass, mobEffectInstanceClass);
            }
        }
        /*for (Field f : ReflectionUtils.entityLivingClass.getDeclaredFields()) {
            if (Map.class.isAssignableFrom(f.getType())) {
                Type[] params = ((ParameterizedType) f.getGenericType()).getActualTypeArguments();
                //Map<MobEffectList, MobEffect>
                if (params[0] == ReflectionUtils.mobEffectListClass && params[1] == ReflectionUtils.mobEffectClass) {
                    mobEffectMapField = f;
                    break;
                }
            }
        }*/
        if (mobEffectMapField == null) throw new AlixException("NO MOB EFFECT MAP FIELD!");
        MOB_MAP_OFFSET = UNSAFE.objectFieldOffset(mobEffectMapField);
    }

    public static void init() {
    }
}