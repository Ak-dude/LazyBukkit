package org.bukkit.lazybukkit.performance;

/**
 * Immutable snapshot of a single server tick's timing data.
 * Thread-safe by design — all fields are final.
 */
public final class TickTimingSnapshot {
    private final long tickNumber;
    private final long durationNanos;
    private final int entityCount;
    private final int tileEntityCount;
    private final int loadedChunks;

    public TickTimingSnapshot(long tickNumber, long durationNanos,
                              int entityCount, int tileEntityCount,
                              int loadedChunks) {
        this.tickNumber = tickNumber;
        this.durationNanos = durationNanos;
        this.entityCount = entityCount;
        this.tileEntityCount = tileEntityCount;
        this.loadedChunks = loadedChunks;
    }

    /**
     * @return The absolute tick number since server start
     */
    public long getTickNumber() {
        return tickNumber;
    }

    /**
     * @return Duration of this tick in nanoseconds
     */
    public long getDurationNanos() {
        return durationNanos;
    }

    /**
     * @return Duration of this tick in milliseconds
     */
    public double getDurationMillis() {
        return durationNanos / 1_000_000.0;
    }

    /**
     * @return Whether this tick exceeded the 50ms target (20 TPS)
     */
    public boolean isOverBudget() {
        return durationNanos > 50_000_000L;
    }

    /**
     * @return Total entities ticked in this cycle
     */
    public int getEntityCount() {
        return entityCount;
    }

    /**
     * @return Total tile entities (chests, furnaces, hoppers, etc.) ticked
     */
    public int getTileEntityCount() {
        return tileEntityCount;
    }

    /**
     * @return Total loaded chunks across all worlds
     */
    public int getLoadedChunks() {
        return loadedChunks;
    }

    @Override
    public String toString() {
        return "TickTimingSnapshot{tick=" + tickNumber
                + ", duration=" + String.format("%.2f", getDurationMillis()) + "ms"
                + ", entities=" + entityCount
                + ", tileEntities=" + tileEntityCount
                + ", chunks=" + loadedChunks + "}";
    }
}
