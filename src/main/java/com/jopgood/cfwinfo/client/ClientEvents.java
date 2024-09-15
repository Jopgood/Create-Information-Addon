package com.jopgood.cfwinfo.client;

import com.jopgood.cfwinfo.CfwInfo;
import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("unused")
public class ClientEvents {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	@Mod.EventBusSubscriber(modid = CfwInfo.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
		@SubscribeEvent
		public static void onKeyInput(InputEvent.Key event) {
			if (KeyBinding.INFO_KEY.consumeClick()) {
				Boolean enabled = CommonConfig.FEATURE_ENABLED.get();
				CommonConfig.FEATURE_ENABLED.set(!enabled);
			}
		}
	}
	
}