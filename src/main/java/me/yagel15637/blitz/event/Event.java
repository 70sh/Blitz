package me.yagel15637.blitz.event;

import me.yagel15637.blitz.modifiers.EventEra;

/**
 * @author Reap
 */
public abstract class Event {
    private boolean cancelled = false;
    public final EventEra era;

    public Event(EventEra era) {
        this.era = era;
    }

    /**
     * @return is the event cancelled
     */
    public boolean isCancelled() { return cancelled; }

    /**
     * cancel the event
     */
    public void cancel() { this.cancelled = true; }
}