package com.jopgood.cfwinfo.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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

        if (canDisplayTankInfo()) {
            ItemStack jetpackItem = player.getItemBySlot(EquipmentSlot.CHEST);
            double fuelLevel = TankDataManager.getFuelLevel(jetpackItem);
            double waterLevel = TankDataManager.getWaterLevel(jetpackItem);

            tooltip.add(Component.literal("Tank Monitor"));
            if (TankDataManager.isWearingWaterCapableItem(player)) {
                tooltip.add(Component.literal("Water: " + formatLevel(waterLevel)));
            }
            if (TankDataManager.isWearingFuelCapableItem(player)) {
                tooltip.add(Component.literal("Fuel: " + formatLevel(fuelLevel)));
            }
        } else {
            tooltip.add(Component.literal("No Tank Equipment"));
        }

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
     * Formats level values for display
     * @param level The level value to format
     * @return Formatted string representation
     */
    private static String formatLevel(double level) {
        // Format to 1 decimal place, or whole number if it's a round value
        if (level == (int) level) {
            return String.valueOf((int) level);
        } else {
            return String.format("%.1f", level);
        }
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

        return TankDataManager.isWearingFuelCapableItem(player) || TankDataManager.isWearingWaterCapableItem(player);
    }
}