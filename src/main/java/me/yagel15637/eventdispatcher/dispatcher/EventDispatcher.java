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
     * stores all registered objects and the filtered methods for them.
     */
    private final HashMap<Object, ArrayList<Method>> listenerMap = new HashMap<>();

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

        listenerMap.put(object, methods);
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
    public <T extends Event> void dispatch(T event) {
        for (Map.Entry<Object, ArrayList<Method>> entry : listenerMap.entrySet()) {
            for (Method m : filterArrayList(entry.getValue(), event)) {
                invoke(m, entry.getKey(), event);
                if (event.isCancelled()) return;
            }
        }
    }

    private void invoke(Method m, Object caller, Object... args) {
        try {
            m.invoke(caller, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param arrayList the {@link ArrayList} being filtered
     * @param event the {@link Event}
     * @param <T> type of the {@link Event} - better reflection
     * @return A filtered {@link ArrayList}
     */
    private <T extends Event> List<Method> filterArrayList(ArrayList<Method> arrayList, T event) {
        return arrayList.stream()
                .filter(
                        it -> it.getDeclaredAnnotation(DispatcherEntry.class)
                                .era() == event.era
                ).filter(
                        it -> it.getParameterTypes()[0].isAssignableFrom(event.getClass())
                ).sorted(
                        Comparator.comparing(
                                it -> it.getDeclaredAnnotation(DispatcherEntry.class).priority().ordinal()
                        )
                )
                .collect(Collectors.toList());
    }
}
