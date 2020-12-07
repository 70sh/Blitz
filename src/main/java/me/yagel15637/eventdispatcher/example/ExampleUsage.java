package me.yagel15637.eventdispatcher.example;

import me.yagel15637.eventdispatcher.dispatcher.DispatcherEntry;
import me.yagel15637.eventdispatcher.event.Event;
import me.yagel15637.eventdispatcher.dispatcher.EventDispatcher;
import me.yagel15637.eventdispatcher.modifiers.EventEra;
import me.yagel15637.eventdispatcher.modifiers.EventPriority;

/**
 * @author Reap
 *
 * good example for using EventDispatcher
 */
public class ExampleUsage {
    public static EventDispatcher dispatcher = new EventDispatcher();

    public static void main(String[] args) {
        ExampleUsage instance = new ExampleUsage();
        dispatcher.register(instance);
        dispatcher.setMultiThreading(true);
        dispatcher.dispatch(new Event1(EventEra.PRE));
        dispatcher.dispatch(new Event2(EventEra.PRE));
        dispatcher.dispatch(new EventWithVariables(10, 20, 30, EventEra.PRE));
        dispatcher.unregister(instance);
        dispatcher.dispatch(new Event3(EventEra.PRE));
    }

    @DispatcherEntry(priority = EventPriority.HIGH)
    public void onEvent1(Event1 event) {
        System.out.print("Hello ");
        event.cancel();
    }

    @DispatcherEntry
    public void onEvent1Take2(Event1 event) {
        System.out.println("This will not be displayed; another method that listens to Event 1 has higher priority and will always cancel it!");
    }

    @DispatcherEntry(era = EventEra.POST)
    public void onEvent1Post(Event1 event1) {
        System.out.println("This will not be displayed; there is no event 1 being called with EventEra set to POST.");
    }

    @DispatcherEntry
    public static void onEvent2(Event2 event) {
        System.out.println("World!");
    }

    @DispatcherEntry
    public static void onEvent3(Event3 event) {
        System.out.println("This will not be displayed; object main is unregistered!");
    }

    @DispatcherEntry
    public static void onEventWithVariables(EventWithVariables event) {
        System.out.println(event.int1 + event.int2 + event.int3);
    }

    public static class Event1 extends Event {
        public Event1(EventEra era) { super(era); }
    }
    public static class Event2 extends Event {
        public Event2(EventEra era) { super(era); }
    }
    public static class Event3 extends Event {
        public Event3(EventEra era) { super(era); }
    }
    public static class EventWithVariables extends Event {
        private final int int1;
        private final int int2;
        private final int int3;

        public EventWithVariables(int int1, int int2, int int3, EventEra era) {
            super(era);

            this.int1 = int1;
            this.int2 = int2;
            this.int3 = int3;
        }
    }
}
