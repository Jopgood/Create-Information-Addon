package com.jopgood.cfwinfo.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class CommonConfig {

    // Use the configure method to create both the config holder and spec together
    public static final CommonConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    static {
        Pair<CommonConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    // Config values as instance fields
    public final ModConfigSpec.BooleanValue infoEnabled;
    public final ModConfigSpec.BooleanValue simplifiedEnabled;
    public final ModConfigSpec.BooleanValue messagesEnabled;

    // Overlay Configs
    public final ModConfigSpec.IntValue overlayOpacity;
    public final ModConfigSpec.EnumValue<OverlayPosition> overlayPosition;
    public final ModConfigSpec.DoubleValue spriteScaleFactor;

    private CommonConfig(ModConfigSpec.Builder builder) {
        // Create a section for overlay settings
        builder.push("overlay");

        infoEnabled = builder
                .comment("Enable or disable the fuel and water information overlay")
                .translation("cfwinfo.config.info_enabled")
                .define("info_enabled", true);

        simplifiedEnabled = builder
                .comment("Show a simplified version of the overlay without requiring goggles")
                .translation("cfwinfo.config.simplified_enabled")
                .define("simplified_enabled", false);

        overlayOpacity = builder
                .comment("Opacity of the overlay (0-100)")
                .translation("cfwinfo.config.overlay_opacity")
                .defineInRange("overlay_opacity", 80, 0, 100);

        overlayPosition = builder
                .comment("Position of the overlay on screen")
                .translation("cfwinfo.config.overlay_position")
                .defineEnum("overlay_position", OverlayPosition.TOP_LEFT);

        spriteScaleFactor = builder
                .comment("Scale factor for tank overlay size (1.0 = normal size, 2.0 = double size)")
                .translation("cfwinfo.config.sprite_scale_factor")
                .defineInRange("sprite_scale_factor", 2.0, 0.5, 5.0);

        messagesEnabled = builder
                .comment("Show notification messages when changing settings")
                .translation("cfwinfo.config.messages_enabled")
                .define("messages_enabled", false);

        builder.pop(); // Exit overlay section
    }

    // Enum for overlay position options
    public enum OverlayPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    // Convenience getters (you can access INSTANCE.configValue.get() directly)
    public static boolean isInfoEnabled() {
        return INSTANCE.infoEnabled.get();
    }

    public static void enableInfo(Boolean enable) {
        INSTANCE.infoEnabled.set(enable);
    }

    public static boolean isSimplifiedEnabled() {
        return INSTANCE.simplifiedEnabled.get();
    }

    public static void enableSimplified(Boolean enable) {
        INSTANCE.simplifiedEnabled.set(enable);
    }

    public static boolean isMessagesEnabled() {
        return INSTANCE.messagesEnabled.get();
    }

    public static int getOverlayOpacity() {
        return INSTANCE.overlayOpacity.get();
    }

    public static OverlayPosition getOverlayPosition() {
        return INSTANCE.overlayPosition.get();
    }

    public static double getSpriteScaleFactor() {
        return INSTANCE.spriteScaleFactor.get();
    }

}