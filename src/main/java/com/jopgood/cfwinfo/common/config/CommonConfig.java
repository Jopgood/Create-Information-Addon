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

    // Default values, used as a fallback whenever a config value is requested
    // before the config has been loaded into memory (see safeGet/safeSet below).
    private static final boolean DEFAULT_INFO_ENABLED = true;
    private static final boolean DEFAULT_SIMPLIFIED_ENABLED = false;
    private static final boolean DEFAULT_MESSAGES_ENABLED = false;
    private static final int DEFAULT_OVERLAY_OPACITY = 80;
    private static final OverlayPosition DEFAULT_OVERLAY_POSITION = OverlayPosition.TOP_LEFT;
    private static final double DEFAULT_SPRITE_SCALE_FACTOR = 2.0;

    /**
     * Reads a config value, falling back to {@code fallback} if the config spec has
     * not yet been loaded into memory. NeoForge throws
     * "trying to get config values before these are loaded to memory" if a value is
     * accessed too early (e.g. during early client ticks or before the config file is
     * bound), so every accessor routes through here to stay crash-safe.
     */
    private static <T> T safeGet(ModConfigSpec.ConfigValue<T> value, T fallback) {
        if (SPEC.isLoaded()) {
            return value.get();
        }
        return fallback;
    }

    /**
     * Writes a config value, but only once the config spec has been loaded. Setting
     * a value before load would throw the same "not loaded" error, so the write is
     * silently skipped until the config is available.
     *
     * <p>{@link ModConfigSpec.ConfigValue#set} only updates the in-memory config; it does
     * not flush to disk, so runtime changes (e.g. toggling simplified mode with a keybind)
     * would be lost on restart. We call {@link ModConfigSpec#save()} afterwards to persist
     * the change to the config file.
     */
    private static <T> void safeSet(ModConfigSpec.ConfigValue<T> value, T newValue) {
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

    public static double getSpriteScaleFactor() {
        return safeGet(INSTANCE.spriteScaleFactor, DEFAULT_SPRITE_SCALE_FACTOR);
    }

}