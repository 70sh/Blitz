package me.yagel15637.eventdispatcher.event;

import me.yagel15637.eventdispatcher.modifiers.EventEra;

public abstract class Event {
    private boolean cancelled = false;
    public final EventEra era;

    public Event(EventEra era) {
        this.era = era;
    }

    public boolean isCancelled() { return cancelled; }
    public void cancel() { this.cancelled = true; }
}
