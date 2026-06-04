package com.jopgood.cfwinfo.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class CommonConfig {

    // Use the configure method to create both the config holder and spec together
    public static final CommonConfig INSTANCE;
    public static final ForgeConfigSpec SPEC;

    static {
        Pair<CommonConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    // Config values as instance fields
    public final ForgeConfigSpec.BooleanValue infoEnabled;
    public final ForgeConfigSpec.BooleanValue simplifiedEnabled;
    public final ForgeConfigSpec.BooleanValue messagesEnabled;

    // Overlay Configs
    public final ForgeConfigSpec.IntValue overlayOpacity;
    public final ForgeConfigSpec.EnumValue<OverlayPosition> overlayPosition;
    public final ForgeConfigSpec.DoubleValue spriteScaleFactor;

    // Custom (dragged) overlay position, in GUI-scaled pixels. Only used when
    // overlayPosition == CUSTOM. Stored as the top-left anchor of the overlay.
    public final ForgeConfigSpec.IntValue customX;
    public final ForgeConfigSpec.IntValue customY;

    private CommonConfig(ForgeConfigSpec.Builder builder) {
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

        customX = builder
                .comment("Custom overlay X position in GUI pixels (used only when overlay_position = CUSTOM)")
                .translation("cfwinfo.config.custom_x")
                .defineInRange("custom_x", 10, 0, 10000);

        customY = builder
                .comment("Custom overlay Y position in GUI pixels (used only when overlay_position = CUSTOM)")
                .translation("cfwinfo.config.custom_y")
                .defineInRange("custom_y", 10, 0, 10000);

        builder.pop(); // Exit overlay section
    }

    // Enum for overlay position options
    public enum OverlayPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        /** Free position dragged by the player; uses customX / customY. */
        CUSTOM
    }

    // Default values, used as a fallback whenever a config value is requested
    // before the config has been loaded into memory (see safeGet/safeSet below).
    private static final boolean DEFAULT_INFO_ENABLED = true;
    private static final boolean DEFAULT_SIMPLIFIED_ENABLED = false;
    private static final boolean DEFAULT_MESSAGES_ENABLED = false;
    private static final int DEFAULT_OVERLAY_OPACITY = 80;
    private static final OverlayPosition DEFAULT_OVERLAY_POSITION = OverlayPosition.TOP_LEFT;
    private static final double DEFAULT_SPRITE_SCALE_FACTOR = 2.0;
    private static final int DEFAULT_CUSTOM_X = 10;
    private static final int DEFAULT_CUSTOM_Y = 10;

    /**
     * Reads a config value, falling back to {@code fallback} until the spec is loaded.
     * NeoForge throws if a value is accessed before the config is bound to memory.
     */
    private static <T> T safeGet(ForgeConfigSpec.ConfigValue<T> value, T fallback) {
        if (SPEC.isLoaded()) {
            return value.get();
        }
        return fallback;
    }

    /**
     * Writes a config value once the spec is loaded, then flushes it to disk.
     * {@link ForgeConfigSpec.ConfigValue#set} only updates the in-memory config, so an explicit
     * {@link ForgeConfigSpec#save()} is needed for runtime changes to survive a restart.
     */
    private static <T> void safeSet(ForgeConfigSpec.ConfigValue<T> value, T newValue) {
        if (SPEC.isLoaded()) {
            value.set(newValue);
            SPEC.save();
        }
    }

    // Convenience getters (you can access INSTANCE.configValue.get() directly)
    public static boolean isInfoEnabled() {
        return safeGet(INSTANCE.infoEnabled, DEFAULT_INFO_ENABLED);
    }

    public static void enableInfo(Boolean enable) {
        safeSet(INSTANCE.infoEnabled, enable);
    }

    public static boolean isSimplifiedEnabled() {
        return safeGet(INSTANCE.simplifiedEnabled, DEFAULT_SIMPLIFIED_ENABLED);
    }

    public static void enableSimplified(Boolean enable) {
        safeSet(INSTANCE.simplifiedEnabled, enable);
    }

    public static boolean isMessagesEnabled() {
        return safeGet(INSTANCE.messagesEnabled, DEFAULT_MESSAGES_ENABLED);
    }

    public static int getOverlayOpacity() {
        return safeGet(INSTANCE.overlayOpacity, DEFAULT_OVERLAY_OPACITY);
    }

    public static OverlayPosition getOverlayPosition() {
        return safeGet(INSTANCE.overlayPosition, DEFAULT_OVERLAY_POSITION);
    }

    public static void setOverlayPosition(OverlayPosition position) {
        safeSet(INSTANCE.overlayPosition, position);
    }

    public static double getSpriteScaleFactor() {
        return safeGet(INSTANCE.spriteScaleFactor, DEFAULT_SPRITE_SCALE_FACTOR);
    }

    /**
     * Updates the sprite scale in memory only (no disk write). Used for live previewing in the
     * overlay editor; the value is persisted later when the editor commits via
     * {@link #setCustomPosition} or {@link #setOverlayPosition}, or reverted on cancel.
     */
    public static void setSpriteScaleFactorTransient(double value) {
        if (SPEC.isLoaded()) {
            INSTANCE.spriteScaleFactor.set(value);
        }
    }

    public static int getCustomX() {
        return safeGet(INSTANCE.customX, DEFAULT_CUSTOM_X);
    }

    public static int getCustomY() {
        return safeGet(INSTANCE.customY, DEFAULT_CUSTOM_Y);
    }

    /**
     * Persists a freely-dragged overlay position and switches the overlay into CUSTOM mode.
     * Writes all three values then saves once, to avoid three separate disk writes.
     */
    public static void setCustomPosition(int x, int y) {
        if (SPEC.isLoaded()) {
            INSTANCE.customX.set(x);
            INSTANCE.customY.set(y);
            INSTANCE.overlayPosition.set(OverlayPosition.CUSTOM);
            SPEC.save();
        }
    }

}