package org.bukkit.lazybukkit.developer;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility for inspecting block state in a human-readable format.
 * Useful for debugging redstone circuits, piston contraptions,
 * and block physics issues.
 */
public final class BlockInspector {

    private static final BlockFace[] CARDINAL = {
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
        BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    };

    private BlockInspector() {}

    /**
     * Returns a map of all observable properties for a block.
     *
     * @param block The block to inspect
     * @return Ordered map of property name to value
     */
    public static Map<String, Object> inspect(Block block) {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("type", block.getType().name());
        info.put("typeId", block.getTypeId());
        info.put("data", block.getData());
        info.put("x", block.getX());
        info.put("y", block.getY());
        info.put("z", block.getZ());
        info.put("world", block.getWorld().getName());
        info.put("chunk", block.getChunk().getX() + "," + block.getChunk().getZ());
        info.put("lightLevel", block.getLightLevel());
        info.put("lightFromSky", block.getLightFromSky());
        info.put("lightFromBlocks", block.getLightFromBlocks());
        info.put("biome", block.getBiome().name());
        info.put("temperature", block.getTemperature());
        info.put("humidity", block.getHumidity());
        info.put("isBlockPowered", block.isBlockPowered());
        info.put("isBlockIndirectlyPowered", block.isBlockIndirectlyPowered());
        info.put("blockPower", block.getBlockPower());
        info.put("pistonReaction", block.getPistonMoveReaction().name());
        info.put("isLiquid", block.isLiquid());
        info.put("isEmpty", block.isEmpty());
        return info;
    }

    /**
     * Returns redstone-specific information for a block and its neighbors.
     *
     * @param block The block to inspect for redstone properties
     * @return Ordered map of redstone property name to value
     */
    public static Map<String, Object> inspectRedstone(Block block) {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("type", block.getType().name());
        info.put("blockPower", block.getBlockPower());
        info.put("isBlockPowered", block.isBlockPowered());
        info.put("isBlockIndirectlyPowered", block.isBlockIndirectlyPowered());

        // Power from each face
        for (BlockFace face : CARDINAL) {
            int power = block.getBlockPower(face);
            if (power > 0) {
                info.put("powerFrom_" + face.name(), power);
            }
        }

        // Neighbor types (useful for understanding redstone context)
        for (BlockFace face : CARDINAL) {
            Block neighbor = block.getRelative(face);
            if (neighbor.getType() != org.bukkit.Material.AIR) {
                info.put("neighbor_" + face.name(),
                    neighbor.getType().name() + " (power=" + neighbor.getBlockPower() + ")");
            }
        }

        return info;
    }

    /**
     * Formats an inspection result as a human-readable string.
     *
     * @param info Map from {@link #inspect} or {@link #inspectRedstone}
     * @return Formatted multi-line string
     */
    public static String format(Map<String, Object> info) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : info.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
