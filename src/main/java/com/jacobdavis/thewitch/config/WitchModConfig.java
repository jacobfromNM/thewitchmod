package com.jacobdavis.thewitch.config;

// Minecraft Imports...
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * WitchModConfig class for managing mod configuration.
 * This class defines the configuration options for the mod.
 */
public class WitchModConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;
    
    // Witch Behavior...
    public static final ForgeConfigSpec.IntValue SPAWN_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue STARE_TICKS;
    public static final ForgeConfigSpec.IntValue LIGHT_BREAK_RADIUS;
    public static final ForgeConfigSpec.DoubleValue WITCH_SPEED;
        public static final ForgeConfigSpec.BooleanValue SPAWN_CANDLES;
    
    // Sound Settings...
    public static final ForgeConfigSpec.BooleanValue PLAY_PROXIMITY_SOUND;
    public static final ForgeConfigSpec.BooleanValue PLAY_SCREAM_SOUND;
    public static final ForgeConfigSpec.BooleanValue PLAY_ATTACK_SOUND;
    
    // Logging settings...
    public static ForgeConfigSpec.BooleanValue ENABLE_LOGGING;


    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("The Witch Mod Configuration").push("general");
        
        // Witch Behavior Settings...
        builder.comment("Witch Behavior Settings").push("witchBehavior");
        SPAWN_INTERVAL_TICKS = builder
                .comment("Time between spawns in ticks (default = 48000 for 2 in-game days)")
                .defineInRange("spawnIntervalTicks", 48000, 3000, Integer.MAX_VALUE);

        STARE_TICKS = builder
                .comment("Ticks the player must stare to trigger the witch (default = 10 [half a second])")
                .defineInRange("stareTicks", 10, 1, 60);

        LIGHT_BREAK_RADIUS = builder
                .comment("Radius (in blocks) to break lights when triggered. WARNING: High values will cause tick delays! (default = 128)")
                .defineInRange("lightBreakRadius", 128, 1, 512);

        WITCH_SPEED = builder
                .comment("Witch movement speed after being triggered (default = 2.0)")
                .defineInRange("witchSpeed", 2.0, 0.1, 5.0);
        SPAWN_CANDLES = builder
                .comment("Spawn candles when the witch is triggered and when lore books drop. (default = true)")
                .define("spawnCandles", true);                

        builder.pop();

        // Sound Settings...
        builder.comment("Sound Settings").push("soundSettings");
        PLAY_PROXIMITY_SOUND = builder
                .comment("Play the proximity sound with the player is near the witch. (default = true)")
                .define("playProximitySound", true);

        PLAY_SCREAM_SOUND = builder
                .comment("Play scream sound when the player attacks the witch. (default = true)")
                .define("playScreamSound", true);

        PLAY_ATTACK_SOUND = builder
                .comment("Play attack sound when the witch attacks the player. (default = true)")
                .define("playAttackSound", true);
        builder.pop();

        // Logging Settings...
        builder.comment("Logging Settings").push("loggingSettings");
        ENABLE_LOGGING = builder
                .comment("Enable logging for debugging purposes. (default = false)")
                .define("enableLogging", false);
        builder.pop();
        builder.pop();

        COMMON_CONFIG = builder.build();
    }
}
