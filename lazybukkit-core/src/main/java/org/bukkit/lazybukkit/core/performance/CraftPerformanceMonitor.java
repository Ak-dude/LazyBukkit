package org.bukkit.lazybukkit.core.performance;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.lazybukkit.event.ServerTickEvent;
import org.bukkit.lazybukkit.performance.PerformanceMonitor;
import org.bukkit.lazybukkit.performance.TickTimingSnapshot;

/**
 * CraftBukkit implementation of the PerformanceMonitor API.
 * <p>
 * Measures tick duration by scheduling a task every tick and
 * recording the wall-clock time between invocations.
 */
public class CraftPerformanceMonitor implements PerformanceMonitor {

    private final Plugin plugin;
    private volatile TickTimingSnapshot lastSnapshot;
    private volatile long currentTick;

    // Rolling TPS calculation
    private static final int TPS_SAMPLE_SIZE = 100;
    private final long[] tickDurations = new long[TPS_SAMPLE_SIZE];
    private int tickIndex;

    // For TPS averaging
    private final long[] tpsTimestamps = new long[600]; // 30 seconds at 20 TPS
    private int tpsIndex;

    private long lastTickTime;
    private BukkitRunnable tickTask;

    public CraftPerformanceMonitor(Plugin plugin) {
        this.plugin = plugin;
        this.lastTickTime = System.nanoTime();
        this.currentTick = 0;
        startTickMonitor();
    }

    private void startTickMonitor() {
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.nanoTime();
                long elapsed = now - lastTickTime;
                lastTickTime = now;
                currentTick++;

                // Record tick duration
                tickDurations[(int) (currentTick % TPS_SAMPLE_SIZE)] = elapsed;

                // Record timestamp for TPS calculation
                tpsTimestamps[(int) (currentTick % tpsTimestamps.length)] = now;
                tpsIndex = (int) (currentTick % tpsTimestamps.length);

                // Count entities and chunks
                int entities = 0;
                int tileEntities = 0;
                int chunks = 0;
                for (World world : Bukkit.getWorlds()) {
                    entities += world.getEntities().size();
                    chunks += world.getLoadedChunks().length;
                    // Tile entity count requires CraftWorld access;
                    // approximate from loaded chunks
                }

                // Build snapshot
                lastSnapshot = new TickTimingSnapshot(
                    currentTick, elapsed, entities, tileEntities, chunks
                );

                // Fire ServerTickEvent
                Bukkit.getPluginManager().callEvent(
                    new ServerTickEvent(currentTick, elapsed, lastSnapshot)
                );
            }
        };
        tickTask.runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public double[] getRecentTps() {
        long now = System.nanoTime();
        return new double[]{
            calcTps(now, 20),   // ~1 second
            calcTps(now, 200),  // ~10 seconds (approximating 1 min for fast feedback)
            calcTps(now, 600)   // ~30 seconds (approximating 5 min)
        };
    }

    private double calcTps(long now, int sampleTicks) {
        if (currentTick < 2) return 20.0;

        int samples = (int) Math.min(sampleTicks, Math.min(currentTick, tpsTimestamps.length - 1));
        if (samples < 1) return 20.0;

        int currentIdx = (int) (currentTick % tpsTimestamps.length);
        int oldIdx = (int) ((currentTick - samples) % tpsTimestamps.length);
        if (oldIdx < 0) oldIdx += tpsTimestamps.length;

        long elapsed = tpsTimestamps[currentIdx] - tpsTimestamps[oldIdx];
        if (elapsed <= 0) return 20.0;

        double seconds = elapsed / 1_000_000_000.0;
        double tps = samples / seconds;
        return Math.min(20.0, tps);
    }

    @Override
    public TickTimingSnapshot getLastTickSnapshot() {
        TickTimingSnapshot snap = lastSnapshot;
        if (snap == null) {
            return new TickTimingSnapshot(0, 0, 0, 0, 0);
        }
        return snap;
    }

    @Override
    public long getCurrentTick() {
        return currentTick;
    }

    @Override
    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getAllocatedMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    @Override
    public long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    @Override
    public int getTotalLoadedChunks() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getLoadedChunks().length;
        }
        return total;
    }

    @Override
    public int getTotalEntityCount() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getEntities().size();
        }
        return total;
    }

    @Override
    public double getMemoryUsagePercent() {
        long max = getMaxMemory();
        if (max <= 0) return 0.0;
        long used = getAllocatedMemory() - getFreeMemory();
        return (double) used / max;
    }

    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
        }
    }
}
