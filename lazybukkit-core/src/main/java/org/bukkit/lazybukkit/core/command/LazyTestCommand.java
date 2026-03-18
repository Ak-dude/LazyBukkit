package org.bukkit.lazybukkit.core.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.lazybukkit.vanilla.VanillaTestResult;
import org.bukkit.lazybukkit.vanilla.VanillaTestRunner;

import java.util.List;

/**
 * /lazytest command — runs vanilla mechanics verification tests.
 */
public class LazyTestCommand implements CommandExecutor {

    private final VanillaTestRunner runner;

    public LazyTestCommand(VanillaTestRunner runner) {
        this.runner = runner;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("list")) {
            listTests(sender);
            return true;
        }

        if (sub.equals("all")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Tests must be run by a player (needs a world).");
                return true;
            }
            runAll(sender, (Player) sender);
            return true;
        }

        // Treat argument as a category name
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Tests must be run by a player (needs a world).");
            return true;
        }
        runCategory(sender, (Player) sender, sub);
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== LazyBukkit Vanilla Test Runner ===");
        sender.sendMessage(ChatColor.WHITE + "  /lazytest list            - List all tests and categories");
        sender.sendMessage(ChatColor.WHITE + "  /lazytest all             - Run all tests");
        sender.sendMessage(ChatColor.WHITE + "  /lazytest <category>      - Run tests in a category");
        sender.sendMessage(ChatColor.GRAY + "  Categories: "
            + String.join(", ", runner.getRegisteredCategories()));
    }

    private void listTests(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Registered Tests ===");
        for (String category : runner.getRegisteredCategories()) {
            sender.sendMessage(ChatColor.YELLOW + "  [" + category + "]");
        }
        sender.sendMessage(ChatColor.WHITE + "  Total: "
            + runner.getRegisteredTests().size() + " tests in "
            + runner.getRegisteredCategories().size() + " categories");
    }

    private void runAll(final CommandSender sender, Player player) {
        sender.sendMessage(ChatColor.GOLD + "Running all vanilla tests...");
        runner.runAll(player.getWorld(), new VanillaTestRunner.VanillaTestCallback() {
            @Override
            public void onComplete(List<VanillaTestResult> results) {
                reportResults(sender, results);
            }
        });
    }

    private void runCategory(final CommandSender sender, Player player, String category) {
        if (!runner.getRegisteredCategories().contains(category)) {
            sender.sendMessage(ChatColor.RED + "Unknown category: " + category);
            sender.sendMessage(ChatColor.GRAY + "Available: "
                + String.join(", ", runner.getRegisteredCategories()));
            return;
        }
        sender.sendMessage(ChatColor.GOLD + "Running [" + category + "] tests...");
        runner.runCategory(category, player.getWorld(),
            new VanillaTestRunner.VanillaTestCallback() {
                @Override
                public void onComplete(List<VanillaTestResult> results) {
                    reportResults(sender, results);
                }
            });
    }

    private void reportResults(CommandSender sender, List<VanillaTestResult> results) {
        sender.sendMessage(ChatColor.GOLD + "=== Test Results ===");
        int passed = 0;
        int failed = 0;
        for (VanillaTestResult result : results) {
            if (result.isPassed()) {
                passed++;
                sender.sendMessage(ChatColor.GREEN + "  PASS " + ChatColor.WHITE
                    + result.getTestName() + ChatColor.GRAY
                    + " (" + result.getDurationMillis() + "ms)");
            } else {
                failed++;
                sender.sendMessage(ChatColor.RED + "  FAIL " + ChatColor.WHITE
                    + result.getTestName() + ChatColor.GRAY
                    + " - " + result.getMessage());
            }
        }
        sender.sendMessage(ChatColor.GOLD + "---");
        ChatColor summaryColor = failed == 0 ? ChatColor.GREEN : ChatColor.RED;
        sender.sendMessage(summaryColor + "  " + passed + " passed, " + failed + " failed"
            + " out of " + results.size() + " tests");
    }
}
