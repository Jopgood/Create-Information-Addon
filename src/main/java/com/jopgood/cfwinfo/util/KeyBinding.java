package com.jopgood.cfwinfo.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY_MOD = "Create: Information";
    public static final String KEY_ENABLE_INFO = "Enable Information";

    public static final KeyMapping INFO_KEY = new KeyMapping(KEY_ENABLE_INFO, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KEY_CATEGORY_MOD);
}