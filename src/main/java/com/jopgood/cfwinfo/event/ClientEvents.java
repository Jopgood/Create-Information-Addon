package com.jopgood.cfwinfo.event;

import com.jopgood.cfwinfo.CfwInfo;
import com.jopgood.cfwinfo.client.InformationHudOverlay;
import com.jopgood.cfwinfo.config.ClientConfig;
import com.jopgood.cfwinfo.util.KeyBinding;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class ClientEvents {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	@Mod.EventBusSubscriber(modid = CfwInfo.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
        	if (KeyBinding.INFO_KEY.consumeClick()) {
                Boolean enabled = ClientConfig.INFORMATION_ENABLED.get();
	            ClientConfig.INFORMATION_ENABLED.set(!enabled);
            }
        }
    }
	
	
	
	@Mod.EventBusSubscriber(modid = CfwInfo.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ClientModBusEvents {
		
		@SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.INFO_KEY);
        }
		
		
		@SubscribeEvent
		public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
			LOGGER.info("Gui Rendered...");
			event.registerAboveAll("info", InformationHudOverlay.INFO_OVERLAY);
		}
	}
	
}