package com.jopgood.cfwinfo.client.gui;

import java.util.List;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.jopgood.cfwinfo.common.data.TankDataManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.catnip.gui.element.GuiGameElement;
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

/**
 * Renders detailed text-based tank information overlay
 * This is the "full" view that requires goggles and shows detailed tooltip
 */
public class TankTooltipOverlay implements IGuiOverlay {

    private static int hoverTicks = 0;
    private static boolean wasRenderEnabled = false;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
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
        int width = screenWidth;
        int height = screenHeight;
        float partialTicks = partialTick;

        renderDetailedTooltip(graphics, partialTicks, width, height, player);
    }

    // Tooltip box padding around the text (matches the renderTooltip call in drawBox).
    private static final int PAD_TOP = 6;
    private static final int PAD_BOTTOM = 6;
    private static final int PAD_LEFT = 3;
    private static final int PAD_RIGHT = 12;

    private void renderDetailedTooltip(GuiGraphics graphics, float partialTicks, int width, int height, Player player) {
        CClient cfg = AllConfigs.client();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        hoverTicks++;

        List<Component> tooltip = TankGuiHelper.generateTooltipWithSpacing(player);
        if (tooltip.isEmpty()) {
            poseStack.popPose();
            return;
        }

        // Anchor the box through the shared resolver so a position chosen in the editor matches here.
        int[] anchor = OverlayAnchor.resolve(CommonConfig.getOverlayPosition(), width, height,
                boxWidth(tooltip), boxHeight(tooltip));

        // Fade-in: slide in horizontally and ramp alpha.
        float fadeSpeed = 24.0f;
        float fade = Mth.clamp((hoverTicks + partialTicks) / fadeSpeed, 0, 1);
        if (fade < 1) {
            poseStack.translate(
                    Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 8, 0, 0);
        }

        drawBox(graphics, tooltip, anchor[0], anchor[1], fade);

        poseStack.popPose();
    }

    /**
     * Draws the tooltip box (background, border, text, goggles icon) with its top-left at the given
     * box anchor, scaled by {@code fade} (1 = fully opaque). Shared by the live HUD and the editor.
     */
    private void drawBox(GuiGraphics graphics, List<Component> tooltip, int boxX, int boxY, float fade) {
        CClient cfg = AllConfigs.client();

        Boolean useCustom = cfg.overlayCustomColor.get();
        Color tooltipBackground = new Color(0xf0100010, true);
        Color colorBackground = useCustom ? new Color(cfg.overlayBackgroundColor.get())
                : tooltipBackground.scaleAlpha(85.0f);
        Color colorBorderTop = useCustom ? new Color(cfg.overlayBorderColorTop.get())
                : new Color(0x505000ff, true);
        Color colorBorderBot = useCustom ? new Color(cfg.overlayBorderColorBot.get())
                : new Color(0x5028007f, true);

        float alpha = (CommonConfig.getOverlayOpacity() / 100.0f) * fade;
        colorBackground.scaleAlpha(alpha);
        colorBorderTop.scaleAlpha(alpha);
        colorBorderBot.scaleAlpha(alpha);

        // The box extends PAD_LEFT/PAD_TOP beyond the text origin, so offset the text inward.
        int posX = boxX + PAD_LEFT;
        int posY = boxY + PAD_TOP;

        PositionedTooltipRenderer.renderTooltip(
                graphics, tooltip, posX, posY,
                colorBackground.getRGB(), colorBorderTop.getRGB(), colorBorderBot.getRGB(),
                PAD_TOP, PAD_BOTTOM, PAD_LEFT, PAD_RIGHT);

        ItemStack item = AllItems.GOGGLES.asStack();
        GuiGameElement.of(item)
                .at(posX - 2, posY - 4, 450) // Small offset from tooltip start
                .render(graphics);
    }

    /**
     * Renders a static (full-opacity) tooltip preview with its top-left at {@code (boxX, boxY)}.
     * Used by the overlay editor so the detailed view can be positioned with a live preview.
     */
    public void renderPreviewAt(GuiGraphics graphics, int boxX, int boxY, Player player) {
        List<Component> tooltip = TankGuiHelper.generateTooltipWithSpacing(player);
        if (tooltip.isEmpty()) {
            return;
        }
        drawBox(graphics, tooltip, boxX, boxY, 1.0f);
    }

    private static int textWidth(List<Component> tooltip) {
        Minecraft mc = Minecraft.getInstance();
        int w = 0;
        for (FormattedText line : tooltip) {
            w = Math.max(w, mc.font.width(line));
        }
        return w;
    }

    private static int textHeight(List<Component> tooltip) {
        int h = 8;
        if (tooltip.size() > 1) {
            h += 2; // gap between title lines and next lines
            h += (tooltip.size() - 1) * 10;
        }
        return h;
    }

    private static int boxWidth(List<Component> tooltip) {
        return textWidth(tooltip) + PAD_LEFT + PAD_RIGHT;
    }

    private static int boxHeight(List<Component> tooltip) {
        return textHeight(tooltip) + PAD_TOP + PAD_BOTTOM;
    }

    /** Rendered width of the tooltip box in GUI pixels (for the editor's hit-test / clamping). */
    public static int tooltipWidthPx(Player player) {
        return boxWidth(TankGuiHelper.generateTooltipWithSpacing(player));
    }

    /** Rendered height of the tooltip box in GUI pixels. */
    public static int tooltipHeightPx(Player player) {
        return boxHeight(TankGuiHelper.generateTooltipWithSpacing(player));
    }

    /** Top-left box anchor for the configured position, so the editor opens where the HUD renders. */
    public static int[] currentAnchor(Player player, int guiW, int guiH) {
        List<Component> tooltip = TankGuiHelper.generateTooltipWithSpacing(player);
        return OverlayAnchor.resolve(CommonConfig.getOverlayPosition(), guiW, guiH,
                boxWidth(tooltip), boxHeight(tooltip));
    }
}