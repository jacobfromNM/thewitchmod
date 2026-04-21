package com.jacobdavis.thewitch.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("The Witch Configuration"))
                .setSavingRunnable(WitchModConfig::save);

        ConfigEntryBuilder eb = builder.entryBuilder();

        ConfigCategory stats = builder.getOrCreateCategory(Component.literal("Entity Stats"));
        stats.addEntry(eb.startDoubleField(Component.literal("Witch Speed"), WitchModConfig.WITCH_SPEED.get())
                .setDefaultValue(2.0)
                .setMin(0.1).setMax(5.0)
                .setSaveConsumer(v -> WitchModConfig.WITCH_SPEED.set(v))
                .build());
        stats.addEntry(eb.startDoubleField(Component.literal("Kill Zone Radius"), WitchModConfig.KILL_ZONE_RADIUS.get())
                .setDefaultValue(3.0)
                .setMin(1.0).setMax(10.0)
                .setSaveConsumer(v -> WitchModConfig.KILL_ZONE_RADIUS.set(v))
                .build());
        stats.addEntry(eb.startIntField(Component.literal("Aggression Ramp (ticks)"), WitchModConfig.AGGRESSION_RAMP_TICKS.get())
                .setDefaultValue(200)
                .setMin(20).setMax(600)
                .setSaveConsumer(v -> WitchModConfig.AGGRESSION_RAMP_TICKS.set(v))
                .build());
        stats.addEntry(eb.startIntField(Component.literal("Light Break Radius"), WitchModConfig.LIGHT_BREAK_RADIUS.get())
                .setDefaultValue(128)
                .setMin(1).setMax(512)
                .setSaveConsumer(v -> WitchModConfig.LIGHT_BREAK_RADIUS.set(v))
                .build());
        stats.addEntry(eb.startBooleanToggle(Component.literal("Spawn Candles"), WitchModConfig.SPAWN_CANDLES.get())
                .setDefaultValue(true)
                .setSaveConsumer(v -> WitchModConfig.SPAWN_CANDLES.set(v))
                .build());

        ConfigCategory spawn = builder.getOrCreateCategory(Component.literal("Spawn Settings"));
        spawn.addEntry(eb.startIntField(Component.literal("Spawn Interval (ticks)"), WitchModConfig.SPAWN_INTERVAL_TICKS.get())
                .setDefaultValue(48000)
                .setMin(3000).setMax(Integer.MAX_VALUE)
                .setSaveConsumer(v -> WitchModConfig.SPAWN_INTERVAL_TICKS.set(v))
                .build());
        spawn.addEntry(eb.startIntField(Component.literal("Stare Ticks to Trigger"), WitchModConfig.STARE_TICKS.get())
                .setDefaultValue(10)
                .setMin(1).setMax(60)
                .setSaveConsumer(v -> WitchModConfig.STARE_TICKS.set(v))
                .build());

        ConfigCategory sounds = builder.getOrCreateCategory(Component.literal("Sound Settings"));
        sounds.addEntry(eb.startBooleanToggle(Component.literal("Play Proximity Sound"), WitchModConfig.PLAY_PROXIMITY_SOUND.get())
                .setDefaultValue(true)
                .setSaveConsumer(v -> WitchModConfig.PLAY_PROXIMITY_SOUND.set(v))
                .build());
        sounds.addEntry(eb.startBooleanToggle(Component.literal("Play Scream Sound"), WitchModConfig.PLAY_SCREAM_SOUND.get())
                .setDefaultValue(true)
                .setSaveConsumer(v -> WitchModConfig.PLAY_SCREAM_SOUND.set(v))
                .build());
        sounds.addEntry(eb.startBooleanToggle(Component.literal("Play Attack Sound"), WitchModConfig.PLAY_ATTACK_SOUND.get())
                .setDefaultValue(true)
                .setSaveConsumer(v -> WitchModConfig.PLAY_ATTACK_SOUND.set(v))
                .build());

        ConfigCategory logging = builder.getOrCreateCategory(Component.literal("Logging"));
        logging.addEntry(eb.startBooleanToggle(Component.literal("Enable Logging"), WitchModConfig.ENABLE_LOGGING.get())
                .setDefaultValue(false)
                .setSaveConsumer(v -> WitchModConfig.ENABLE_LOGGING.set(v))
                .build());

        return builder.build();
    }
}
