package club.cafedevelopment.blitz.modifiers;

/**
 * @author Reap
 * constants for priorities, although it is advised to use normal {@link Integer}s instead for more customizability.
 */
public final class EventPriority {
    public static final int HIGHEST = Integer.MAX_VALUE;
    public static final int HIGH = Integer.MAX_VALUE / 2;
    public static final int MEDIUM = 0;
    public static final int LOW = Integer.MIN_VALUE / 2;
    public static final int LOWEST = Integer.MIN_VALUE;
}
