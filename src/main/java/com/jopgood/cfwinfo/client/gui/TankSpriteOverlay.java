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
 * Renders visual tank sprites showing fuel and water levels
 * This is the "simplified" view that shows tank graphics instead of text
 *
 * <p>The overlay uses a single canonical layout for every position (chest tank on the left, tool
 * tank on the right, item icons directly below, no edge padding). Corner/preset positioning is
 * handled purely by the resolved anchor (see {@link OverlayAnchor}), using the full composite size
 * so nothing clips. Keeping one layout means the live HUD and the editor preview render identically,
 * which is what keeps drag-positioning WYSIWYG for presets as well as custom positions.
 */
public class TankSpriteOverlay implements LayeredDraw.Layer {

    private static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath("cfwinfo", "textures/gui/sprites/tank_sprite_sheet.png");
    private static final int FRAME_WIDTH = 15;
    private static final int FRAME_HEIGHT = 32;
    private static final int FRAMES_PER_ROW = 18;
    private static final int TOTAL_FRAMES = 18;
    private static final int SPRITE_SHEET_WIDTH = 270;
    private static final int SPRITE_SHEET_HEIGHT = 96;
    private static final int MAX_LEVEL = 1600;

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
        int[] anchor = OverlayAnchor.resolveSprite(position, graphics.guiWidth(), graphics.guiHeight(),
                compositeWidthPx(player), compositeHeightPx());

        renderCompositeAt(graphics, anchor[0], anchor[1], player);
    }

    /**
     * Renders the tank composite with its top-left at GUI coordinates {@code (gx, gy)}.
     *
     * <p>Public so the overlay editor can draw an identical preview at an arbitrary anchor — this is
     * what guarantees the editor is WYSIWYG: the HUD and the editor run the exact same rendering, in
     * the exact same (single) layout.
     */
    public void renderCompositeAt(GuiGraphics graphics, int gx, int gy, Player player) {
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

        // Decide layout based on which slots actually hold a tank, so we never render a phantom
        // tank for an empty/non-tank slot.
        boolean chestIsTank = TankDataManager.isWearingFuelCapableItem(player) || TankDataManager.isWearingWaterCapableItem(player);
        boolean toolIsTank = TankDataManager.isHoldingFuelCapableItem(player) || TankDataManager.isHoldingWaterCapableItem(player);

        if (chestIsTank && toolIsTank) {
            renderDualTankDisplay(graphics, scaledX, scaledY, tankItem, toolItem);
        } else {
            // Exactly one tank present (the render() gate guarantees at least one). Show whichever
            // slot is the tank; the held item is only drawn when it is itself a tank.
            ItemStack subject = chestIsTank ? tankItem : toolItem;
            renderSingleTankDisplay(graphics, scaledX, scaledY, subject);
        }

        graphics.pose().popPose();

        // Reset shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderDualTankDisplay(GuiGraphics graphics, int scaledX, int scaledY,
                                      ItemStack tankItem, ItemStack toolItem) {
        int tankU = 0;
        int tankV = 2 * FRAME_HEIGHT; // tank outline row

        int chestFuelU = frameU(TankDataManager.getFuelLevel(tankItem));
        int chestWaterU = frameU(TankDataManager.getWaterLevel(tankItem));
        int toolFuelU = frameU(TankDataManager.getFuelLevel(toolItem));
        int toolWaterU = frameU(TankDataManager.getWaterLevel(toolItem));

        int tank1X = scaledX;                 // Chest tank (left)
        int tank2X = scaledX + FRAME_WIDTH;   // Tool tank (right)
        int itemY = scaledY + FRAME_HEIGHT;   // Items directly below

        renderTankLayers(graphics, tank1X, scaledY, tankU, tankV, chestFuelU, 0, chestWaterU, FRAME_HEIGHT);
        renderTankLayers(graphics, tank2X, scaledY, tankU, tankV, toolFuelU, 0, toolWaterU, FRAME_HEIGHT);

        GuiGameElement.of(tankItem).at(tank1X, itemY, 450).render(graphics);
        GuiGameElement.of(toolItem).at(tank2X, itemY, 450).render(graphics);
    }

    private void renderSingleTankDisplay(GuiGraphics graphics, int scaledX, int scaledY, ItemStack tankItem) {
        int tankU = 0;
        int tankV = 2 * FRAME_HEIGHT; // tank outline row
        int fuelU = frameU(TankDataManager.getFuelLevel(tankItem));
        int waterU = frameU(TankDataManager.getWaterLevel(tankItem));

        renderTankLayers(graphics, scaledX, scaledY, tankU, tankV, fuelU, 0, waterU, FRAME_HEIGHT);

        // Item icon directly below. Only the tank item's icon is drawn — a non-tank held item must
        // not appear (tank-capable held items use the dual layout instead).
        int itemY = scaledY + FRAME_HEIGHT;
        GuiGameElement.of(tankItem).at(scaledX, itemY, 450).render(graphics);
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

    /** Maps a fuel/water level to its column U offset in the sprite sheet. */
    private static int frameU(double level) {
        int frameIndex = ((MAX_LEVEL - (int) Math.round(level)) * (TOTAL_FRAMES - 1)) / MAX_LEVEL;
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
        return OverlayAnchor.resolveSprite(CommonConfig.getOverlayPosition(), guiW, guiH,
                compositeWidthPx(player), compositeHeightPx());
    }
}
