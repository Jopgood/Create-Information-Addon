package com.jopgood.cfwinfo;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Main mod class (Forge / 1.20.1).
 *
 * <p>Note: unlike NeoForge (which permits several {@code @Mod} classes per mod id), Forge allows
 * exactly one. Client-only wiring therefore lives in {@code @Mod.EventBusSubscriber} classes guarded
 * by {@code Dist.CLIENT} (see {@code client.ClientSetup} / {@code client.ClientTickHandler}), not in a
 * separate {@code @Mod} client class.
 */
@Mod(CfwInfo.MODID)
public class CfwInfo {
    public static final String MODID = "cfwinfo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CfwInfo() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        // Register the config spec so FML creates/loads the config file.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
