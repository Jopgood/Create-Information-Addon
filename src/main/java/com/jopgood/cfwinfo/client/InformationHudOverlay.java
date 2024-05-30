package com.jopgood.cfwinfo.client;

import java.util.ArrayList;
import java.util.List;

import com.jopgood.cfwinfo.config.ClientConfig;
import com.jopgood.cfwinfo.items.JetpackInformation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class InformationHudOverlay {

    public static final IGuiOverlay INFO_OVERLAY = InformationHudOverlay::renderOverlay;
    private static int hoverTicks = 0;
    private static List<Component> tooltip = new ArrayList<>();
    private static final String spacing = "    ";
    private static final Component componentSpacing = Component.literal(spacing);
    
    // Add a variable to track the previous state of the overlay
    private static boolean wasRenderEnabled = false;

    public static void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
    	
    	Boolean renderEnabled = ClientConfig.INFORMATION_ENABLED.get();
    	Minecraft mc = Minecraft.getInstance();
    	
    	boolean wearingGoggles = GogglesItem.isWearingGoggles(mc.player);
    	
    	if (!renderEnabled || !wearingGoggles) { // Check if rendering is disabled
            wasRenderEnabled = false; // Update the previous state
            return; // If disabled, don't render anything
        }

        // Check if the overlay has just been re-enabled
        if (renderEnabled && !wasRenderEnabled) {
            hoverTicks = 0; // Reset hoverTicks for the fade-in effect
        }
        
        wasRenderEnabled = renderEnabled; // Update the previous state
    	
        CClient cfg = AllConfigs.client();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        hoverTicks++;

        // Retrieve jetpack information
        Player player = mc.player;
        if (player != null) {
        	setTooltip(JetpackInformation.updateTooltipWithSpacing(player), true);
        }
        
        // Colors
        Boolean useCustom = cfg.overlayCustomColor.get();
        Color colorBackground = useCustom ? new Color(cfg.overlayBackgroundColor.get())
            : Theme.c(Theme.Key.VANILLA_TOOLTIP_BACKGROUND).scaleAlpha(.75f);
        Color colorBorderTop = useCustom ? new Color(cfg.overlayBorderColorTop.get())
            : Theme.c(Theme.Key.VANILLA_TOOLTIP_BORDER, true).copy();
        Color colorBorderBot = useCustom ? new Color(cfg.overlayBorderColorBot.get())
            : Theme.c(Theme.Key.VANILLA_TOOLTIP_BORDER, false).copy();

        // Tooltip Dimensions
        int tooltipTextWidth = 0;
        for (FormattedText textLine : tooltip) {
            int textLineWidth = mc.font.width(textLine);
            if (textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }
        int tooltipHeight = 8;
        if (tooltip.size() > 1) {
            tooltipHeight += 2; // gap between title lines and next lines
            tooltipHeight += (tooltip.size() - 1) * 10;
        }
        
        // Position Calculation
        int posX = width / 2 - 240;
        int posY = height / 2 - 100;
        posX = Math.min(posX, width - tooltipTextWidth - 20);
        posY = Math.min(posY, height - tooltipHeight - 20);

        // Fade Effect
        float fade = Mth.clamp((hoverTicks + partialTicks) / 24f, 0, 1);
        if (fade < 1) {
            poseStack.translate(Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 8, 0, 0);
            colorBackground.scaleAlpha(fade);
            colorBorderTop.scaleAlpha(fade);
            colorBorderBot.scaleAlpha(fade);
        }

        // Render Tooltip Background
        RemovedGuiUtils.drawHoveringText(graphics, tooltip, posX, posY, width, height, -1, colorBackground.getRGB(),
            colorBorderTop.getRGB(), colorBorderBot.getRGB(), mc.font);

        // Render Icon in Tooltip
        ItemStack item = AllItems.GOGGLES.asStack();
        GuiGameElement.of(item)
        	.at(posX + 10, posY - 16, 450)
        	.render(graphics);

        poseStack.popPose();
    }

    // Method to set tooltip content with optional spacing
    public static void setTooltip(List<Component> tooltipText, boolean addSpacing) {
        tooltip.clear();
        for (Component component : tooltipText) {
            if (addSpacing) {
                tooltip.add(componentSpacing.plainCopy().append(component)); // Add spacing before each line
            } else {
                tooltip.add(component); // Add without spacing
            }
        }
    }
}
