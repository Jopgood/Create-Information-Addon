package com.jopgood.cfwinfo.common.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Reads tank contents from Create: Stuff 'N Additions items.
 *
 * <p>1.20.1 note: this version reads legacy NBT via {@link ItemStack#getTag()} rather than 1.20.5+
 * Data Components. CS&A's keys are the same across versions: jetpacks/exoskeletons store
 * {@code tagFuel}/{@code tagWater}; standalone filling/fueling tanks store {@code tagStock}.
 */
public class TankDataManager {

    /** Namespace of Create: Stuff 'N Additions. */
    private static final String CSA_NAMESPACE = "create_sa";

    private static final Set<String> FUEL_CAPABLE_ITEMS = Set.of(
            "andesite_jetpack_chestplate",
            "brass_jetpack_chestplate",
            "netherite_jetpack_chestplate",
            "andesite_exoskeleton_chestplate",
            "brass_exoskeleton_chestplate",
            "portable_drill",
            "small_fueling_tank",
            "medium_fueling_tank",
            "large_fueling_tank",
            "creative_filling_tank",
            "flamethrower",
            "grapplin_whisk"
    );

    private static final Set<String> WATER_CAPABLE_ITEMS = Set.of(
            "brass_jetpack_chestplate",
            "copper_jetpack_chestplate",
            "netherite_jetpack_chestplate",
            "brass_exoskeleton_chestplate",
            "copper_exoskeleton_chestplate",
            "portable_drill",
            "small_filling_tank",
            "medium_filling_tank",
            "large_filling_tank",
            "creative_filling_tank",
            "block_picker"
    );

    /** Standalone CS&A tanks that store water in {@code tagStock} (capacity is a configurable variable). */
    private static final Set<String> WATER_STOCK_TANKS = Set.of(
            "small_filling_tank", "medium_filling_tank", "large_filling_tank");

    /** Standalone CS&A tanks that store fuel in {@code tagStock}. */
    private static final Set<String> FUEL_STOCK_TANKS = Set.of(
            "small_fueling_tank", "medium_fueling_tank", "large_fueling_tank");

    /** Infinite creative water tank: holds no {@code tagStock} and always reads as full. */
    private static final String CREATIVE_FILLING_TANK = "creative_filling_tank";

    /** Capacity of the jetpack/exoskeleton {@code tagFuel}/{@code tagWater} bars. */
    private static final double TAG_LEVEL_CAPACITY = 1600.0;

    /** Max width of a vanilla item durability bar; CS&A scales {@code tagStock} to this. */
    private static final int ITEM_BAR_MAX = 13;

    /** Registry path if {@code stack} is a CS&A item, otherwise {@code null}. */
    private static String csaPath(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return CSA_NAMESPACE.equals(id.getNamespace()) ? id.getPath() : null;
    }

    /** Reads a numeric NBT tag from the stack, or 0 if the tag/key is absent. */
    private static double readTag(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getDouble(key) : 0.0;
    }

    private static boolean hasTag(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key);
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    /**
     * Fill fraction (0..1) of a CS&A standalone tank, read from its own capacity-aware item bar
     * ({@code getBarWidth}/13) so we honour CS&A's configurable tank capacities. Client-only.
     */
    private static double stockFraction(ItemStack stack) {
        return clamp01(stack.getItem().getBarWidth(stack) / (double) ITEM_BAR_MAX);
    }

    private static boolean isKnownCsaItem(ItemStack itemStack, Set<String> knownItems) {
        String path = csaPath(itemStack);
        return path != null && knownItems.contains(path);
    }

    public static boolean canItemStoreFuel(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        return hasTag(itemStack, "tagFuel") || isKnownCsaItem(itemStack, FUEL_CAPABLE_ITEMS);
    }

    public static boolean canItemStoreWater(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        return hasTag(itemStack, "tagWater") || isKnownCsaItem(itemStack, WATER_CAPABLE_ITEMS);
    }

    public static boolean isHoldingFuelCapableItem(Player player) {
        return canItemStoreFuel(player.getMainHandItem());
    }

    public static boolean isHoldingWaterCapableItem(Player player) {
        return canItemStoreWater(player.getMainHandItem());
    }

    public static boolean isWearingFuelCapableItem(Player player) {
        return canItemStoreFuel(player.getInventory().getArmor(2));
    }

    public static boolean isWearingWaterCapableItem(Player player) {
        return canItemStoreWater(player.getInventory().getArmor(2));
    }

    /** Raw stored fuel: {@code tagStock} for fueling tanks, else {@code tagFuel}. */
    public static double getFuelLevel(ItemStack itemStack) {
        String path = csaPath(itemStack);
        boolean stockTank = path != null && FUEL_STOCK_TANKS.contains(path);
        return readTag(itemStack, stockTank ? "tagStock" : "tagFuel");
    }

    /** Raw stored water: {@code tagStock} for filling tanks, else {@code tagWater}. */
    public static double getWaterLevel(ItemStack itemStack) {
        String path = csaPath(itemStack);
        boolean stockTank = path != null && WATER_STOCK_TANKS.contains(path);
        return readTag(itemStack, stockTank ? "tagStock" : "tagWater");
    }

    /** Fuel fill fraction (0..1) for the sprite bar. */
    public static double getFuelFraction(ItemStack itemStack) {
        if (itemStack.isEmpty()) return 0.0;
        String path = csaPath(itemStack);
        if (path != null && FUEL_STOCK_TANKS.contains(path)) return stockFraction(itemStack);
        return clamp01(readTag(itemStack, "tagFuel") / TAG_LEVEL_CAPACITY);
    }

    /** Water fill fraction (0..1) for the sprite bar. */
    public static double getWaterFraction(ItemStack itemStack) {
        if (itemStack.isEmpty()) return 0.0;
        String path = csaPath(itemStack);
        if (CREATIVE_FILLING_TANK.equals(path)) return 1.0; // infinite tank: always full
        if (path != null && WATER_STOCK_TANKS.contains(path)) return stockFraction(itemStack);
        return clamp01(readTag(itemStack, "tagWater") / TAG_LEVEL_CAPACITY);
    }
}
