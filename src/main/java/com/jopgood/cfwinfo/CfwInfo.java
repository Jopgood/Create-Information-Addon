package com.jopgood.cfwinfo;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(CfwInfo.MODID)
public class CfwInfo {
	public static final String MODID = "cfwinfo";

	public CfwInfo() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "cfwinfo-common.toml");

		if (FMLLoader.getDist().isClient()) {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		}

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		// Client-specific setup code
		com.jopgood.cfwinfo.client.ClientSetup.init(event);
	}
}