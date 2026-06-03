package com.jopgood.cfwinfo.client.gui;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.jopgood.cfwinfo.common.config.CommonConfig.OverlayPosition;
import com.jopgood.cfwinfo.common.data.TankDataManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The "simplified" overlay: tank sprites showing fuel and water levels instead of text.
 *
 * <p>Layout is the same for every position — chest tank left, tool tank right, item icons below,
 * no edge padding. Placement is decided entirely by the resolved anchor (see {@link OverlayAnchor}),
 * which uses the full composite size so corners do not clip. A single layout lets the HUD and the
 * editor preview share one render path.
 */
public class TankSpriteOverlay implements LayeredDraw.Layer {

    private static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath("cfwinfo", "textures/gui/sprites/tank_sprite_sheet.png");
    private static final int FRAME_WIDTH = 15;
    private static final int FRAME_HEIGHT = 32;
    private static final int FRAMES_PER_ROW = 18;
    private static final int TOTAL_FRAMES = 18;
    private static final int SPRITE_SHEET_WIDTH = 270;
    private static final int SPRITE_SHEET_HEIGHT = 96;

    /** Item icon size (in unscaled sprite units) drawn beneath the tank. */
    private static final int ICON_SIZE = 16;
    /** Vertical extent of the whole composite (tank + item row) in unscaled sprite units. */
    private static final int COMPOSITE_UNITS_HIGH = FRAME_HEIGHT + ICON_SIZE; // 48


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
        boolean wearingTank = TankGuiHelper.canDisplayTankInfo();

        // Only render in simplified mode, and only if enabled
        if (!renderEnabled || !simpleEnabled) {
            return;
        }

        // In simplified mode, goggles are not required (but can be worn)
        if (!wearingTank) {
            return; // No tank, nothing to show
        }

        OverlayPosition position = CommonConfig.getOverlayPosition();
        int[] anchor = OverlayAnchor.resolve(position, graphics.guiWidth(), graphics.guiHeight(),
                compositeWidthPx(player), compositeHeightPx());

        renderCompositeAt(graphics, anchor[0], anchor[1], player);
    }

    /** Depth at which item icons are drawn on the HUD, so they sit above other HUD elements. */
    private static final int HUD_ITEM_Z = 450;

    /**
     * Renders the tank composite with its top-left at GUI coordinates {@code (gx, gy)} for the HUD.
     */
    public void renderCompositeAt(GuiGraphics graphics, int gx, int gy, Player player) {
        renderCompositeAt(graphics, gx, gy, player, HUD_ITEM_Z);
    }

    /**
     * Renders the tank composite with its top-left at GUI coordinates {@code (gx, gy)}.
     * Public so the editor can preview at an arbitrary anchor using the same render path as the HUD.
     *
     * @param itemZ depth for the item icons; the editor passes a low value so the whole preview
     *              stays behind the editor controls (the HUD uses {@link #HUD_ITEM_Z}).
     */
    public void renderCompositeAt(GuiGraphics graphics, int gx, int gy, Player player, int itemZ) {
        float scaleFactor = (float) CommonConfig.getSpriteScaleFactor();
        ItemStack tankItem = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack toolItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Set up rendering
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SPRITE);

        // Apply opacity from config
        float opacity = CommonConfig.getOverlayOpacity() / 100.0f;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);

        // Use pose stack scaling for proper sprite scaling. We position in unscaled units
        // (gx / scaleFactor) so that, after the scale, the sprite lands at gx in GUI pixels.
        graphics.pose().pushPose();
        graphics.pose().scale(scaleFactor, scaleFactor, 1.0f);
        int scaledX = (int) (gx / scaleFactor);
        int scaledY = (int) (gy / scaleFactor);

        // Only show a tank for a slot that actually holds one.
        boolean chestIsTank = TankDataManager.isWearingFuelCapableItem(player) || TankDataManager.isWearingWaterCapableItem(player);
        boolean toolIsTank = TankDataManager.isHoldingFuelCapableItem(player) || TankDataManager.isHoldingWaterCapableItem(player);

        if (chestIsTank && toolIsTank) {
            renderDualTankDisplay(graphics, scaledX, scaledY, tankItem, toolItem, itemZ);
        } else {
            // render() guarantees at least one tank; show whichever slot has it.
            ItemStack subject = chestIsTank ? tankItem : toolItem;
            renderSingleTankDisplay(graphics, scaledX, scaledY, subject, itemZ);
        }

        graphics.pose().popPose();

        // Reset shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderDualTankDisplay(GuiGraphics graphics, int scaledX, int scaledY,
                                      ItemStack tankItem, ItemStack toolItem, int itemZ) {
        int tankU = 0;
        int tankV = 2 * FRAME_HEIGHT; // tank outline row

        int chestFuelU = frameU(TankDataManager.getFuelFraction(tankItem));
        int chestWaterU = frameU(TankDataManager.getWaterFraction(tankItem));
        int toolFuelU = frameU(TankDataManager.getFuelFraction(toolItem));
        int toolWaterU = frameU(TankDataManager.getWaterFraction(toolItem));

        int tank1X = scaledX;                 // Chest tank (left)
        int tank2X = scaledX + FRAME_WIDTH;   // Tool tank (right)
        int itemY = scaledY + FRAME_HEIGHT;   // Items directly below

        renderTankLayers(graphics, tank1X, scaledY, tankU, tankV, chestFuelU, 0, chestWaterU, FRAME_HEIGHT);
        renderTankLayers(graphics, tank2X, scaledY, tankU, tankV, toolFuelU, 0, toolWaterU, FRAME_HEIGHT);

        GuiGameElement.of(tankItem).at(tank1X, itemY, itemZ).render(graphics);
        GuiGameElement.of(toolItem).at(tank2X, itemY, itemZ).render(graphics);
    }

    private void renderSingleTankDisplay(GuiGraphics graphics, int scaledX, int scaledY, ItemStack tankItem, int itemZ) {
        int tankU = 0;
        int tankV = 2 * FRAME_HEIGHT; // tank outline row
        int fuelU = frameU(TankDataManager.getFuelFraction(tankItem));
        int waterU = frameU(TankDataManager.getWaterFraction(tankItem));

        renderTankLayers(graphics, scaledX, scaledY, tankU, tankV, fuelU, 0, waterU, FRAME_HEIGHT);

        // Item icon directly below the tank. A held non-tank item is intentionally not drawn here.
        int itemY = scaledY + FRAME_HEIGHT;
        GuiGameElement.of(tankItem).at(scaledX, itemY, itemZ).render(graphics);
    }

    private void renderTankLayers(GuiGraphics graphics, int scaledX, int scaledY,
                                 int tankU, int tankV, int fuelU, int fuelV, int waterU, int waterV) {
        // Tank outline first (background layer)
        graphics.blit(SPRITE, scaledX, scaledY, tankU, tankV, FRAME_WIDTH, FRAME_HEIGHT,
                SPRITE_SHEET_WIDTH, SPRITE_SHEET_HEIGHT);

        // Fuel level (middle layer)
        graphics.blit(SPRITE, scaledX, scaledY, fuelU, fuelV, FRAME_WIDTH, FRAME_HEIGHT,
                SPRITE_SHEET_WIDTH, SPRITE_SHEET_HEIGHT);

        // Water level (top layer)
        graphics.blit(SPRITE, scaledX, scaledY, waterU, waterV, FRAME_WIDTH, FRAME_HEIGHT,
                SPRITE_SHEET_WIDTH, SPRITE_SHEET_HEIGHT);
    }

    /** Maps a fill fraction (0..1) to its column U offset in the sprite sheet (frame 0 = full). */
    private static int frameU(double fraction) {
        fraction = Math.max(0.0, Math.min(1.0, fraction));
        int frameIndex = (int) Math.round((1.0 - fraction) * (TOTAL_FRAMES - 1));
        frameIndex = Math.max(0, Math.min(TOTAL_FRAMES - 1, frameIndex));
        return (frameIndex % FRAMES_PER_ROW) * FRAME_WIDTH;
    }

    // ----- Helpers for the overlay editor (bounding box for hit-testing / clamping) -----

    /** True when both a worn tank and a held tank are present (dual layout). */
    public static boolean isDualLayout(Player player) {
        boolean chestIsTank = TankDataManager.isWearingFuelCapableItem(player) || TankDataManager.isWearingWaterCapableItem(player);
        boolean toolIsTank = TankDataManager.isHoldingFuelCapableItem(player) || TankDataManager.isHoldingWaterCapableItem(player);
        return chestIsTank && toolIsTank;
    }

    /** Rendered width of the composite in GUI pixels, given the current scale and layout. */
    public static int compositeWidthPx(Player player) {
        float scaleFactor = (float) CommonConfig.getSpriteScaleFactor();
        int frames = isDualLayout(player) ? 2 : 1;
        return (int) (frames * FRAME_WIDTH * scaleFactor);
    }

    /** Rendered height of the composite (tank + item row) in GUI pixels. */
    public static int compositeHeightPx() {
        float scaleFactor = (float) CommonConfig.getSpriteScaleFactor();
        return (int) (COMPOSITE_UNITS_HIGH * scaleFactor);
    }

    /**
     * The current top-left anchor for the configured position, in GUI pixels. Used by the editor to
     * seed the drag position so opening it shows the overlay exactly where it already renders.
     */
    public static int[] currentAnchor(Player player, int guiW, int guiH) {
        return OverlayAnchor.resolve(CommonConfig.getOverlayPosition(), guiW, guiH,
                compositeWidthPx(player), compositeHeightPx());
    }
}
