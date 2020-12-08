package me.yagel15637.eventdispatcher.dispatcher;

import me.yagel15637.eventdispatcher.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Reap
 */
public final class EventDispatcher {
    /**
     * stores all registered objects and the filtered methods for them by events.
     * TODO find a better method for that...
     */
    private final HashMap<Object, HashMap<Class<? extends Event>, List<Method>>> listenerMap = new HashMap<>();

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
        ArrayList<Method> methods = Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(it -> it.isAnnotationPresent(DispatcherEntry.class))
                .filter(
                        it -> it.getParameterCount() == 1 &&
                                it.getParameterTypes()[0]
                                        .getSuperclass().isAssignableFrom(Event.class)
                ).collect(Collectors.toCollection(ArrayList::new));

        HashMap<Class<? extends Event>, List<Method>> filteredMethods = new HashMap<>();

        for (Method method : methods) {
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];

            List<Method> methodList = filteredMethods.containsKey(eventClass) ? filteredMethods.get(eventClass) : new ArrayList<>();
            methodList.add(method);
            filteredMethods.put(
                    eventClass, methodList.stream()
                            .filter(it -> it.getDeclaredAnnotation(DispatcherEntry.class) != null)
                            .sorted(
                                    Comparator.comparing(it -> it.getDeclaredAnnotation(DispatcherEntry.class).priority().ordinal())
                            ).collect(Collectors.toList())
            );
        }

        listenerMap.put(object, filteredMethods);
    }

    /**
     * removes an object and all it's listeners from {@link EventDispatcher#listenerMap}
     * @param object the object being removed
     */
    public void unregister(Object object) { listenerMap.remove(object); }

    /**
     * dispatches an {@link Event}, PRE then POST
     * @param event the event being dispatched
     * @param <T> type of the event - better reflection checks
     */
    private synchronized <T extends Event> void dispatch0(T event) {
        for (Map.Entry<Object, HashMap<Class<? extends Event>, List<Method>>> entry : listenerMap.entrySet()) {
            if (entry.getValue().containsKey(event.getClass())) {
                for (Method m : entry.getValue().get(event.getClass())) {
                    invoke(m, entry.getKey(), event);
                    if (event.isCancelled()) return;
                }
            }
        }
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

    private void invoke(Method m, Object caller, Object... args) {
        try {
            m.invoke(caller, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
