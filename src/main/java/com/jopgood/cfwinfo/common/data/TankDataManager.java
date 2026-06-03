package com.jopgood.cfwinfo.common.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Set;

public class TankDataManager {

    /** Namespace of Create: Stuff 'N Additions. */
    private static final String CSA_NAMESPACE = "create_sa";

    /**
     * Create: Stuff 'N Additions item ids that can hold fuel. Identity detection recognises a tank
     * even when empty, since CS&A only writes the "tagFuel"/"tagWater" NBT once a tank has content.
     * Update if CS&A adds or renames fuel-holding items.
     */
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

    /**
     * Create: Stuff 'N Additions item ids that can hold water. See {@link #FUEL_CAPABLE_ITEMS}.
     */
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

    /**
     * Standalone CS&A tanks that store water in {@code tagStock} (not {@code tagWater}). Their
     * capacity is a configurable world variable, so we never hardcode it — see {@link #stockFraction}.
     */
    private static final Set<String> WATER_STOCK_TANKS = Set.of(
            "small_filling_tank", "medium_filling_tank", "large_filling_tank");

    /** Standalone CS&A tanks that store fuel in {@code tagStock}. See {@link #WATER_STOCK_TANKS}. */
    private static final Set<String> FUEL_STOCK_TANKS = Set.of(
            "small_fueling_tank", "medium_fueling_tank", "large_fueling_tank");

    /** Infinite creative water tank: it holds no {@code tagStock} and always reads as full. */
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

    private static double readTag(ItemStack stack, String key) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble(key);
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    /**
     * Fill fraction (0..1) of a CS&A standalone tank. Read from the item's own capacity-aware bar
     * ({@code getBarWidth}/13) so we honour CS&A's configurable tank capacities rather than guessing
     * a maximum. Client-only (the bar reads the local player), which is fine for the HUD overlay.
     */
    private static double stockFraction(ItemStack stack) {
        return clamp01(stack.getItem().getBarWidth(stack) / (double) ITEM_BAR_MAX);
    }

    /** True if the stack is a known CS&A item whose registry path is in {@code knownItems}. */
    private static boolean isKnownCsaItem(ItemStack itemStack, Set<String> knownItems) {
        if (itemStack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        return CSA_NAMESPACE.equals(id.getNamespace()) && knownItems.contains(id.getPath());
    }

    public static boolean canItemStoreFuel(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        // Tag presence covers items that currently hold fuel; identity covers empty tanks.
        return customData.copyTag().contains("tagFuel") || isKnownCsaItem(itemStack, FUEL_CAPABLE_ITEMS);
    }

    public static boolean canItemStoreWater(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        // Tag presence covers items that currently hold water; identity covers empty tanks.
        return customData.copyTag().contains("tagWater") || isKnownCsaItem(itemStack, WATER_CAPABLE_ITEMS);
    }

    public static boolean isHoldingFuelCapableItem(Player player) {
        ItemStack tool = player.getMainHandItem();
        return canItemStoreFuel(tool);
    }

    public static boolean isHoldingWaterCapableItem(Player player) {
        ItemStack tool = player.getMainHandItem();
        return canItemStoreWater(tool);
    }

    public static boolean isWearingFuelCapableItem(Player player) {
        ItemStack chestplate = player.getInventory().getArmor(2);
        return canItemStoreFuel(chestplate);
    }

    public static boolean isWearingWaterCapableItem(Player player) {
        ItemStack chestplate = player.getInventory().getArmor(2);
        return canItemStoreWater(chestplate);
    }

    /** Raw stored fuel for {@code itemStack}: {@code tagStock} for fueling tanks, else {@code tagFuel}. */
    public static double getFuelLevel(ItemStack itemStack) {
        String path = csaPath(itemStack);
        boolean stockTank = path != null && FUEL_STOCK_TANKS.contains(path);
        return readTag(itemStack, stockTank ? "tagStock" : "tagFuel");
    }

    /** Raw stored water for {@code itemStack}: {@code tagStock} for filling tanks, else {@code tagWater}. */
    public static double getWaterLevel(ItemStack itemStack) {
        String path = csaPath(itemStack);
        boolean stockTank = path != null && WATER_STOCK_TANKS.contains(path);
        return readTag(itemStack, stockTank ? "tagStock" : "tagWater");
    }

    /** Fuel fill fraction (0..1) for the sprite bar. Tank items scale by their (configurable) capacity. */
    public static double getFuelFraction(ItemStack itemStack) {
        if (itemStack.isEmpty()) return 0.0;
        String path = csaPath(itemStack);
        if (path != null && FUEL_STOCK_TANKS.contains(path)) return stockFraction(itemStack);
        return clamp01(readTag(itemStack, "tagFuel") / TAG_LEVEL_CAPACITY);
    }

    /** Water fill fraction (0..1) for the sprite bar. Tank items scale by their (configurable) capacity. */
    public static double getWaterFraction(ItemStack itemStack) {
        if (itemStack.isEmpty()) return 0.0;
        String path = csaPath(itemStack);
        if (CREATIVE_FILLING_TANK.equals(path)) return 1.0; // infinite tank: always full
        if (path != null && WATER_STOCK_TANKS.contains(path)) return stockFraction(itemStack);
        return clamp01(readTag(itemStack, "tagWater") / TAG_LEVEL_CAPACITY);
    }

}
