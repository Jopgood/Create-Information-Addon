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
     * Create: Stuff 'N Additions items that can hold fuel. Detection by item identity lets us
     * recognise a tank even when it is empty — CS&A only writes the "tagFuel"/"tagWater" NBT keys
     * when there is content, so a freshly crafted or fully drained tank would otherwise look like a
     * non-tank item. Derived from which item/procedure classes reference "tagFuel" in CS&A.
     * Update this set if CS&A adds or renames fuel-holding items.
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
     * Create: Stuff 'N Additions items that can hold water. See {@link #FUEL_CAPABLE_ITEMS} for why
     * identity detection is used. Derived from which item/procedure classes reference "tagWater".
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

    public static double getFuelLevel(ItemStack itemStack) {
        int tagFuel = 0;
        tagFuel = (int) itemStack
                .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getDouble("tagFuel");
        return tagFuel;
    }

    public static double getWaterLevel(ItemStack itemStack) {
        int tagFuel = 0;
        tagFuel = (int) itemStack
                .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getDouble("tagWater");
        return tagFuel;
    }

}
