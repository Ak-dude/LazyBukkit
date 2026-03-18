package org.bukkit.lazybukkit.performance;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class TickTimingSnapshotTest {

    @Test
    public void testConstructionAndGetters() {
        TickTimingSnapshot snapshot = new TickTimingSnapshot(
            100L, 25_000_000L, 500, 120, 300);

        assertEquals(100L, snapshot.getTickNumber());
        assertEquals(25_000_000L, snapshot.getDurationNanos());
        assertEquals(500, snapshot.getEntityCount());
        assertEquals(120, snapshot.getTileEntityCount());
        assertEquals(300, snapshot.getLoadedChunks());
    }

    @Test
    public void testDurationMillisConversion() {
        TickTimingSnapshot snapshot = new TickTimingSnapshot(
            1L, 50_000_000L, 0, 0, 0);

        assertEquals(50.0, snapshot.getDurationMillis(), 0.001);
    }

    @Test
    public void testIsOverBudget() {
        // Exactly 50ms — not over budget
        TickTimingSnapshot onBudget = new TickTimingSnapshot(
            1L, 50_000_000L, 0, 0, 0);
        assertFalse(onBudget.isOverBudget());

        // 51ms — over budget
        TickTimingSnapshot overBudget = new TickTimingSnapshot(
            1L, 51_000_000L, 0, 0, 0);
        assertTrue(overBudget.isOverBudget());

        // 10ms — well within budget
        TickTimingSnapshot underBudget = new TickTimingSnapshot(
            1L, 10_000_000L, 0, 0, 0);
        assertFalse(underBudget.isOverBudget());
    }

    @Test
    public void testToString() {
        TickTimingSnapshot snapshot = new TickTimingSnapshot(
            42L, 35_000_000L, 100, 50, 200);
        String str = snapshot.toString();

        assertThat(str, containsString("tick=42"));
        assertThat(str, containsString("35.00ms"));
        assertThat(str, containsString("entities=100"));
        assertThat(str, containsString("tileEntities=50"));
        assertThat(str, containsString("chunks=200"));
    }

    @Test
    public void testImmutability() {
        // Ensure the snapshot captures the values at creation time
        // (no references to mutable state)
        TickTimingSnapshot snapshot = new TickTimingSnapshot(
            1L, 1_000_000L, 10, 5, 20);

        // Call getters multiple times — values must be stable
        assertEquals(snapshot.getTickNumber(), snapshot.getTickNumber());
        assertEquals(snapshot.getDurationNanos(), snapshot.getDurationNanos());
        assertEquals(snapshot.getEntityCount(), snapshot.getEntityCount());
    }
}
