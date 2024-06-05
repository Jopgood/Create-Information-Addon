package com.jopgood.cfwinfo.client;

import com.jopgood.cfwinfo.CfwInfo;
import com.jopgood.cfwinfo.config.ClientConfig;
import com.jopgood.cfwinfo.data.JetpackDataManager;
import com.mojang.blaze3d.systems.RenderSystem;

import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.common.Mod;

import java.lang.Math;


@Mod.EventBusSubscriber(modid = CfwInfo.MODID, value = Dist.CLIENT)
public class JetpackHudOverlay {

	private static final ResourceLocation SPRITE = new ResourceLocation("cfwinfo", "textures/gui/sprites/tank_sprite_sheet.png");
    private static final int FRAME_WIDTH = 15;
    private static final int FRAME_HEIGHT = 32;
    private static final int FRAMES_PER_ROW = 18;
    private static final int TOTAL_FRAMES = 18;
    private static final int SPRITE_SHEET_WIDTH = 270;
    private static final int SPRITE_SHEET_HEIGHT = 96;

    public static final IGuiOverlay TANK_SPRITE = ((gui, guiGraphics, partialTick, width, height) -> {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null){
            return;
        }

        Boolean renderEnabled = ClientConfig.INFORMATION_ENABLED.get();
        Boolean simpleEnabled = ClientConfig.SIMPLIFIED.get();

        boolean wearingGoggles = GogglesItem.isWearingGoggles(mc.player);

        if (!renderEnabled || !wearingGoggles || !simpleEnabled) { // Check if rendering is disabled
            return; // If disabled, don't render anything
        }

        if (JetpackDataManager.isWearingJetpack(player)) {
            ItemStack jetpackItem = player.getItemBySlot(EquipmentSlot.CHEST);

            // Get the fuel and water levels and calculate the frame index
            int max = 1600;

            int fuelLevel = (int) Math.round(JetpackDataManager.getFuelLevel(jetpackItem));
            int waterLevel = (int) Math.round(JetpackDataManager.getWaterLevel(jetpackItem));

            int fuelFrameIndex = ((max - fuelLevel) * (TOTAL_FRAMES - 1)) / max;
            int waterFrameIndex = ((max - waterLevel) * (TOTAL_FRAMES - 1)) / max;

            // Calculate texture coordinates for row 1 (water level)
            int fuelU = (fuelFrameIndex % FRAMES_PER_ROW) * FRAME_WIDTH;
            int fuelV = 0; // First row

            int waterU = (waterFrameIndex % FRAMES_PER_ROW) * FRAME_WIDTH;
            int waterV;
            waterV = FRAME_HEIGHT;

            int tankU = 0;
            int tankV = 2 * FRAME_HEIGHT;

            // Draw the sprites
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, SPRITE);
            int x = 10; // X position to render the sprite
            int y = 10; // Y position to render the sprite
            guiGraphics.blit(SPRITE, x, y, 0, fuelU, fuelV, FRAME_WIDTH, FRAME_HEIGHT, SPRITE_SHEET_WIDTH, SPRITE_SHEET_HEIGHT);
            guiGraphics.blit(SPRITE, x, y, 0, waterU, waterV, FRAME_WIDTH, FRAME_HEIGHT, SPRITE_SHEET_WIDTH, SPRITE_SHEET_HEIGHT);
            guiGraphics.blit(SPRITE, x, y, 0, tankU, tankV, FRAME_WIDTH, FRAME_HEIGHT, SPRITE_SHEET_WIDTH, SPRITE_SHEET_HEIGHT);
        }
    });
}
