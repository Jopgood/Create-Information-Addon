package com.jopgood.cfwinfo.common.data;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class TankDataManager {

    public static boolean canItemStoreFuel(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().contains("tagFuel");
    }

    public static boolean canItemStoreWater(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().contains("tagWater");
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
