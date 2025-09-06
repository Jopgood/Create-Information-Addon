package com.jopgood.cfwinfo.client;

import com.jopgood.cfwinfo.CfwInfo;
import com.jopgood.cfwinfo.client.gui.TankSpriteOverlay;
import com.jopgood.cfwinfo.client.gui.TankTooltipOverlay;
import com.jopgood.cfwinfo.common.config.CommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import static com.jopgood.cfwinfo.client.KeyBinding.TOGGLE_OVERLAY;
import static com.jopgood.cfwinfo.client.KeyBinding.TOGGLE_SIMPLIFIED;

@Mod(value = CfwInfo.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = CfwInfo.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_OVERLAY.get());
        event.register(TOGGLE_SIMPLIFIED.get());
    }


    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        boolean messages = CommonConfig.isMessagesEnabled();

        while (TOGGLE_OVERLAY.get().consumeClick()) {
            // Toggle overlay visibility
            boolean visible = CommonConfig.isInfoEnabled();
            CommonConfig.enableInfo(!visible);

            if (mc.player != null & messages) {
                mc.player.sendSystemMessage(Component.literal(
                        "Overlay: " + (!visible ? "Shown" : "Hidden")
                ));
            }
        }

        while (TOGGLE_SIMPLIFIED.get().consumeClick()) {
            // Toggle simplified mode
            boolean simple = CommonConfig.isSimplifiedEnabled();
            CommonConfig.enableSimplified(!simple);

            if (mc.player != null & messages) {
                mc.player.sendSystemMessage(Component.literal(
                        "Simplified Mode: " + (!simple ? "On" : "Off")
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        // Register tooltip overlay (full mode)
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(CfwInfo.MODID, "tank_tooltip_overlay"),
                new TankTooltipOverlay()
        );

        // Register sprite overlay (simplified mode)
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(CfwInfo.MODID, "tank_sprite_overlay"),
                new TankSpriteOverlay()
        );

    }
}
