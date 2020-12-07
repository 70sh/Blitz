package me.yagel15637.eventdispatcher.dispatcher;

import me.yagel15637.eventdispatcher.modifiers.EventEra;
import me.yagel15637.eventdispatcher.modifiers.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DispatcherEntry {
    EventEra era() default EventEra.PRE;
    EventPriority priority() default EventPriority.MEDIUM;
}
