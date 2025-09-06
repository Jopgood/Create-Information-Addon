package com.jopgood.cfwinfo.client.gui;

import java.util.List;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.jopgood.cfwinfo.common.data.TankDataManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.createmod.catnip.theme.Color;
import net.createmod.catnip.gui.element.GuiGameElement;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CClient;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Renders detailed text-based tank information overlay
 * This is the "full" view that requires goggles and shows detailed tooltip
 */
public class TankTooltipOverlay implements LayeredDraw.Layer {

    private static int hoverTicks = 0;
    private static boolean wasRenderEnabled = false;

    @Override
    public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            return;
        }

        // Check if this overlay should be active
        boolean renderEnabled = CommonConfig.isInfoEnabled();
        boolean simpleEnabled = CommonConfig.isSimplifiedEnabled();
        boolean wearingGoggles = GogglesItem.isWearingGoggles(player);
        boolean wearingTank = TankGuiHelper.canDisplayTankInfo();

        // Only render in full mode (non-simplified) and requires goggles
        if (!renderEnabled || simpleEnabled || !wearingGoggles) {
            wasRenderEnabled = false;
            return;
        }

        // Check if we have tank to display info for
        if (!wearingTank) {
            wasRenderEnabled = false;
            return;
        }

        // Check if the overlay has just been re-enabled
        if (!wasRenderEnabled) {
            hoverTicks = 0; // Reset hoverTicks for the fade-in effect
        }
        wasRenderEnabled = true;

        // Get screen dimensions and timing
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);

        renderDetailedTooltip(graphics, partialTicks, width, height, player);
    }

    private void renderDetailedTooltip(GuiGraphics graphics, float partialTicks, int width, int height, Player player) {
        CClient cfg = AllConfigs.client();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        hoverTicks++;

        // Generate tooltip content
        List<Component> tooltip = TankGuiHelper.generateTooltipWithSpacing(player);

        Minecraft mc = Minecraft.getInstance();

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

        // Colors with config integration
        Boolean useCustom = cfg.overlayCustomColor.get();
        Color tooltipBackground = new Color(0xf0100010, true);
        Color colorBackground = useCustom ? new Color(cfg.overlayBackgroundColor.get())
                : tooltipBackground.scaleAlpha(.75f);
        Color colorBorderTop = useCustom ? new Color(cfg.overlayBorderColorTop.get())
                : new Color(0x505000ff, true);
        Color colorBorderBot = useCustom ? new Color(cfg.overlayBorderColorBot.get())
                : new Color(0x5028007f, true);

        // Apply opacity from our config
        float opacityMultiplier = CommonConfig.getOverlayOpacity() / 100.0f;
        colorBackground.scaleAlpha(opacityMultiplier);
        colorBorderTop.scaleAlpha(opacityMultiplier);
        colorBorderBot.scaleAlpha(opacityMultiplier);

        // Position calculation based on config - properly sized for tooltips
        int padding = 20;
        int posX, posY;
        switch (CommonConfig.getOverlayPosition()) {
            case TOP_LEFT:
                posX = padding;
                posY = padding;
                break;
            case TOP_RIGHT:
                posX = width - tooltipTextWidth - padding;
                posY = padding;
                break;
            case BOTTOM_LEFT:
                posX = padding;
                posY = height - tooltipHeight - padding;
                break;
            case BOTTOM_RIGHT:
                posX = width - tooltipTextWidth - padding;
                posY = height - tooltipHeight - padding;
                break;
            case CENTER:
            default:
                // Actually center the tooltip
                posX = (width - tooltipTextWidth) / 2;
                posY = (height - tooltipHeight) / 2;

                // Ensure it stays on screen
                posX = Math.max(padding, Math.min(posX, width - tooltipTextWidth - padding));
                posY = Math.max(padding, Math.min(posY, height - tooltipHeight - padding));
                break;
        }

        // Fade Effect using config fade speed
        float fadeSpeed = (float) (24.0);
        float fade = Mth.clamp((hoverTicks + partialTicks) / fadeSpeed, 0, 1);
        if (fade < 1) {
            poseStack.translate(
                    Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 8,
                    0,
                    0
            );
            colorBackground.scaleAlpha(fade);
            colorBorderTop.scaleAlpha(fade);
            colorBorderBot.scaleAlpha(fade);
        }

        // Render tooltip background using Create's styling
        PositionedTooltipRenderer.renderTooltip(graphics, tooltip, posX, posY,
                colorBackground.getRGB(), colorBorderTop.getRGB(), colorBorderBot.getRGB());

        // Render goggles icon
        ItemStack item = AllItems.GOGGLES.asStack();
        GuiGameElement.of(item)
                .at(posX - 2, posY - 4, 450)  // Small offset from tooltip start
                .render(graphics);

        poseStack.popPose();
    }
}