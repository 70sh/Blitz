package club.cafedevelopment.blitz.dispatcher;

import club.cafedevelopment.blitz.event.Event;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Reap
 */
@SuppressWarnings({"unused", "unchecked"})
public final class EventDispatcher {
    /**
     * determines whether to send debug messages to the {@link #stream} or not
     */
    public boolean DEBUG = false;

    /**
     * the {@link PrintStream} we are debugging onto; is {@link System#out} by default
     */
    private PrintStream stream = System.out;
    public void setDebugStream(PrintStream stream) { this.stream = stream; }
    public PrintStream getDebugStream() { return stream; }

    /**
     * stores all registered objects and the filtered methods for them by events.
     */
    private final HashMap<Object, HashMap<Class<? extends Event>, List<Method>>> listenerMap = new HashMap<>();

    /**
     * the cached object hash maps, to optimize re-registering objects. we will attempt to use the cache first when registering,
     * if it does not include the linked object we will filter the listeners and put them in the listenerMap and the cache both.
     */
    private final HashMap<Object, HashMap<Class<? extends Event>, List<Method>>> cache = new HashMap<>();

    /**
     * indicates whether we'll start a new thread to dispatch the event.
     */
    private boolean multiThreading = false;
    public boolean isMultithreading() { return multiThreading; }
    public void setMultiThreading(boolean multiThreading) { this.multiThreading = multiThreading; }

    /**
     * looks through all the object's methods, and stores all event listeners under object in {@link EventDispatcher#listenerMap}
     * @param object the object being registered
     */
    public void register(Object object) {
        if (cache.containsKey(object)) {
            listenerMap.put(object, cache.get(object));
            if (DEBUG) stream.println("Registered " + object + " from the cache.");
            return;
        }

        List<Method> methods = Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(it -> it.isAnnotationPresent(DispatcherEntry.class))
                .filter(
                        it -> it.getParameterCount() == 1 &&
                                it.getParameterTypes()[0]
                                        .getSuperclass().isAssignableFrom(Event.class)
                ).collect(Collectors.toList());

        HashMap<Class<? extends Event>, List<Method>> filteredMethods = new HashMap<>();

        for (Method method : methods) {
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];

            List<Method> methodList = filteredMethods.containsKey(eventClass) ? filteredMethods.get(eventClass) : new ArrayList<>();
            methodList.add(method);
            filteredMethods.put(
                    eventClass, methodList.stream()
                            .sorted(
                                    Comparator.comparing(it -> -it.getDeclaredAnnotation(DispatcherEntry.class).priority())
                            ).collect(Collectors.toList())
            );
        }

        listenerMap.put(object, filteredMethods);
        if (DEBUG) stream.println("Registered " + object + " for the first time.");
    }

    /**
     * removes an object and all it's listeners from {@link EventDispatcher#listenerMap}, also will put the object's listeners in the cache if it is not present.
     * @param object the object being removed
     */
    public void unregister(Object object) {
        if (!cache.containsKey(object)) {
            cache.put(object, listenerMap.get(object));
            if (DEBUG) stream.println("Unregistered " + object + " for the first time and put it's listeners in the cache.");
        }

        listenerMap.remove(object);
        if (DEBUG) stream.println("Unregistered " + object);
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
                        .filter(it -> it.getDeclaredAnnotation(DispatcherEntry.class).era() == event.era).collect(Collectors.toList())) {
                    invoke(m, entry.getKey(), event);
                    if (DEBUG) stream.println("Invoked " + event + " on Method " + m);
                    if (event.isCancelled()) return;
                }
            }
        }

        if (DEBUG) stream.println("Finished dispatching " + event);
    }

    /**
     * wrapper for {@link EventDispatcher#dispatch0(Event)} to consider {@link EventDispatcher#multiThreading}
     * TODO fix MultiThreading.
     * @param event the event being dispatched
     * @param <T> type of the event - better reflection checks
     */
    public synchronized <T extends Event> void dispatch(T event) {
        if (multiThreading) new Thread(() -> dispatch0(event)).start();
        else dispatch0(event);
    }

    /**
     * invokes an event so we don't have to put try catch out where it is
     * @param m the method to invoke
     * @param object the object the method is extracted from
     * @param args the arguments for the method
     */
    private void invoke(Method m, Object object, Object... args) {
        try {
            m.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
