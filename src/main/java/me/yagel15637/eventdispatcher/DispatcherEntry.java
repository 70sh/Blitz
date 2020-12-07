package me.yagel15637.eventdispatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DispatcherEntry {
    EventEra era();
    EventPriority priority() default EventPriority.MEDIUM;
}
