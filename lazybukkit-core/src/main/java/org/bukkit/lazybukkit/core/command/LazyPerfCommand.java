package org.bukkit.lazybukkit.core.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.lazybukkit.performance.PerformanceMonitor;
import org.bukkit.lazybukkit.performance.TickTimingSnapshot;

/**
 * /lazyperf command — displays server performance metrics.
 */
public class LazyPerfCommand implements CommandExecutor {

    private final PerformanceMonitor monitor;

    public LazyPerfCommand(PerformanceMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        String sub = args.length > 0 ? args[0].toLowerCase() : "tps";

        switch (sub) {
            case "tps":
                showTps(sender);
                return true;
            case "memory":
            case "mem":
                showMemory(sender);
                return true;
            case "snapshot":
            case "tick":
                showSnapshot(sender);
                return true;
            case "full":
                showTps(sender);
                showMemory(sender);
                showSnapshot(sender);
                return true;
            default:
                sender.sendMessage(ChatColor.GOLD + "Usage: /lazyperf [tps|memory|snapshot|full]");
                return true;
        }
    }

    private void showTps(CommandSender sender) {
        double[] tps = monitor.getRecentTps();
        sender.sendMessage(ChatColor.GOLD + "=== LazyBukkit TPS ===");
        sender.sendMessage(ChatColor.WHITE + "  1s:  " + formatTps(tps[0]));
        sender.sendMessage(ChatColor.WHITE + "  10s: " + formatTps(tps[1]));
        sender.sendMessage(ChatColor.WHITE + "  30s: " + formatTps(tps[2]));
    }

    private void showMemory(CommandSender sender) {
        long free = monitor.getFreeMemory();
        long allocated = monitor.getAllocatedMemory();
        long max = monitor.getMaxMemory();
        long used = allocated - free;
        double percent = monitor.getMemoryUsagePercent() * 100;

        sender.sendMessage(ChatColor.GOLD + "=== LazyBukkit Memory ===");
        sender.sendMessage(ChatColor.WHITE + "  Used:      " + mb(used)
            + " / " + mb(max) + " (" + String.format("%.1f%%", percent) + ")");
        sender.sendMessage(ChatColor.WHITE + "  Allocated: " + mb(allocated));
        sender.sendMessage(ChatColor.WHITE + "  Free:      " + mb(free));
    }

    private void showSnapshot(CommandSender sender) {
        TickTimingSnapshot snap = monitor.getLastTickSnapshot();
        sender.sendMessage(ChatColor.GOLD + "=== LazyBukkit Tick Snapshot ===");
        sender.sendMessage(ChatColor.WHITE + "  Tick #:       " + snap.getTickNumber());
        sender.sendMessage(ChatColor.WHITE + "  Duration:     "
            + String.format("%.2f", snap.getDurationMillis()) + "ms"
            + (snap.isOverBudget() ? ChatColor.RED + " (OVER BUDGET)" : ChatColor.GREEN + " (OK)"));
        sender.sendMessage(ChatColor.WHITE + "  Entities:     " + snap.getEntityCount());
        sender.sendMessage(ChatColor.WHITE + "  Tile entities:" + snap.getTileEntityCount());
        sender.sendMessage(ChatColor.WHITE + "  Chunks:       " + snap.getLoadedChunks());
    }

    private String formatTps(double tps) {
        ChatColor color;
        if (tps >= 19.5) color = ChatColor.GREEN;
        else if (tps >= 17.0) color = ChatColor.YELLOW;
        else color = ChatColor.RED;
        return color + String.format("%.2f", tps);
    }

    private String mb(long bytes) {
        return String.format("%.0f MB", bytes / 1_048_576.0);
    }
}
