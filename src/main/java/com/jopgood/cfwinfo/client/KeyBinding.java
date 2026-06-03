package com.jopgood.cfwinfo.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.jarjar.nio.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final Lazy<KeyMapping> TOGGLE_OVERLAY = Lazy.of(() -> new KeyMapping(
            "key.cfwinfo.toggle_overlay",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            "key.categories.cfwinfo"
    ));

    public static final Lazy<KeyMapping> TOGGLE_SIMPLIFIED = Lazy.of(() -> new KeyMapping(
            "key.cfwinfo.toggle_simplified",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.cfwinfo"
    ));

    public static final Lazy<KeyMapping> EDIT_OVERLAY = Lazy.of(() -> new KeyMapping(
            "key.cfwinfo.edit_overlay",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.cfwinfo"
    ));
}
