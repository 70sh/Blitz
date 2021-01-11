package club.cafedevelopment.blitz.dispatcher;

import club.cafedevelopment.blitz.event.Event;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Reap
 */
@SuppressWarnings({"unused", "unchecked"})
public final class EventDispatcher {
    /**
     * A public Event Dispatcher for the program.
     * TODO: Make a system-wide event dispatcher, and global events.
     */
    private static final EventDispatcher SYSTEM_EVENT_DISPATCHER = new EventDispatcher();
    public static EventDispatcher getSystemEventDispatcher() { return SYSTEM_EVENT_DISPATCHER; }

    /**
     * determines whether to send debug messages to the {@link #stream} or not; is {@link Boolean#FALSE} by default.
     */
    private boolean debug = false;
    public boolean isDebugging() { return debug; }
    public void setDebugging(boolean debug) { this.debug = debug; }

    /**
     * indicates whether we'll cache objects after unregistering them or not; is {@link Boolean#TRUE} by default.
     */
    private boolean caching = true;
    public boolean isCaching() { return caching; }
    public void setCaching(boolean caching) { this.caching = caching; }

    /**
     * indicates whether we'll remove objects from the cache after registering them or not; is {@link Boolean#TRUE} by default.
     */
    private boolean compactCaching = true;
    public boolean isCompactCaching() { return compactCaching; }
    public void setCompactCaching(boolean compactCaching) { this.compactCaching = compactCaching; }

    /**
     * determines whether events should get sent to the system event dispatcher.
     */
    private boolean dispatchToSystemDispatcher = false;
    public boolean isDispatchToSystemDispatcher() { return dispatchToSystemDispatcher; }
    public void setDispatchToSystemDispatcher(boolean dispatchToSystemDispatcher) {
        this.dispatchToSystemDispatcher = dispatchToSystemDispatcher;
    }

    /**
     * indicates whether we'll start a new thread to dispatch the event; is {@link Boolean#TRUE} by default.
     */
    private boolean multiThreading = false;
    public boolean isMultithreading() { return multiThreading; }
    public void setMultiThreading(boolean multiThreading) { this.multiThreading = multiThreading; }

    /**
     * the {@link PrintStream} we are debugging onto; is {@link System#out} by default.
     */
    private PrintStream stream = System.out;
    public PrintStream getStream() { return stream; }
    public void setStream(PrintStream stream) { this.stream = stream; }

    /**
     * stores all registered objects and the filtered methods for them by events.
     */
    private final HashMap<Object, HashMap<Class<? extends Event>, List<Method>>> listenerMap = new HashMap<>();
    public HashMap<Object, HashMap<Class<? extends Event>, List<Method>>> getListenerMap() { return listenerMap; }

    /**
     * the cached object hash maps, to optimize re-registering objects. we will attempt to use the cache first when registering,
     * if it does not include the linked object we will filter the listeners and put them in the listenerMap and the cache both.
     */
    private final HashMap<Object, HashMap<Class<? extends Event>, List<Method>>> cache = new HashMap<>();
    public HashMap<Object, HashMap<Class<? extends Event>, List<Method>>> getCache() { return cache; }

    /**
     * looks through all the object's methods, and stores all event listeners under object in {@link EventDispatcher#listenerMap}
     * @param object the object being registered
     */
    public void register(Object object) {
        if (caching && cache.containsKey(object)) {
            listenerMap.put(object, cache.get(object));
            if (compactCaching) {
                cache.remove(object);
                if (debug) stream.println("[Blitz] Registered " + object + " from the cache and cleared it. (compactCaching=true)");
            } else if (debug) stream.println("[Blitz] Registered " + object + " from the cache. (compactCaching=false)");
            return;
        }

        List<Method> methods = Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(it -> it.isAnnotationPresent(DispatcherEntry.class))
                .filter(it ->
                        it.getParameterCount() == 1 &&
                        it.getParameterTypes()[0].getSuperclass().isAssignableFrom(Event.class)
                ).collect(Collectors.toList());

        HashMap<Class<? extends Event>, List<Method>> filteredMethods = new HashMap<>();

        for (Method method : methods) {
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];

            List<Method> methodList = filteredMethods.containsKey(eventClass) ? filteredMethods.get(eventClass) : new ArrayList<>();
            methodList.add(method);
            filteredMethods.put(eventClass,
                    methodList.stream()
                            .sorted(Comparator.comparing(it -> -it.getDeclaredAnnotation(DispatcherEntry.class).priority()))
                            .collect(Collectors.toList())
            );
        }

        listenerMap.put(object, filteredMethods);
        if (debug) stream.println("[Blitz] Registered " + object + " for the first time.");
    }

    /**
     * removes an object and all it's listeners from {@link EventDispatcher#listenerMap}, also will put the object's listeners in the cache if it is not present.
     * @param object the object being removed
     */
    public void unregister(Object object) {
        if (caching && !cache.containsKey(object)) {
            cache.put(object, listenerMap.get(object));
            if (debug) stream.println("[Blitz] Unregistered " + object + " and put it's listeners in the cache.");
        }

        listenerMap.remove(object);
        if (debug) stream.println("[Blitz] Unregistered " + object);
    }

    /**
     * dispatches an {@link Event}
     * @param event the event being dispatched
     * @param <T> type of the event - better reflection checks
     */
    private synchronized <T extends Event> void dispatch0(T event) {
        for (Map.Entry<Object, HashMap<Class<? extends Event>, List<Method>>> entry : listenerMap.entrySet()) {
            if (entry.getValue().containsKey(event.getClass())) {
                for (Method m : entry.getValue().get(event.getClass())
                        .stream()
                        .filter(it -> it.getDeclaredAnnotation(DispatcherEntry.class).era() == event.era)
                        .collect(Collectors.toList())) {
                    try {
                        if (listenerMap.containsKey(entry.getKey())) m.invoke(entry.getKey(), event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        if (debug) stream.println(e.getMessage());
                    }
                    if (debug) stream.println("[Blitz] Invoked " + event + " on Method " + m + " from Thread " + Thread.currentThread());
                    if (event.isCancelled()) return;
                }
            }
        }

        if (debug) stream.println("[Blitz] Finished dispatching " + event);
    }

    /**
     * Thread Group for Multithreading.
     */
    private final ExecutorService service = Executors.newFixedThreadPool(5);
    public void shutdown() { service.shutdown(); }

    /**
     * wrapper for {@link EventDispatcher#dispatch0(Event)} to consider {@link EventDispatcher#multiThreading}
     * @param event the event being dispatched
     * @param <T> type of the event - better reflection checks
     */
    public <T extends Event> void dispatch(T event) {
        if (multiThreading) service.submit(() -> dispatch0(event));
        else dispatch0(event);

        if (dispatchToSystemDispatcher && this != SYSTEM_EVENT_DISPATCHER) service.submit(() -> dispatch0(event));
    }

    /**
     * Overrides {@link Object#clone()} to have the new EventDispatcher use a different Thread Pool to not flood this one.
     * @return a new EventDispatcher with all the properties of this one, using a new Thread Pool.
     */
    @SuppressWarnings("all")
    @Override public EventDispatcher clone() {
        EventDispatcher clone = new EventDispatcher();
        clone.setDebugging(debug);
        clone.setCaching(caching);
        clone.setCompactCaching(compactCaching);
        clone.setDispatchToSystemDispatcher(dispatchToSystemDispatcher);
        clone.setMultiThreading(multiThreading);
        clone.setStream(stream);
        clone.getListenerMap().putAll(listenerMap);
        clone.getCache().putAll(cache);
        return clone;
    }
}
