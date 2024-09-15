package com.jopgood.cfwinfo.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    public static final ForgeConfigSpec.ConfigValue<Boolean> FEATURE_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SIMPLIFIED;
    
    static {
        BUILDER.push("Configs for Create: Fuel & Water Levels Mod");

        // HERE DEFINE THE CONFIGS
        FEATURE_ENABLED = BUILDER.comment("This will determine whether to display fuel and water information.")
        .define("Enable/Disable Jetpack Information", false);

        SIMPLIFIED = BUILDER.comment("Determine to show a simplified version of the fuel and water levels.")
                        .define("Enable/Disable simplified information", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}