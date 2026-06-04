package com.jopgood.cfwinfo.client;

import com.jopgood.cfwinfo.CfwInfo;
import com.jopgood.cfwinfo.client.gui.TankSpriteOverlay;
import com.jopgood.cfwinfo.client.gui.TankTooltipOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.jopgood.cfwinfo.client.KeyBinding.EDIT_OVERLAY;
import static com.jopgood.cfwinfo.client.KeyBinding.TOGGLE_OVERLAY;
import static com.jopgood.cfwinfo.client.KeyBinding.TOGGLE_SIMPLIFIED;

/**
 * Client-side registration on the MOD event bus (keybinds + HUD overlays).
 *
 * <p>Forge 1.20.1 uses {@link net.minecraftforge.client.gui.overlay.IGuiOverlay} registered via
 * {@link RegisterGuiOverlaysEvent}, rather than NeoForge's {@code LayeredDraw.Layer} /
 * {@code RegisterGuiLayersEvent}.
 */
@Mod.EventBusSubscriber(modid = CfwInfo.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_OVERLAY.get());
        event.register(TOGGLE_SIMPLIFIED.get());
        event.register(EDIT_OVERLAY.get());
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        // Detailed/text overlay (full mode) and simplified sprite overlay, drawn above all vanilla HUD.
        event.registerAboveAll("tank_tooltip_overlay", new TankTooltipOverlay());
        event.registerAboveAll("tank_sprite_overlay", new TankSpriteOverlay());
    }
}
