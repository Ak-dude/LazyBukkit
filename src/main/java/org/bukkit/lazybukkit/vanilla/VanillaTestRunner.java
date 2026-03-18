package org.bukkit.lazybukkit.vanilla;

import java.util.List;

/**
 * Discovers and executes {@link VanillaTest}-annotated methods to verify
 * that vanilla Minecraft mechanics are intact.
 * <p>
 * Obtain an instance via the {@link org.bukkit.plugin.ServicesManager}.
 * <p>
 * Usage from a command handler:
 * <pre>
 * VanillaTestRunner runner = Bukkit.getServicesManager()
 *     .getRegistration(VanillaTestRunner.class).getProvider();
 *
 * // Run all redstone tests
 * runner.runCategory("redstone", world, new VanillaTestCallback() {
 *     public void onComplete(List&lt;VanillaTestResult&gt; results) {
 *         for (VanillaTestResult r : results) {
 *             sender.sendMessage(r.toString());
 *         }
 *     }
 * });
 * </pre>
 */
public interface VanillaTestRunner {

    /**
     * Register a test suite class. All methods annotated with
     * {@link VanillaTest} will be discovered.
     *
     * @param testSuite Object containing annotated test methods
     */
    void registerTestSuite(Object testSuite);

    /**
     * Run all registered tests.
     *
     * @param world    The world to run tests in
     * @param callback Called when all tests complete
     */
    void runAll(org.bukkit.World world, VanillaTestCallback callback);

    /**
     * Run all tests in a specific category.
     *
     * @param category Category name (e.g., "redstone", "piston", "physics")
     * @param world    The world to run tests in
     * @param callback Called when category tests complete
     */
    void runCategory(String category, org.bukkit.World world,
                     VanillaTestCallback callback);

    /**
     * @return List of all registered test names
     */
    List<String> getRegisteredTests();

    /**
     * @return List of all registered categories
     */
    List<String> getRegisteredCategories();

    /**
     * Callback for async test completion.
     */
    interface VanillaTestCallback {
        void onComplete(List<VanillaTestResult> results);
    }
}
