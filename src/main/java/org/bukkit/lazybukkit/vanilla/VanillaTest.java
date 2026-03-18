package org.bukkit.lazybukkit.vanilla;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a vanilla mechanics verification test.
 * <p>
 * Annotated methods must have the signature:
 * <pre>
 * {@literal @}VanillaTest(name = "...", category = "redstone")
 * public VanillaTestResult testName(org.bukkit.World world)
 * </pre>
 * <p>
 * Tests are discovered and executed by the {@link VanillaTestRunner}.
 * They are designed to run in-game to verify that server modifications
 * have not broken vanilla Minecraft mechanics.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VanillaTest {
    /**
     * @return Human-readable test name
     */
    String name();

    /**
     * @return Test category (e.g., "redstone", "piston", "physics", "mob")
     */
    String category() default "general";

    /**
     * @return Description of what this test verifies
     */
    String description() default "";

    /**
     * @return Number of ticks to wait for the test to complete.
     *         Some tests (e.g., repeater delays) need multiple ticks.
     */
    int timeoutTicks() default 100;
}
