package org.bukkit.lazybukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.lazybukkit.performance.TickTimingSnapshot;

/**
 * Called at the end of every server tick.
 * <p>
 * This event is fired on the main server thread after all tick work
 * is complete. It cannot be cancelled — it is purely informational.
 * <p>
 * <b>Performance note:</b> Handlers for this event fire every tick
 * (up to 20 times per second). Keep handlers lightweight. For periodic
 * work, check {@link #getTickNumber()} modulo your desired interval.
 * <p>
 * Example usage:
 * <pre>
 * {@literal @}EventHandler
 * public void onTick(ServerTickEvent event) {
 *     // Run every 100 ticks (~5 seconds)
 *     if (event.getTickNumber() % 100 == 0) {
 *         if (event.isLagging()) {
 *             getLogger().warning("Server is lagging! Tick took "
 *                 + String.format("%.1f", event.getTickDurationMillis()) + "ms");
 *         }
 *     }
 * }
 * </pre>
 */
public class ServerTickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final long tickNumber;
    private final long tickDurationNanos;
    private final TickTimingSnapshot snapshot;

    /**
     * @param tickNumber      The absolute tick number since server start
     * @param tickDurationNanos Duration of this tick in nanoseconds
     * @param snapshot         Full timing snapshot for this tick, or null
     */
    public ServerTickEvent(long tickNumber, long tickDurationNanos,
                           TickTimingSnapshot snapshot) {
        this.tickNumber = tickNumber;
        this.tickDurationNanos = tickDurationNanos;
        this.snapshot = snapshot;
    }

    /**
     * @return The absolute tick number since server start
     */
    public long getTickNumber() {
        return tickNumber;
    }

    /**
     * @return Duration of the completed tick in nanoseconds
     */
    public long getTickDurationNanos() {
        return tickDurationNanos;
    }

    /**
     * @return Duration of the completed tick in milliseconds
     */
    public double getTickDurationMillis() {
        return tickDurationNanos / 1_000_000.0;
    }

    /**
     * @return true if this tick exceeded the 50ms budget (server running below 20 TPS)
     */
    public boolean isLagging() {
        return tickDurationNanos > 50_000_000L;
    }

    /**
     * Returns the full timing snapshot for this tick, if available.
     *
     * @return Timing snapshot, or null if detailed timing is disabled
     */
    public TickTimingSnapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
