package org.bukkit.lazybukkit.vanilla;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class VanillaTestResultTest {

    @Test
    public void testPassResult() {
        VanillaTestResult result = VanillaTestResult.pass(
            "Signal Test", "redstone", "All signals correct", 42L);

        assertTrue(result.isPassed());
        assertEquals("Signal Test", result.getTestName());
        assertEquals("redstone", result.getCategory());
        assertEquals("All signals correct", result.getMessage());
        assertEquals(42L, result.getDurationMillis());
    }

    @Test
    public void testFailResult() {
        VanillaTestResult result = VanillaTestResult.fail(
            "Piston Test", "piston", "Push limit exceeded", 100L);

        assertFalse(result.isPassed());
        assertEquals("Piston Test", result.getTestName());
        assertEquals("piston", result.getCategory());
        assertEquals("Push limit exceeded", result.getMessage());
        assertEquals(100L, result.getDurationMillis());
    }

    @Test
    public void testToStringPass() {
        VanillaTestResult result = VanillaTestResult.pass(
            "Test1", "redstone", "OK", 10L);
        String str = result.toString();

        assertThat(str, containsString("PASS"));
        assertThat(str, containsString("redstone"));
        assertThat(str, containsString("Test1"));
    }

    @Test
    public void testToStringFail() {
        VanillaTestResult result = VanillaTestResult.fail(
            "Test2", "physics", "Gravity broken", 5L);
        String str = result.toString();

        assertThat(str, containsString("FAIL"));
        assertThat(str, containsString("physics"));
        assertThat(str, containsString("Gravity broken"));
    }
}
