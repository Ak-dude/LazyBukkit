package org.bukkit.lazybukkit.core.command;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.lazybukkit.developer.BlockInspector;
import org.bukkit.lazybukkit.developer.EntityInspector;

import java.util.List;
import java.util.Map;

/**
 * /lazyinspect command — inspect blocks and entities.
 */
public class LazyInspectCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        String sub = args.length > 0 ? args[0].toLowerCase() : "block";

        switch (sub) {
            case "block":
                inspectBlock(player);
                return true;
            case "redstone":
                inspectRedstone(player);
                return true;
            case "entity":
                inspectEntity(player);
                return true;
            case "self":
                inspectSelf(player);
                return true;
            default:
                player.sendMessage(ChatColor.GOLD + "Usage: /lazyinspect [block|redstone|entity|self]");
                return true;
        }
    }

    private void inspectBlock(Player player) {
        Block block = player.getTargetBlock(null, 50);
        if (block == null || block.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No block in sight (look at a block within 50 blocks).");
            return;
        }
        player.sendMessage(ChatColor.GOLD + "=== Block Inspector ===");
        Map<String, Object> info = BlockInspector.inspect(block);
        for (Map.Entry<String, Object> entry : info.entrySet()) {
            player.sendMessage(ChatColor.YELLOW + "  " + entry.getKey()
                + ": " + ChatColor.WHITE + entry.getValue());
        }
    }

    private void inspectRedstone(Player player) {
        Block block = player.getTargetBlock(null, 50);
        if (block == null || block.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No block in sight.");
            return;
        }
        player.sendMessage(ChatColor.GOLD + "=== Redstone Inspector ===");
        Map<String, Object> info = BlockInspector.inspectRedstone(block);
        for (Map.Entry<String, Object> entry : info.entrySet()) {
            player.sendMessage(ChatColor.YELLOW + "  " + entry.getKey()
                + ": " + ChatColor.WHITE + entry.getValue());
        }
    }

    private void inspectEntity(Player player) {
        // Find the nearest entity within 5 blocks (excluding the player)
        List<Entity> nearby = player.getNearbyEntities(5, 5, 5);
        if (nearby.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No entities within 5 blocks.");
            return;
        }
        Entity closest = nearby.get(0);
        double closestDist = closest.getLocation().distanceSquared(player.getLocation());
        for (Entity e : nearby) {
            double dist = e.getLocation().distanceSquared(player.getLocation());
            if (dist < closestDist) {
                closest = e;
                closestDist = dist;
            }
        }
        player.sendMessage(ChatColor.GOLD + "=== Entity Inspector ===");
        Map<String, Object> info = EntityInspector.inspect(closest);
        for (Map.Entry<String, Object> entry : info.entrySet()) {
            player.sendMessage(ChatColor.YELLOW + "  " + entry.getKey()
                + ": " + ChatColor.WHITE + entry.getValue());
        }
    }

    private void inspectSelf(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Self Inspector ===");
        Map<String, Object> info = EntityInspector.inspect(player);
        for (Map.Entry<String, Object> entry : info.entrySet()) {
            player.sendMessage(ChatColor.YELLOW + "  " + entry.getKey()
                + ": " + ChatColor.WHITE + entry.getValue());
        }
    }
}
