package com.jacobdavis.thewitch;

import com.jacobdavis.thewitch.config.WitchConfigScreen;
import com.jacobdavis.thewitch.config.WitchModConfig;
import com.jacobdavis.thewitch.registry.ModEntities;
import com.jacobdavis.thewitch.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(value="thewitch")
public class TheWitch {
    public static final String MOD_ID = "thewitch";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheWitch() {
        LOGGER.info("[The Witch] Initializing The Witch mod...");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modEventBus);
        ModSounds.register(modEventBus);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onConfigLoad);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WitchModConfig.COMMON_CONFIG);
    }

    private void setup(FMLCommonSetupEvent event) {
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("cloth_config")) {
            ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> WitchConfigScreen.create(parent))
            );
        }
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == WitchModConfig.COMMON_CONFIG) {
            WitchModConfig.onLoad(event.getConfig());
        }
    }
}

