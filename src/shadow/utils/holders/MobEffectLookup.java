package shadow.utils.holders;

import org.bukkit.potion.PotionEffectType;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public final class MobEffectLookup {

    //This method looks for a back-up id to
    //mob effect method by looking for a
    //IRegistry<MobEffectList> in the IRegistry class
/*    protected static Method find0() {
        Class<?> c = ReflectionUtils.forName("net.minecraft.core.registries.BuiltInRegistries");
        Class<?> registryNewerClass = ReflectionUtils.forName("net.minecraft.core.Registry");
        try {
            Player player;
            player.addPotionEffect(
            for (Field f : c.getDeclaredFields()) {
                f.setAccessible(true);
                if (registryNewerClass.isAssignableFrom(f.getType()) && f.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) f.getGenericType();
                    Type[] types = type.getActualTypeArguments();
                    //AlixUtils.debug(f);
                    if (types.length == 1 && types[0] == ReflectionUtils.mobEffectListClass) {//it is the IRegistry<MobEffectList> field
                        Object mobEffectIRegistry = f.get(null);
                        Main.logError("FIELD 222222 " + mobEffectIRegistry);
                        for (Method m : mobEffectIRegistry.getClass().getMethods()) {
                            Class<?>[] params = m.getParameterTypes();
                            Class<?> returnType = m.getReturnType();
                            *//*if (params.length == 1 && params[0] == int.class && m.getGenericReturnType() instanceof ParameterizedType) {
                                ParameterizedType paramReturnType = (ParameterizedType) m.getGenericReturnType();
                                Main.logError("TYPE-UH " + returnType.toGenericString() + " TYPE " + m.getGenericReturnType() + " " + paramReturnType.getActualTypeArguments()[0]);
                            }*//*
                            //ReflectionUtils.mobEffectListClass.isAssignableFrom(returnType)
                            // && ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0]
                            if (returnType == Optional.class && params.length == 1 && params[0] == int.class) {//Is is the Optional<Holder.Reference<
                                Class<?> holderClass = (Class<?>) ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0]; //it is the Holder$Reference class
                                for(Method m2 : holderClass.getMethods()) {
                                    if(m2
                                }
                                return m;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            //AlixUtils.debug(c.getDeclaredFields(), Field::toGenericString, '\n');
            throw new Error("Error occurred at - " + c.getSimpleName(), e);
        }
        //AlixUtils.debug(c.getDeclaredFields(), Field::toGenericString, '\n');
        throw new Error("Not found at - " + c.getSimpleName());//+ " - Methods: " + Arrays.toString(c.getMethods()));
    }*/

    private static MobEffectSupplier get_1_20_2() {
        Class<?> c = ReflectionUtils.obc("potion.CraftPotionEffectType");
        Method method = ReflectionUtils.getMethod(c, "bukkitToMinecraft", PotionEffectType.class);
        return new MethodEffectSupplier_1_20_2(method);
    }

    protected static MobEffectSupplier getSupplier(Supplier<Method> supplier) {
        try {
            return new MethodEffectSupplier_Old(supplier.get());
        } catch (Throwable e) {//The Supplier#get may throw an error that the method was not found
            return get_1_20_2();
        }
    }

    private static final class MethodEffectSupplier_1_20_2 implements MobEffectSupplier {

        private final Method method;

        private MethodEffectSupplier_1_20_2(Method method) {
            this.method = method;
        }

        @Override
        public Object toNMSEffectTypeFromId(int id) {
            PotionEffectType type = PotionEffectType.getById(id);
            try {
                return this.method.invoke(null, type);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class MethodEffectSupplier_Old implements MobEffectSupplier {

        private final Method method;

        private MethodEffectSupplier_Old(Method method) {//below but including 1.20.1
            this.method = method;
        }

        @Override
        public Object toNMSEffectTypeFromId(int id) {
            try {
                return method.invoke(null, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private MobEffectLookup() {
    }
}