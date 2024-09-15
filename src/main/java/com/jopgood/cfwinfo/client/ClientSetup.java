package com.jopgood.cfwinfo.client;

import com.jopgood.cfwinfo.CfwInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CfwInfo.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    private static boolean overlaysRegistered = false;

    public static void init(FMLClientSetupEvent event) {
        // Client-specific initialisation code

    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBinding.INFO_KEY);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        if (!overlaysRegistered) {
            event.registerAboveAll("info", InformationHudOverlay.INFO_OVERLAY);
            event.registerAboveAll("tank_info", JetpackHudOverlay.TANK_SPRITE);
            overlaysRegistered = true;
        }
    }
}
