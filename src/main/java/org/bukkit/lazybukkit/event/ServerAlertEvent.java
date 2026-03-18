package org.bukkit.lazybukkit.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a server alert condition is triggered (e.g., TPS drop,
 * memory critical, player count threshold).
 * <p>
 * Cancelling this event prevents the default alert action (console log,
 * op broadcast) but does not suppress the underlying condition.
 */
public class ServerAlertEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final AlertLevel level;
    private final String category;
    private final String message;
    private final double value;
    private final double threshold;
    private boolean cancelled;

    public ServerAlertEvent(AlertLevel level, String category,
                            String message, double value, double threshold) {
        this.level = level;
        this.category = category;
        this.message = message;
        this.value = value;
        this.threshold = threshold;
    }

    /**
     * Severity levels for server alerts.
     */
    public enum AlertLevel {
        INFO,
        WARNING,
        CRITICAL
    }

    public AlertLevel getLevel() {
        return level;
    }

    /**
     * @return Alert category (e.g., "tps", "memory", "players")
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return Human-readable alert message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return The actual value that triggered the alert
     */
    public double getValue() {
        return value;
    }

    /**
     * @return The threshold that was exceeded
     */
    public double getThreshold() {
        return threshold;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
