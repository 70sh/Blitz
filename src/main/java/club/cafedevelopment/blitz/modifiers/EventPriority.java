package club.cafedevelopment.blitz.modifiers;

/**
 * @author Reap
 * constant priorities aiming for backwards compatibility for before 3.0, although it is advised to use normal {@link Integer}s instead for more customizability. Is marked as {@link Deprecated} to have people switch to normal Integers instead.
 */
@Deprecated @SuppressWarnings("unused")
public final class EventPriority {
    public static final int HIGHEST = Integer.MAX_VALUE;
    public static final int HIGH = Integer.MAX_VALUE / 2;
    public static final int MEDIUM = 0;
    public static final int LOW = Integer.MIN_VALUE / 2;
    public static final int LOWEST = Integer.MIN_VALUE;
}
