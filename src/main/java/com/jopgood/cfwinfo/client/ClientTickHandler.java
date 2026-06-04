package com.jopgood.cfwinfo.client;

import com.jopgood.cfwinfo.CfwInfo;
import com.jopgood.cfwinfo.client.gui.OverlayEditScreen;
import com.jopgood.cfwinfo.common.config.CommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.jopgood.cfwinfo.client.KeyBinding.EDIT_OVERLAY;
import static com.jopgood.cfwinfo.client.KeyBinding.TOGGLE_OVERLAY;
import static com.jopgood.cfwinfo.client.KeyBinding.TOGGLE_SIMPLIFIED;

/**
 * Keybind handling on the FORGE event bus (client tick). Separate from {@link ClientSetup} because
 * Forge 1.20.1 keybind/overlay registration is on the MOD bus while {@link TickEvent.ClientTickEvent}
 * is on the FORGE bus.
 */
@Mod.EventBusSubscriber(modid = CfwInfo.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        boolean messages = CommonConfig.isMessagesEnabled();

        while (TOGGLE_OVERLAY.get().consumeClick()) {
            boolean visible = CommonConfig.isInfoEnabled();
            CommonConfig.enableInfo(!visible);
            if (mc.player != null && messages) {
                mc.player.sendSystemMessage(Component.literal("Overlay: " + (!visible ? "Shown" : "Hidden")));
            }
        }

        while (TOGGLE_SIMPLIFIED.get().consumeClick()) {
            boolean simple = CommonConfig.isSimplifiedEnabled();
            CommonConfig.enableSimplified(!simple);
            if (mc.player != null && messages) {
                mc.player.sendSystemMessage(Component.literal("Simplified Mode: " + (!simple ? "On" : "Off")));
            }
        }

        while (EDIT_OVERLAY.get().consumeClick()) {
            if (mc.player != null && mc.screen == null) {
                mc.setScreen(new OverlayEditScreen());
            }
        }
    }
}
