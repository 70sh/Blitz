package me.yagel15637.eventdispatcher;

public class Event {
    private boolean cancelled = false;
    public final EventEra era;

    public Event(EventEra era) {
        this.era = era;
    }

    public boolean isCancelled() { return cancelled; }
    public void cancel() { this.cancelled = true; }
}
