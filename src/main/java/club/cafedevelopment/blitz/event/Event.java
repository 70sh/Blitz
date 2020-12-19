package club.cafedevelopment.blitz.event;

import club.cafedevelopment.blitz.dispatcher.DispatcherEntry;
import club.cafedevelopment.blitz.modifiers.EventEra;

/**
 * @author Reap
 */
public abstract class Event {
    private boolean cancelled = false;
    public final EventEra era;

    /**
     * will be used when you will make an event that does have eras
     * @param era the era for the event
     */
    public Event(EventEra era) { this.era = era; }

    /**
     * will be used when you will make an event that does not have eras, as {@link EventEra#PRE} is the default for {@link DispatcherEntry#era()}
     */
    public Event() { this.era = EventEra.PRE; }

    /**
     * @return is the event cancelled
     */
    public boolean isCancelled() { return cancelled; }

    /**
     * cancel the event
     */
    public void cancel() { this.cancelled = true; }
}
