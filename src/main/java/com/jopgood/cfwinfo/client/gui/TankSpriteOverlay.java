package com.jopgood.cfwinfo.client.gui;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.jopgood.cfwinfo.common.data.TankDataManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
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

    // Scale factor is now configurable - these will be calculated dynamically

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

        // Set up rendering (following NeoForged best practices)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SPRITE);

        // Apply opacity from config
        float opacity = CommonConfig.getOverlayOpacity() / 100.0f;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);

        // Use pose stack scaling for proper sprite scaling
        graphics.pose().pushPose();
        graphics.pose().scale(scaleFactor, scaleFactor, 1.0f);
        
        // Adjust position to account for scaling (scale affects position too)
        int scaledX = (int) (x / scaleFactor);
        int scaledY = (int) (y / scaleFactor);

        // Render the sprites at original size (scaling is handled by pose stack)
        // Tank outline first (background layer)
        graphics.blit(SPRITE, scaledX, scaledY, tankU, tankV, FRAME_WIDTH, FRAME_HEIGHT,
                SPRITE_SHEET_WIDTH, SPRITE_SHEET_HEIGHT);

        // Fuel level (middle layer)
        graphics.blit(SPRITE, scaledX, scaledY, fuelU, fuelV, FRAME_WIDTH, FRAME_HEIGHT,
                SPRITE_SHEET_WIDTH, SPRITE_SHEET_HEIGHT);

        // Water level (top layer)
        graphics.blit(SPRITE, scaledX, scaledY, waterU, waterV, FRAME_WIDTH, FRAME_HEIGHT,
                SPRITE_SHEET_WIDTH, SPRITE_SHEET_HEIGHT);
        
        graphics.pose().popPose();

        // Reset shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}