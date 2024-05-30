package com.jopgood.cfwinfo.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;

public class JetpackDataManager {
    
    private static final List<ResourceLocation> JETPACK_ITEMS = Arrays.asList(
        new ResourceLocation("create_sa:brass_jetpack_chestplate"),
        new ResourceLocation("create_sa:copper_jetpack_chestplate"),
        new ResourceLocation("create_sa:andesite_jetpack_chestplate")
    );

    public static boolean isWearingJetpack(Player player) {
        ItemStack chestplate = player.getInventory().getArmor(2); // 2 is the index for the chestplate slot
        if (chestplate.isEmpty()) {
            return false;
        }
        
        Item chestplateItem = chestplate.getItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(chestplateItem);
        
        return JETPACK_ITEMS.contains(itemId);
    }

    // Methods to retrieve fuel and water levels
    public static double getFuelLevel(ItemStack itemstack) {
        if (itemstack.hasTag() && itemstack.getTag().contains("tagFuel")) {
            return itemstack.getTag().getDouble("tagFuel");
        }
        return 0.0;
    }

    public static double getWaterLevel(ItemStack itemstack) {
        if (itemstack.hasTag() && itemstack.getTag().contains("tagWater")) {
            return itemstack.getTag().getDouble("tagWater");
        }
        return 0.0;
    }

    public static boolean hasFuelLevel(ItemStack itemstack) {
        return itemstack.hasTag() && itemstack.getTag().contains("tagFuel");
    }

    public static boolean hasWaterLevel(ItemStack itemstack) {
        return itemstack.hasTag() && itemstack.getTag().contains("tagWater");
    }
}

