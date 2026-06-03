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

    /** Item icon size (in unscaled sprite units) drawn beneath/above the tank. */
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

        float scaleFactor = (float) CommonConfig.getSpriteScaleFactor();
        int renderWidth = (int) (FRAME_WIDTH * scaleFactor);
        int renderHeight = (int) (FRAME_HEIGHT * scaleFactor);

        OverlayPosition position = CommonConfig.getOverlayPosition();
        int[] anchor = OverlayAnchor.resolveSprite(position, graphics.guiWidth(), graphics.guiHeight(),
                renderWidth, renderHeight);

        renderCompositeAt(graphics, anchor[0], anchor[1], player, position);
    }

    /**
     * Renders the tank composite with its top-left tank sprite at GUI coordinates {@code (gx, gy)}.
     *
     * <p>Public so the overlay editor can draw an identical preview at an arbitrary anchor — this is
     * what guarantees the editor is WYSIWYG: the HUD and the editor run the exact same rendering.
     */
    public void renderCompositeAt(GuiGraphics graphics, int gx, int gy, Player player, OverlayPosition position) {
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
            // Both a worn tank and a held tank: render two tank sprites with position-aware layout
            renderDualTankDisplay(graphics, scaledX, scaledY, tankItem, toolItem, position);
        } else {
            // Exactly one tank present (the render() gate guarantees at least one). Show whichever
            // slot is the tank; the held item is only drawn when it is itself a tank.
            ItemStack subject = chestIsTank ? tankItem : toolItem;
            renderSingleTankDisplay(graphics, scaledX, scaledY, subject, position);
        }

        graphics.pose().popPose();

        // Reset shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderDualTankDisplay(GuiGraphics graphics, int scaledX, int scaledY,
                                      ItemStack tankItem, ItemStack toolItem,
                                      OverlayPosition position) {
        int tankU = 0;
        int tankV = 2 * FRAME_HEIGHT; // tank outline row

        int chestFuelU = frameU(TankDataManager.getFuelLevel(tankItem));
        int chestWaterU = frameU(TankDataManager.getWaterLevel(tankItem));
        int toolFuelU = frameU(TankDataManager.getFuelLevel(toolItem));
        int toolWaterU = frameU(TankDataManager.getWaterLevel(toolItem));

        // Determine tank and item positioning based on overlay position
        int tank1X, tank2X, item1X, item2X, itemY;
        boolean toolTankOnLeft = position == OverlayPosition.TOP_RIGHT ||
                                position == OverlayPosition.BOTTOM_RIGHT;
        boolean itemsAbove = position == OverlayPosition.BOTTOM_LEFT ||
                            position == OverlayPosition.BOTTOM_RIGHT;
        boolean isRightSide = position == OverlayPosition.TOP_RIGHT ||
                             position == OverlayPosition.BOTTOM_RIGHT;

        float scaleFactor = (float) CommonConfig.getSpriteScaleFactor();

        // Apply right side padding to prevent clipping (preset right positions only)
        int paddingOffset = isRightSide ? (int) (16 / scaleFactor) : 0;
        int adjustedX = scaledX - paddingOffset;

        if (toolTankOnLeft) {
            // Tool tank left, chest tank right
            tank1X = adjustedX + FRAME_WIDTH;  // Chest tank
            tank2X = adjustedX;                // Tool tank
            item1X = adjustedX + FRAME_WIDTH;  // Chest item
            item2X = adjustedX;                // Tool item
        } else {
            // Chest tank left, tool tank right (default, incl. CUSTOM)
            tank1X = adjustedX;                // Chest tank
            tank2X = adjustedX + FRAME_WIDTH;  // Tool tank
            item1X = adjustedX;                // Chest item
            item2X = adjustedX + FRAME_WIDTH;  // Tool item
        }

        itemY = itemsAbove ? scaledY - ICON_SIZE : scaledY + FRAME_HEIGHT;

        // Render chest tank sprite
        renderTankLayers(graphics, tank1X, scaledY, tankU, tankV, chestFuelU, 0, chestWaterU, FRAME_HEIGHT);

        // Render tool tank sprite
        renderTankLayers(graphics, tank2X, scaledY, tankU, tankV, toolFuelU, 0, toolWaterU, FRAME_HEIGHT);

        // Render items
        GuiGameElement.of(tankItem).at(item1X, itemY, 450).render(graphics);
        GuiGameElement.of(toolItem).at(item2X, itemY, 450).render(graphics);
    }

    private void renderSingleTankDisplay(GuiGraphics graphics, int scaledX, int scaledY,
                                       ItemStack tankItem,
                                       OverlayPosition position) {
        // Apply right side padding to prevent clipping (preset right positions only)
        boolean isRightSide = position == OverlayPosition.TOP_RIGHT ||
                             position == OverlayPosition.BOTTOM_RIGHT;
        float scaleFactor = (float) CommonConfig.getSpriteScaleFactor();
        int paddingOffset = isRightSide ? (int) (16 / scaleFactor) : 0;
        int adjustedX = scaledX - paddingOffset;

        // Compute fuel/water frames from the tank we're actually showing (works for an empty tank too).
        int fuelU = frameU(TankDataManager.getFuelLevel(tankItem));
        int waterU = frameU(TankDataManager.getWaterLevel(tankItem));
        int tankU = 0;
        int tankV = 2 * FRAME_HEIGHT; // tank outline row

        // Render single tank sprite
        renderTankLayers(graphics, adjustedX, scaledY, tankU, tankV, fuelU, 0, waterU, FRAME_HEIGHT);

        // Determine item positioning based on overlay position
        boolean itemsAbove = position == OverlayPosition.BOTTOM_LEFT ||
                            position == OverlayPosition.BOTTOM_RIGHT;
        int itemY = itemsAbove ? scaledY - ICON_SIZE : scaledY + FRAME_HEIGHT;

        // Render only the tank item's icon. The held item is intentionally not drawn here — if the
        // held item were a tank we'd be in renderDualTankDisplay, so anything else (e.g. bone meal)
        // is irrelevant and must not appear next to the tank.
        GuiGameElement.of(tankItem).at(adjustedX, itemY, 450).render(graphics);
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
    public static int[] currentAnchor(int guiW, int guiH) {
        float scaleFactor = (float) CommonConfig.getSpriteScaleFactor();
        int renderWidth = (int) (FRAME_WIDTH * scaleFactor);
        int renderHeight = (int) (FRAME_HEIGHT * scaleFactor);
        return OverlayAnchor.resolveSprite(CommonConfig.getOverlayPosition(), guiW, guiH, renderWidth, renderHeight);
    }
}
