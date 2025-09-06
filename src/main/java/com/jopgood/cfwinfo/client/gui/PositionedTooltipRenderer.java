package com.jopgood.cfwinfo.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Custom tooltip renderer that respects absolute screen positioning
 * instead of mouse-relative positioning like vanilla tooltips
 */
public class PositionedTooltipRenderer {

    public static void renderTooltip(GuiGraphics graphics, List<Component> tooltip,
                                     int x, int y, int backgroundColor, int borderTop, int borderBot) {
        if (tooltip.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        // Calculate tooltip dimensions
        int tooltipTextWidth = 0;
        for (FormattedText textLine : tooltip) {
            int textLineWidth = mc.font.width(textLine);
            if (textLineWidth > tooltipTextWidth) {
                tooltipTextWidth = textLineWidth;
            }
        }

        int tooltipHeight = 8;
        if (tooltip.size() > 1) {
            tooltipHeight += 2; // gap between title lines and next lines
            tooltipHeight += (tooltip.size() - 1) * 10;
        }

        // Tooltip background padding
        int padding = 3;
        int bgX1 = x - padding;
        int bgY1 = y - 4;
        int bgX2 = x + tooltipTextWidth + padding;
        int bgY2 = y + tooltipHeight + 3;

        int zLevel = 400; // Standard tooltip z-level

        // Render background
        graphics.fillGradient(bgX1, bgY1, bgX2, bgY1 + 1, zLevel, backgroundColor, backgroundColor);
        graphics.fillGradient(bgX1, bgY2 - 1, bgX2, bgY2, zLevel, backgroundColor, backgroundColor);
        graphics.fillGradient(bgX1, bgY1 + 1, bgX2, bgY2 - 1, zLevel, backgroundColor, backgroundColor);

        // Render borders
        graphics.fillGradient(bgX1 - 1, bgY1 + 1, bgX1, bgY2 - 1, zLevel, backgroundColor, backgroundColor);
        graphics.fillGradient(bgX2, bgY1 + 1, bgX2 + 1, bgY2 - 1, zLevel, backgroundColor, backgroundColor);
        graphics.fillGradient(bgX1, bgY1, bgX2, bgY1 + 1, zLevel, borderTop, borderTop);
        graphics.fillGradient(bgX1, bgY2 - 1, bgX2, bgY2, zLevel, borderBot, borderBot);

        // Render left and right borders with gradient
        graphics.fillGradient(bgX1 - 1, bgY1 + 1, bgX1, bgY2 - 1, zLevel, borderTop, borderBot);
        graphics.fillGradient(bgX2, bgY1 + 1, bgX2 + 1, bgY2 - 1, zLevel, borderTop, borderBot);

        // Render text
        poseStack.translate(0.0D, 0.0D, zLevel);
        int textY = y;
        boolean isFirstLine = true;

        for (Component line : tooltip) {
            if (isFirstLine) {
                // Title line might need special handling
                graphics.drawString(mc.font, line, x, textY, 0xFFFFFFFF, false);
                isFirstLine = false;
                textY += 12; // Extra space after title
            } else {
                graphics.drawString(mc.font, line, x, textY, 0xFFFFFFFF, false);
                textY += 10;
            }
        }

        poseStack.popPose();
    }
}