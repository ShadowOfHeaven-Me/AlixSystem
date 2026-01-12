package shadow.systems.virtualization.manager;

import alix.common.utils.other.AlixUnsafe;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shadow.Main;
import shadow.utils.misc.ReflectionUtils;
import shadow.utils.users.Verifications;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.*;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class UserSemiVirtualization {

    private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
    /*private static final SpawnLocEventManager spawnLocEventManager;
    private static final JoinEventManager joinEventManager;
    private static final QuitEventManager quitEventManager;*/
    private static final VirtualEventManager[] listeners;

    //unsafeMode = true is deprecated
    private static final boolean unsafeMode = false;

    /*private static final List ENTITY_PLAYER_LIST;
    private static final Map ENTITY_PLAYERS_BY_UUID;
    private static final Map ENTITY_PLAYERS_BY_NAME;*/
    //public static final boolean isEnabled;

    static {
        //Main.logInfo("Started User Semi-Virtualization set-up...");
            /*Class<?> mcServerClass = ReflectionUtils.nms2("server.MinecraftServer");
            Object mcServer = mcServerClass.getMethod("getServer").invoke(null);

            Class<?> dedicatedPlayerListClazz = ReflectionUtils.nms2("server.dedicated.DedicatedPlayerList");
            Class<?> playerListClazz = dedicatedPlayerListClazz.getSuperclass();

            Field playerListField = ReflectionUtils.getFieldFromTypeAssignable(mcServerClass, playerListClazz);

            Object playerList = playerListField.get(mcServer);

            *//*Field f1 = ReflectionUtils.getFieldByTypeAndParams(playerListClazz, List.class, ReflectionUtils.entityPlayerClass);
            f1.set(playerList, new SemiVirtualizingList<>(ENTITY_PLAYER_LIST = (List) f1.get(playerList)));

            Field f2 = ReflectionUtils.getFieldByTypeAndParams(playerListClazz, Map.class, UUID.class, ReflectionUtils.entityPlayerClass);
            f2.set(playerList, new SemiVirtualizingMap<>(ENTITY_PLAYERS_BY_UUID = (Map) f2.get(playerList)));

            Field f3 = ReflectionUtils.getFieldByTypeAndParams(playerListClazz, Map.class, String.class, ReflectionUtils.entityPlayerClass);
            f3.set(playerList, new SemiVirtualizingMap<>(ENTITY_PLAYERS_BY_NAME = (Map) f3.get(playerList)));*/

        /*spawnLocEventManager = new SpawnLocEventManager();
        joinEventManager = new JoinEventManager();
        quitEventManager = new QuitEventManager();*/

        if (unsafeMode) {
            listeners = new VirtualEventManager[]{new SpawnLocEventManager(), new JoinEventManager(), new QuitEventManager()};
            Main.logInfo("Finished unsafe priority listeners load");
        } else {
            listeners = null;
            UserSafeExecutors.register();
            Main.logInfo("Finished safe priority listeners load");
        }
    }

    public static void returnOriginalSetup() {
        if (!unsafeMode)
            return;
        for (var listener : listeners)
            listener.returnOriginalHandler();
        /*spawnLocEventManager.returnOriginalHandler();
        joinEventManager.returnOriginalHandler();
        quitEventManager.returnOriginalHandler();*/
    }

/*    public static void invokeQuit0(AlixUser user) {
        quitEventManager.invokeOriginalListeners(new PlayerQuitEvent());
    }*/

/*    public static Location invokeVirtualizedSpawnLocationEventNoTeleport(UnverifiedUser user) {
        PlayerSpawnLocationEvent spawnLocationEvent = new PlayerSpawnLocationEvent(user.getPlayer(), user.originalSpawnEventLocation);
        spawnLocEventManager.invokeOriginalListeners(spawnLocationEvent);
        //CompletableFuture<Boolean> tp = MethodProvider.teleportAsync(user.player, spawnLocationEvent.getSpawnLocation());

        return spawnLocationEvent.getSpawnLocation();
    }*/

/*    private static final AlixMessage
            captchaJoinMessage = Messages.getAsObject("log-player-join-captcha-verified"),
            registerJoinMessage = Messages.getAsObject("log-player-join-registered"),
            loginJoinMessage = Messages.getAsObject("log-player-join-logged-in");

    public static void invokeVirtualizedJoinEvent(UnverifiedUser user) {
        //For some reason a world change already does this, so no need (PlayerList#respawn)
        *//*ENTITY_PLAYER_LIST.add(user.entityPlayer);
        ENTITY_PLAYERS_BY_NAME.put(user.getPlayer().getName().toLowerCase(Locale.ROOT), user.entityPlayer);
        ENTITY_PLAYERS_BY_UUID.put(user.getPlayer().getUniqueId(), user.entityPlayer);*//*
        PlayerJoinEvent joinEvent = new PlayerJoinEvent(user.getPlayer(), user.originalJoinMessage);
        joinEventManager.invokeOriginalListeners(joinEvent);

        AlixMessage msg = user.isCaptchaInitialized() ? captchaJoinMessage : user.joinedRegistered ? loginJoinMessage : registerJoinMessage;
        Main.logInfo(msg.format(user.getPlayer().getName(), user.getIPAddress()));

        if (joinEvent.getJoinMessage() != null) AlixUtils.broadcastRaw(joinEvent.getJoinMessage());

    }*/
/*    static abstract class VirtualizedListener extends RegisteredListener {

        final RegisteredListener original;

        VirtualizedListener(RegisteredListener original) throws Exception {
            super(original.getListener(), extractExecutor(original), original.getPriority(), original.getPlugin(), original.isIgnoringCancelled());
            this.original = original;
        }

        private static EventExecutor extractExecutor(RegisteredListener listener) throws Exception {
            return (EventExecutor) ReflectionUtils.getFieldAccessible(RegisteredListener.class, "executor").get(listener);
        }
    }*/

    static <T extends Event> VirtualEventManager.VirtualizedHandlerList replaceHandlers(Class<? extends Event> eventClazz, VirtualEventManager.VirtualEventExecutor<T> executor) {
        try {
            Field f = ReflectionUtils.getFieldFromTypeAssignable(eventClazz, HandlerList.class);// eventClazz.getDeclaredField("handlers");
            f.setAccessible(true);
            HandlerList original = (HandlerList) f.get(null);
            if (original == null)
                throw new AlixError("No HandlerList for " + eventClazz);
            VirtualEventManager.VirtualizedHandlerList virtualizedHandlerList = new VirtualEventManager.VirtualizedHandlerList(original, executor);
            UNSAFE.putObject(UNSAFE.staticFieldBase(f), UNSAFE.staticFieldOffset(f), virtualizedHandlerList);
            return virtualizedHandlerList;
        } catch (Exception e) {
            throw new AlixException(e);
        }
    }

    static void replaceVirtualHandlerWithOriginal(Class<? extends Event> eventClazz, VirtualEventManager.VirtualizedHandlerList virtualizedHandlerList) {
        try {
            Field f = ReflectionUtils.getFieldFromTypeAssignable(eventClazz, HandlerList.class);
            UNSAFE.putObject(UNSAFE.staticFieldBase(f), UNSAFE.staticFieldOffset(f), virtualizedHandlerList.originalHandler);
        } catch (Exception e) {
            throw new AlixException(e);
        }
    }

    //From SimplePluginManager
    static void invokeOriginalEventListeners(RegisteredListener[] originalListeners, Event event) {
        //Main.logError("ORIGINAL EVENT EXECUTED: " + event.getClass().getSimpleName());
        for (RegisteredListener registration : originalListeners) {
            if (registration.getPlugin().isEnabled()) {
                try {
                    registration.callEvent(event);
                } catch (AuthorNagException var10) {
                    Plugin plugin = registration.getPlugin();
                    if (plugin.isNaggable()) {
                        plugin.setNaggable(false);
                        Bukkit.getServer().getLogger().log(Level.SEVERE, String.format("Nag author(s): '%s' of '%s' about the following: %s", plugin.getDescription().getAuthors(), plugin.getDescription().getFullName(), var10.getMessage()));
                    }
                } catch (Throwable ex) {
                    Bukkit.getServer().getLogger().log(Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), ex);
                }
            }
        }
    }

    private static final class SemiVirtualizingMap<K, V> implements Map<K, V> {

        private final Map<K, V> original;

        private SemiVirtualizingMap(Map<K, V> original) {
            this.original = original;
        }

        @Nullable
        @Override
        public V put(K key, V entityPlayer) {
            try {
                Player craftPlayer = (Player) ReflectionUtils.getBukkitEntity_CraftPlayer.invoke(entityPlayer);
                return !Verifications.has(craftPlayer) ? original.put(key, entityPlayer) : null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public V remove(Object key) {
            return original.remove(key);
        }

        @Override
        public int size() {
            return original.size();
        }

        @Override
        public boolean isEmpty() {
            return original.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return original.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return original.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return original.get(key);
        }

        @Override
        public void clear() {
            original.clear();
        }

        @Override
        public void putAll(@NotNull Map<? extends K, ? extends V> m) {
            original.putAll(m);
        }

        @NotNull
        @Override
        public Set<K> keySet() {
            return original.keySet();
        }

        @NotNull
        @Override
        public Collection<V> values() {
            return original.values();
        }

        @NotNull
        @Override
        public Set<Entry<K, V>> entrySet() {
            return original.entrySet();
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object o) {
            return original.equals(o);
        }

        @Override
        public int hashCode() {
            return original.hashCode();
        }

        @Override
        public V getOrDefault(Object key, V defaultValue) {
            return original.getOrDefault(key, defaultValue);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            original.replaceAll(function);
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            original.forEach(action);
        }

        @Nullable
        @Override
        public V putIfAbsent(K key, V value) {
            return original.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return original.remove(key, value);
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            return original.replace(key, oldValue, newValue);
        }

        @Nullable
        @Override
        public V replace(K key, V value) {
            return original.replace(key, value);
        }

        @Override
        public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
            return original.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return original.computeIfPresent(key, remappingFunction);
        }

        @Override
        public V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return original.compute(key, remappingFunction);
        }

        @Override
        public V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            return original.merge(key, value, remappingFunction);
        }
    }

    private static final class SemiVirtualizingList<T> implements List<T> {

        private final List<T> original;

        private SemiVirtualizingList(List<T> original) {
            this.original = original;
        }

        @Override
        public boolean add(T entityPlayer) {
            try {
                //Main.logError("ADD INVOKED");
                Player craftPlayer = (Player) ReflectionUtils.getBukkitEntity_CraftPlayer.invoke(entityPlayer);
                if (!Verifications.has(craftPlayer)) return original.add(entityPlayer);
                else return false;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        public boolean remove(Object o) {
            //boolean b = original.remove(o);
            //Main.logError("REMOVE INVOKED - " + b);
            return original.remove(o);
        }

        @Override
        public void add(int index, T entityPlayer) {
            original.add(index, entityPlayer);
            /*try {
                Main.logError("ADD INVOKED");
                Player craftPlayer = (Player) ReflectionUtils.getBukkitEntity_CraftPlayer.invoke(entityPlayer);
                if (!Verifications.has(craftPlayer)) original.add(index, entityPlayer);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }*/
        }

        @Override
        public int size() {
            return original.size();
        }

        @Override
        public boolean isEmpty() {
            return original.isEmpty();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            original.forEach(action);
        }

        @Override
        public Stream<T> parallelStream() {
            return original.parallelStream();
        }

        @Override
        public boolean removeIf(Predicate<? super T> filter) {
            return original.removeIf(filter);
        }

        @Override
        public Stream<T> stream() {
            return original.stream();
        }

        @Override
        public Spliterator<T> spliterator() {
            return original.spliterator();
        }

        @NotNull
        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return original.subList(fromIndex, toIndex);
        }

        @NotNull
        @Override
        public ListIterator<T> listIterator(int index) {
            return original.listIterator(index);
        }

        @NotNull
        @Override
        public ListIterator<T> listIterator() {
            return original.listIterator();
        }

        @Override
        public int lastIndexOf(Object o) {
            return original.lastIndexOf(o);
        }

        @Override
        public int indexOf(Object o) {
            return original.indexOf(o);
        }

        @Override
        public T remove(int index) {
            return original.remove(index);
        }

        @Override
        public T set(int index, T element) {
            return original.set(index, element);
        }

        @Override
        public T get(int index) {
            return original.get(index);
        }

        @Override
        public int hashCode() {
            return original.hashCode();
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object o) {
            return original.equals(o);
        }

        @Override
        public void clear() {
            original.clear();
        }

        @Override
        public boolean addAll(int index, @NotNull Collection<? extends T> c) {
            return original.addAll(index, c);
        }

        @Override
        public void sort(Comparator<? super T> c) {
            original.sort(c);
        }

        @Override
        public void replaceAll(UnaryOperator<T> operator) {
            original.replaceAll(operator);
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return original.retainAll(c);
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return original.removeAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends T> c) {
            return original.addAll(c);
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return new HashSet<>(original).containsAll(c);
        }

        @NotNull
        @Override
        public <E> E[] toArray(@NotNull E[] a) {
            return original.toArray(a);
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return original.toArray();
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return original.iterator();
        }

        @Override
        public boolean contains(Object o) {
            return original.contains(o);
        }
    }

    public static void init() {
    }
}