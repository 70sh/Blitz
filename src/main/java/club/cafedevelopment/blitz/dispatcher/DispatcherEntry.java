package club.cafedevelopment.blitz.dispatcher;

import club.cafedevelopment.blitz.modifiers.EventEra;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Reap
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DispatcherEntry {
    /**
     * @return the era to listen to ({@link EventEra#PRE} or {@link EventEra#POST})
     */
    EventEra era() default EventEra.PRE;

    /**
     * @return the listener's priority
     */
    int priority() default 500000;
}
