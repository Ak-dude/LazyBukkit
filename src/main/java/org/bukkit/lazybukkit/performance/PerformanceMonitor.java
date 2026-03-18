package org.bukkit.lazybukkit.performance;

/**
 * Provides real-time server performance metrics.
 * <p>
 * Obtain an instance via the {@link org.bukkit.plugin.ServicesManager}:
 * <pre>
 * PerformanceMonitor monitor = Bukkit.getServicesManager()
 *     .getRegistration(PerformanceMonitor.class).getProvider();
 * </pre>
 * <p>
 * All methods are safe to call from any thread. Returned data is
 * either immutable or a primitive — no synchronization needed by callers.
 */
public interface PerformanceMonitor {

    /**
     * Returns recent TPS averages.
     *
     * @return Array of three doubles: [1-second, 1-minute, 5-minute] averages.
     *         Values are capped at 20.0 (the vanilla target).
     */
    double[] getRecentTps();

    /**
     * Returns a snapshot of the most recently completed tick.
     *
     * @return Immutable timing snapshot, never null
     */
    TickTimingSnapshot getLastTickSnapshot();

    /**
     * Returns the number of ticks since the server started.
     *
     * @return Current tick count
     */
    long getCurrentTick();

    /**
     * Returns free JVM heap memory in bytes.
     *
     * @return Free memory in bytes
     */
    long getFreeMemory();

    /**
     * Returns total JVM heap memory currently allocated in bytes.
     *
     * @return Allocated memory in bytes
     */
    long getAllocatedMemory();

    /**
     * Returns maximum JVM heap memory in bytes.
     *
     * @return Max memory in bytes
     */
    long getMaxMemory();

    /**
     * Returns the total number of loaded chunks across all worlds.
     *
     * @return Loaded chunk count
     */
    int getTotalLoadedChunks();

    /**
     * Returns the total number of entities across all worlds.
     *
     * @return Entity count
     */
    int getTotalEntityCount();

    /**
     * Returns memory usage as a percentage (0.0 to 1.0).
     *
     * @return Memory usage ratio
     */
    double getMemoryUsagePercent();
}
