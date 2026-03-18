package org.bukkit.lazybukkit.event;

import org.bukkit.lazybukkit.performance.TickTimingSnapshot;
import org.junit.Test;

import static org.junit.Assert.*;

public class ServerTickEventTest {

    @Test
    public void testConstruction() {
        TickTimingSnapshot snapshot = new TickTimingSnapshot(
            100L, 30_000_000L, 50, 10, 100);
        ServerTickEvent event = new ServerTickEvent(100L, 30_000_000L, snapshot);

        assertEquals(100L, event.getTickNumber());
        assertEquals(30_000_000L, event.getTickDurationNanos());
        assertSame(snapshot, event.getSnapshot());
    }

    @Test
    public void testTickDurationMillis() {
        ServerTickEvent event = new ServerTickEvent(1L, 45_500_000L, null);
        assertEquals(45.5, event.getTickDurationMillis(), 0.001);
    }

    @Test
    public void testIsLagging() {
        // 30ms — not lagging
        ServerTickEvent normal = new ServerTickEvent(1L, 30_000_000L, null);
        assertFalse(normal.isLagging());

        // 50ms — exactly on budget, not lagging
        ServerTickEvent border = new ServerTickEvent(1L, 50_000_000L, null);
        assertFalse(border.isLagging());

        // 80ms — lagging
        ServerTickEvent lagging = new ServerTickEvent(1L, 80_000_000L, null);
        assertTrue(lagging.isLagging());
    }

    @Test
    public void testNullSnapshot() {
        ServerTickEvent event = new ServerTickEvent(1L, 10_000_000L, null);
        assertNull(event.getSnapshot());
    }

    @Test
    public void testHandlerList() {
        // HandlerList must be static and non-null
        assertNotNull(ServerTickEvent.getHandlerList());

        ServerTickEvent event = new ServerTickEvent(1L, 10_000_000L, null);
        assertSame(ServerTickEvent.getHandlerList(), event.getHandlers());
    }
}
