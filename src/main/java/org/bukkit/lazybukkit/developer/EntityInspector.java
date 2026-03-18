package org.bukkit.lazybukkit.developer;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility for inspecting entity state in a human-readable format.
 */
public final class EntityInspector {

    private EntityInspector() {}

    /**
     * Returns a map of all observable properties for an entity.
     *
     * @param entity The entity to inspect
     * @return Ordered map of property name to value
     */
    public static Map<String, Object> inspect(Entity entity) {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("type", entity.getType().name());
        info.put("entityId", entity.getEntityId());
        info.put("uuid", entity.getUniqueId().toString());
        info.put("world", entity.getWorld().getName());
        info.put("x", entity.getLocation().getX());
        info.put("y", entity.getLocation().getY());
        info.put("z", entity.getLocation().getZ());
        info.put("yaw", entity.getLocation().getYaw());
        info.put("pitch", entity.getLocation().getPitch());
        info.put("velocity", entity.getVelocity().toString());
        info.put("isOnGround", entity.isOnGround());
        info.put("ticksLived", entity.getTicksLived());
        info.put("fireTicks", entity.getFireTicks());
        info.put("passenger", entity.getPassenger() != null
            ? entity.getPassenger().getType().name() : "none");
        info.put("vehicle", entity.getVehicle() != null
            ? entity.getVehicle().getType().name() : "none");

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            info.put("health", living.getHealth());
            info.put("maxHealth", living.getMaxHealth());
            info.put("remainingAir", living.getRemainingAir());
            info.put("maximumAir", living.getMaximumAir());
            info.put("canPickupItems", living.getCanPickupItems());
            info.put("noDamageTicks", living.getNoDamageTicks());
            info.put("lastDamage", living.getLastDamage());
            info.put("eyeHeight", living.getEyeHeight());
        }

        if (entity instanceof Player) {
            Player player = (Player) entity;
            info.put("name", player.getName());
            info.put("displayName", player.getDisplayName());
            info.put("gameMode", player.getGameMode().name());
            info.put("level", player.getLevel());
            info.put("exp", player.getExp());
            info.put("totalExperience", player.getTotalExperience());
            info.put("foodLevel", player.getFoodLevel());
            info.put("saturation", player.getSaturation());
            info.put("exhaustion", player.getExhaustion());
            info.put("isFlying", player.isFlying());
            info.put("isSneaking", player.isSneaking());
            info.put("isSprinting", player.isSprinting());
            info.put("ping", player.getAddress() != null ? "connected" : "unknown");
        }

        return info;
    }

    /**
     * Formats an inspection result as a human-readable string.
     *
     * @param info Map from {@link #inspect}
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
