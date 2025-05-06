package com.jacobdavis.thewitch;

// Project Imports...
import com.jacobdavis.thewitch.config.WitchModConfig;
import com.jacobdavis.thewitch.registry.ModEntities;
import com.jacobdavis.thewitch.registry.ModSounds;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

// Minecraft Imports...
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * TheWitch class is the main entry point for the mod.
 * It handles the registration of entities, sounds, and configuration.
 */
@Mod("thewitch")
public class TheWitch {
    public static final String MOD_ID = "thewitch";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Constructor for TheWitch mod.
     * This method registers the mod's entities, sounds, and configuration.
     */
    @SuppressWarnings("removal")
    public TheWitch() {
        LOGGER.info("[The Witch] Initializing The Witch mod...");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modEventBus);
        ModSounds.register(modEventBus);
        modEventBus.addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WitchModConfig.COMMON_CONFIG);
    }


    private void setup(final FMLCommonSetupEvent event) {
        // mmhmm...
    }
}
