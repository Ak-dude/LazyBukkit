package org.bukkit.lazybukkit.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.lazybukkit.core.command.LazyTestCommand;
import org.bukkit.lazybukkit.core.command.LazyInspectCommand;
import org.bukkit.lazybukkit.core.command.LazyPerfCommand;
import org.bukkit.lazybukkit.core.performance.CraftPerformanceMonitor;
import org.bukkit.lazybukkit.core.vanilla.CraftVanillaTestRunner;
import org.bukkit.lazybukkit.performance.PerformanceMonitor;
import org.bukkit.lazybukkit.vanilla.RedstoneTestSuite;
import org.bukkit.lazybukkit.vanilla.VanillaTestRunner;

/**
 * Core plugin that bootstraps all LazyBukkit services.
 * This plugin ships with the server and provides implementations
 * for all LazyBukkit API interfaces.
 */
public class LazyBukkitPlugin extends JavaPlugin {

    private CraftPerformanceMonitor performanceMonitor;
    private CraftVanillaTestRunner testRunner;

    @Override
    public void onEnable() {
        // --- Performance Monitor ---
        performanceMonitor = new CraftPerformanceMonitor(this);
        Bukkit.getServicesManager().register(
            PerformanceMonitor.class,
            performanceMonitor,
            this,
            ServicePriority.Normal
        );
        getLogger().info("Performance monitor started.");

        // --- Vanilla Test Runner ---
        testRunner = new CraftVanillaTestRunner(this);
        testRunner.registerTestSuite(new RedstoneTestSuite());
        Bukkit.getServicesManager().register(
            VanillaTestRunner.class,
            testRunner,
            this,
            ServicePriority.Normal
        );
        getLogger().info("Vanilla test runner ready ("
            + testRunner.getRegisteredTests().size() + " tests in "
            + testRunner.getRegisteredCategories().size() + " categories).");

        // --- Commands ---
        getCommand("lazytest").setExecutor(new LazyTestCommand(testRunner));
        getCommand("lazyinspect").setExecutor(new LazyInspectCommand());
        getCommand("lazyperf").setExecutor(new LazyPerfCommand(performanceMonitor));

        getLogger().info("LazyBukkit Core v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (performanceMonitor != null) {
            performanceMonitor.shutdown();
        }
        Bukkit.getServicesManager().unregisterAll(this);
        getLogger().info("LazyBukkit Core disabled.");
    }
}
