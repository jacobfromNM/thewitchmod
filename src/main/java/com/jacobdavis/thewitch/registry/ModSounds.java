package com.jacobdavis.thewitch.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "thewitch");
    public static final RegistryObject<SoundEvent> WITCH_AMBIENT = ModSounds.registerSound("witch_ambient");
    public static final RegistryObject<SoundEvent> WITCH_SCREAM = ModSounds.registerSound("witch_scream");
    public static final RegistryObject<SoundEvent> WITCH_ATTACK = ModSounds.registerSound("witch_attack");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation("thewitch", name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}

