package org.bukkit.lazybukkit.core.vanilla;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.lazybukkit.vanilla.VanillaTest;
import org.bukkit.lazybukkit.vanilla.VanillaTestResult;
import org.bukkit.lazybukkit.vanilla.VanillaTestRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of VanillaTestRunner that discovers and executes
 * {@link VanillaTest}-annotated methods.
 */
public class CraftVanillaTestRunner implements VanillaTestRunner {

    private final Plugin plugin;

    /** Maps test name -> (object, method, annotation) */
    private final Map<String, TestEntry> tests = new LinkedHashMap<String, TestEntry>();

    public CraftVanillaTestRunner(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerTestSuite(Object testSuite) {
        for (Method method : testSuite.getClass().getMethods()) {
            VanillaTest annotation = method.getAnnotation(VanillaTest.class);
            if (annotation == null) continue;

            // Validate signature: must accept World, return VanillaTestResult
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !World.class.isAssignableFrom(params[0])) {
                plugin.getLogger().warning("Skipping @VanillaTest method "
                    + method.getName() + ": must accept a single World parameter");
                continue;
            }
            if (!VanillaTestResult.class.isAssignableFrom(method.getReturnType())) {
                plugin.getLogger().warning("Skipping @VanillaTest method "
                    + method.getName() + ": must return VanillaTestResult");
                continue;
            }

            tests.put(annotation.name(), new TestEntry(testSuite, method, annotation));
        }
    }

    @Override
    public void runAll(World world, VanillaTestCallback callback) {
        runTests(new ArrayList<TestEntry>(tests.values()), world, callback);
    }

    @Override
    public void runCategory(String category, World world, VanillaTestCallback callback) {
        List<TestEntry> filtered = new ArrayList<TestEntry>();
        for (TestEntry entry : tests.values()) {
            if (entry.annotation.category().equalsIgnoreCase(category)) {
                filtered.add(entry);
            }
        }
        if (filtered.isEmpty()) {
            callback.onComplete(new ArrayList<VanillaTestResult>());
            return;
        }
        runTests(filtered, world, callback);
    }

    @Override
    public List<String> getRegisteredTests() {
        return new ArrayList<String>(tests.keySet());
    }

    @Override
    public List<String> getRegisteredCategories() {
        List<String> categories = new ArrayList<String>();
        for (TestEntry entry : tests.values()) {
            String cat = entry.annotation.category();
            if (!categories.contains(cat)) {
                categories.add(cat);
            }
        }
        return categories;
    }

    /**
     * Runs tests sequentially on the main thread, one per tick,
     * to allow block updates to process between tests.
     */
    private void runTests(final List<TestEntry> toRun, final World world,
                          final VanillaTestCallback callback) {
        final List<VanillaTestResult> results = new ArrayList<VanillaTestResult>();
        final int[] index = {0};

        new BukkitRunnable() {
            @Override
            public void run() {
                if (index[0] >= toRun.size()) {
                    cancel();
                    callback.onComplete(results);
                    return;
                }

                TestEntry entry = toRun.get(index[0]);
                index[0]++;

                try {
                    VanillaTestResult result =
                        (VanillaTestResult) entry.method.invoke(entry.instance, world);
                    results.add(result);
                } catch (Exception e) {
                    results.add(VanillaTestResult.fail(
                        entry.annotation.name(),
                        entry.annotation.category(),
                        "Exception: " + e.getMessage(),
                        0L
                    ));
                    plugin.getLogger().warning("Test '" + entry.annotation.name()
                        + "' threw exception: " + e.getMessage());
                }
            }
        }.runTaskTimer(plugin, 1L, 5L); // 5 ticks between tests for block updates
    }

    private static class TestEntry {
        final Object instance;
        final Method method;
        final VanillaTest annotation;

        TestEntry(Object instance, Method method, VanillaTest annotation) {
            this.instance = instance;
            this.method = method;
            this.annotation = annotation;
        }
    }
}
