package com.jopgood.cfwinfo.client.gui;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.jopgood.cfwinfo.common.data.TankDataManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
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

        renderTankSprites(graphics, player);
    }

    private void renderTankSprites(GuiGraphics graphics, Player player) {
        ItemStack tankItem = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack toolItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Get the fuel and water levels and calculate the frame index
        int fuelLevel = (int) Math.round(TankDataManager.getFuelLevel(tankItem));
        int waterLevel = (int) Math.round(TankDataManager.getWaterLevel(tankItem));

        int fuelFrameIndex = ((MAX_LEVEL - fuelLevel) * (TOTAL_FRAMES - 1)) / MAX_LEVEL;
        int waterFrameIndex = ((MAX_LEVEL - waterLevel) * (TOTAL_FRAMES - 1)) / MAX_LEVEL;

        // Clamp frame indices to valid range
        fuelFrameIndex = Math.max(0, Math.min(TOTAL_FRAMES - 1, fuelFrameIndex));
        waterFrameIndex = Math.max(0, Math.min(TOTAL_FRAMES - 1, waterFrameIndex));

        // Calculate texture coordinates (unchanged - these are UV coordinates in the sprite sheet)
        int fuelU = (fuelFrameIndex % FRAMES_PER_ROW) * FRAME_WIDTH;
        int fuelV = 0; // First row

        int waterU = (waterFrameIndex % FRAMES_PER_ROW) * FRAME_WIDTH;
        int waterV = FRAME_HEIGHT; // Second row

        int tankU = 0;
        int tankV = 2 * FRAME_HEIGHT; // Third row (tank outline)

        // Get configurable scale factor and calculate dimensions
        float scaleFactor = (float) CommonConfig.getSpriteScaleFactor();
        int renderWidth = (int) (FRAME_WIDTH * scaleFactor);
        int renderHeight = (int) (FRAME_HEIGHT * scaleFactor);

        // Position based on config - using scaled dimensions for positioning
        int x, y;
        CommonConfig.OverlayPosition position = CommonConfig.getOverlayPosition();

        y = switch (position) {
            case TOP_LEFT -> {
                x = 10;
                yield 10;
            }
            case TOP_RIGHT -> {
                x = graphics.guiWidth() - renderWidth - 10;
                yield 10;
            }
            case BOTTOM_LEFT -> {
                x = 10;
                yield graphics.guiHeight() - renderHeight - 10;
            }
            case BOTTOM_RIGHT -> {
                x = graphics.guiWidth() - renderWidth - 10;
                yield graphics.guiHeight() - renderHeight - 10;
            }
            default -> {
                x = graphics.guiWidth() / 2 - renderWidth / 2;
                yield graphics.guiHeight() / 2 - renderHeight / 2;
            }
        };

        // Set up rendering
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SPRITE);

        // Apply opacity from config
        float opacity = CommonConfig.getOverlayOpacity() / 100.0f;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);

        // Use pose stack scaling for proper sprite scaling
        graphics.pose().pushPose();
        graphics.pose().scale(scaleFactor, scaleFactor, 1.0f);
        
        // Adjust position to account for scaling
        int scaledX = (int) (x / scaleFactor);
        int scaledY = (int) (y / scaleFactor);

        // Decide layout based on which slots actually hold a tank, so we never render a phantom
        // tank for an empty/non-tank slot.
        boolean chestIsTank = TankDataManager.isWearingFuelCapableItem(player) || TankDataManager.isWearingWaterCapableItem(player);
        boolean toolIsTank = TankDataManager.isHoldingFuelCapableItem(player) || TankDataManager.isHoldingWaterCapableItem(player);

        if (chestIsTank && toolIsTank) {
            // Both a worn tank and a held tank: render two tank sprites with position-aware layout
            renderDualTankDisplay(graphics, scaledX, scaledY, tankItem, toolItem,
                                tankU, tankV, fuelU, fuelV, waterU, waterV, position);
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
                                      int tankU, int tankV, int fuelU, int fuelV, int waterU, int waterV,
                                      CommonConfig.OverlayPosition position) {
        // Calculate tool tank levels
        int toolFuelLevel = (int) Math.round(TankDataManager.getFuelLevel(toolItem));
        int toolWaterLevel = (int) Math.round(TankDataManager.getWaterLevel(toolItem));
        
        int toolFuelFrameIndex = ((MAX_LEVEL - toolFuelLevel) * (TOTAL_FRAMES - 1)) / MAX_LEVEL;
        int toolWaterFrameIndex = ((MAX_LEVEL - toolWaterLevel) * (TOTAL_FRAMES - 1)) / MAX_LEVEL;
        
        toolFuelFrameIndex = Math.max(0, Math.min(TOTAL_FRAMES - 1, toolFuelFrameIndex));
        toolWaterFrameIndex = Math.max(0, Math.min(TOTAL_FRAMES - 1, toolWaterFrameIndex));
        
        int toolFuelU = (toolFuelFrameIndex % FRAMES_PER_ROW) * FRAME_WIDTH;
        int toolWaterU = (toolWaterFrameIndex % FRAMES_PER_ROW) * FRAME_WIDTH;
        
        // Determine tank and item positioning based on overlay position
        int tank1X, tank2X, item1X, item2X, itemY;
        boolean toolTankOnLeft = position == CommonConfig.OverlayPosition.TOP_RIGHT || 
                                position == CommonConfig.OverlayPosition.BOTTOM_RIGHT;
        boolean itemsAbove = position == CommonConfig.OverlayPosition.BOTTOM_LEFT || 
                            position == CommonConfig.OverlayPosition.BOTTOM_RIGHT;
        boolean isRightSide = position == CommonConfig.OverlayPosition.TOP_RIGHT || 
                             position == CommonConfig.OverlayPosition.BOTTOM_RIGHT;

        float scaleFactor = (float) CommonConfig.getSpriteScaleFactor();
        
        // Apply right side padding to prevent clipping
        int paddingOffset = isRightSide ? (int)(16 / scaleFactor) : 0;
        int adjustedX = scaledX - paddingOffset;
        
        if (toolTankOnLeft) {
            // Tool tank left, chest tank right
            tank1X = adjustedX + FRAME_WIDTH;  // Chest tank
            tank2X = adjustedX;                // Tool tank  
            item1X = adjustedX + FRAME_WIDTH;  // Chest item
            item2X = adjustedX;                // Tool item
        } else {
            // Chest tank left, tool tank right (default)
            tank1X = adjustedX;                // Chest tank
            tank2X = adjustedX + FRAME_WIDTH;  // Tool tank
            item1X = adjustedX;                // Chest item  
            item2X = adjustedX + FRAME_WIDTH;  // Tool item
        }
        
        itemY = itemsAbove ? scaledY - 16 : scaledY + 32;
        
        // Render chest tank sprite
        renderTankLayers(graphics, tank1X, scaledY, tankU, tankV, fuelU, fuelV, waterU, waterV);
        
        // Render tool tank sprite  
        renderTankLayers(graphics, tank2X, scaledY, tankU, tankV, 
                        toolFuelU, fuelV, toolWaterU, waterV);
        
        // Render items
        GuiGameElement.of(tankItem).at(item1X, itemY, 450).render(graphics);
        GuiGameElement.of(toolItem).at(item2X, itemY, 450).render(graphics);
    }
    
    private void renderSingleTankDisplay(GuiGraphics graphics, int scaledX, int scaledY,
                                       ItemStack tankItem,
                                       CommonConfig.OverlayPosition position) {
        // Apply right side padding to prevent clipping
        boolean isRightSide = position == CommonConfig.OverlayPosition.TOP_RIGHT ||
                             position == CommonConfig.OverlayPosition.BOTTOM_RIGHT;
        float scaleFactor = (float) CommonConfig.getSpriteScaleFactor();
        int paddingOffset = isRightSide ? (int)(16 / scaleFactor) : 0;
        int adjustedX = scaledX - paddingOffset;

        // Compute fuel/water frames from the tank we're actually showing (works for an empty tank too).
        int fuelU = frameU(TankDataManager.getFuelLevel(tankItem));
        int waterU = frameU(TankDataManager.getWaterLevel(tankItem));
        int tankU = 0;
        int tankV = 2 * FRAME_HEIGHT; // tank outline row

        // Render single tank sprite
        renderTankLayers(graphics, adjustedX, scaledY, tankU, tankV, fuelU, 0, waterU, FRAME_HEIGHT);

        // Determine item positioning based on overlay position
        boolean itemsAbove = position == CommonConfig.OverlayPosition.BOTTOM_LEFT ||
                            position == CommonConfig.OverlayPosition.BOTTOM_RIGHT;
        int itemY = itemsAbove ? scaledY - 16 : scaledY + 32;

        // Render only the tank item's icon. The held item is intentionally not drawn here — if the
        // held item were a tank we'd be in renderDualTankDisplay, so anything else (e.g. bone meal)
        // is irrelevant and must not appear next to the tank.
        GuiGameElement.of(tankItem).at(adjustedX, itemY, 450).render(graphics);
    }

    /** Maps a fuel/water level to its column U offset in the sprite sheet. */
    private static int frameU(double level) {
        int frameIndex = ((MAX_LEVEL - (int) Math.round(level)) * (TOTAL_FRAMES - 1)) / MAX_LEVEL;
        frameIndex = Math.max(0, Math.min(TOTAL_FRAMES - 1, frameIndex));
        return (frameIndex % FRAMES_PER_ROW) * FRAME_WIDTH;
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
}