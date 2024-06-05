package com.jopgood.cfwinfo.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    public static final ForgeConfigSpec.ConfigValue<Boolean> INFORMATION_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SIMPLIFIED;
    
    static {
        BUILDER.push("Configs for Create: Fuel & Water Levels Mod");

        // HERE DEFINE YOUR CONFIGS
        INFORMATION_ENABLED = BUILDER.comment("This will determine whether to display fuel and water information.")
        .define("Enable/Disable Jetpack Information", false);

        SIMPLIFIED = BUILDER.comment("Determine to show a simplified version of the fuel and water levels.")
                        .define("Enable/Disable simplified information", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}