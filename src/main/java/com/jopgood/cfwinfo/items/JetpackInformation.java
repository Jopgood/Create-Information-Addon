package com.jopgood.cfwinfo.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import com.jopgood.cfwinfo.data.JetpackDataManager;

public class JetpackInformation {
	    
    public static List<Component> updateTooltipWithSpacing(Player player) {
        List<Component> newTooltip = new ArrayList<>();
        
        if (JetpackDataManager.isWearingJetpack(player)) {
        	        	
            ItemStack jetpackItem = player.getItemBySlot(EquipmentSlot.CHEST);
            double fuelLevel = JetpackDataManager.getFuelLevel(jetpackItem);
            double waterLevel = JetpackDataManager.getWaterLevel(jetpackItem);

            newTooltip.add(Component.literal("Jetpack Information"));
            newTooltip.add(Component.literal("Water: " + waterLevel));
            newTooltip.add(Component.literal("Fuel: " + fuelLevel));

            
        } else {
            newTooltip.add(Component.literal("No Jetpack Equipped"));
        }
        
        return newTooltip;
    }
}

