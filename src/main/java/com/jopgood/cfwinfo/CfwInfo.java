package com.jopgood.cfwinfo;

import org.slf4j.Logger;

import com.jopgood.cfwinfo.client.CustomIHaveGoggleInformation;
import com.jopgood.cfwinfo.config.ClientConfig;
import com.jopgood.cfwinfo.items.JetpackInformation;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(CfwInfo.MODID)
public class CfwInfo implements CustomIHaveGoggleInformation {
	
    public static final String MODID = "cfwinfo";

    private static final Logger LOGGER = LogUtils.getLogger();
    
    public CfwInfo() {
        
        // Register event listeners
        MinecraftForge.EVENT_BUS.register(this);
        
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "csainfo-client.toml");
    }
    
    
    @Mod.EventBusSubscriber(modid = CfwInfo.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ClientSetup {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			LOGGER.info("Client Setup...");
			// Update jetpack information when the client setup event is fired
			updateJetpackInformation();
		}
	}
    
    // Method to update the jetpack information
 	private static void updateJetpackInformation() {
 		// Get the player entity (assuming it's available during client setup)
 		Minecraft mc = Minecraft.getInstance();
 		Player player = mc.player;
 		if (player != null) {
 			LOGGER.info("Player exists, executing JetpackInfo...");
 			JetpackInformation.updateTooltipWithSpacing(player);
 		}
 	}
    
    
}
