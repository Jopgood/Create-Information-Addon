package com.jopgood.cfwinfo.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import com.jopgood.cfwinfo.common.data.TankDataManager;

/**
 * Helper class for generating GUI components related to tank information.
 * Handles tooltip generation and formatting for overlay displays.
 */
public class TankGuiHelper {

    /**
     * Generates tooltip components for tank information display
     * @param player The player to check for tank information
     * @return List of components to display in the overlay
     */
    public static List<Component> generateTankTooltip(Player player) {
        List<Component> tooltip = new ArrayList<>();
        
        tooltip.add(canDisplayTankInfo() ? 
            Component.literal("Tank Monitor:").withStyle(ChatFormatting.WHITE) :
            Component.literal("No Tank Equipment").withStyle(ChatFormatting.GRAY));
            
        if (!canDisplayTankInfo()) {
            return tooltip;
        }

        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack toolItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        String chestLabel = chestItem.isEmpty() ? "On Chest" : chestItem.getHoverName().getString();
        addItemTooltip(tooltip, chestLabel, chestItem,
            TankDataManager.isWearingWaterCapableItem(player),
            TankDataManager.isWearingFuelCapableItem(player), player);
            
        addItemTooltip(tooltip, toolItem.isEmpty() ? "In-Hand" : toolItem.getHoverName().getString(), player.getItemInHand(InteractionHand.MAIN_HAND),
            TankDataManager.isHoldingWaterCapableItem(player),
            TankDataManager.isHoldingFuelCapableItem(player), player);

        return tooltip;
    }

    /**
     * Generates tooltip with spacing for better readability
     * @param player The player to check for tank information
     * @return List of components with spacing applied
     */
    public static List<Component> generateTooltipWithSpacing(Player player) {
        List<Component> baseTooltip = generateTankTooltip(player);
        List<Component> spacedTooltip = new ArrayList<>();

        Component spacing = Component.literal("    ");

        for (Component component : baseTooltip) {
            spacedTooltip.add(spacing.plainCopy().append(component));
        }

        return spacedTooltip;
    }

    /**
     * Adds item tooltip information if the item has tank capabilities
     * @param tooltip The tooltip list to add to
     * @param label The label for this item section
     * @param item The item to check
     * @param hasWater Whether the item has water capability
     * @param hasFuel Whether the item has fuel capability
     * @param player The player (needed for compatibility with existing methods)
     */
    private static void addItemTooltip(List<Component> tooltip, String label, ItemStack item, 
                                      boolean hasWater, boolean hasFuel, Player player) {
        if (item == null || (!hasWater && !hasFuel)) {
            return;
        }
        
        tooltip.add(Component.literal(label).withStyle(ChatFormatting.WHITE));

        if (hasWater) {
            Component waterText = Component.literal("  Water: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(formatLevel(TankDataManager.getWaterLevel(item)))
                            .withStyle(ChatFormatting.AQUA));
            tooltip.add(waterText);
        }
        
        if (hasFuel) {
            Component fuelText = Component.literal("  Fuel: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(formatLevel(TankDataManager.getFuelLevel(item)))
                            .withStyle(ChatFormatting.GOLD));
            tooltip.add(fuelText);
        }
    }

    /**
     * Formats level values for display
     * @param level The level value to format
     * @return Formatted string representation
     */
    private static String formatLevel(double level) {
        return level == (int) level ? 
            String.valueOf((int) level) : 
            String.format("%.1f", level);
    }

    /**
     * Checks if tank information is available for the current player
     * @return true if tank information can be displayed
     */
    public static boolean canDisplayTankInfo() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            return false;
        }

        return TankDataManager.isWearingFuelCapableItem(player) ||
                TankDataManager.isWearingWaterCapableItem(player) ||
                TankDataManager.isHoldingWaterCapableItem(player) ||
                TankDataManager.isHoldingFuelCapableItem(player);
    }
}