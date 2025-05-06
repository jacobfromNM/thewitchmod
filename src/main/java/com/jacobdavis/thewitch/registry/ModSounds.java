package com.jacobdavis.thewitch.registry;

// Project Imports...
import com.jacobdavis.thewitch.TheWitch;

// Minecraft Imports...
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * This class handles the registration of custom sounds for the mod.
 */
public class ModSounds {
    
    /**
     * The DeferredRegister for SoundEvent objects.
     * This is used to register custom sounds in the mod.
     */
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister
            .create(ForgeRegistries.SOUND_EVENTS, TheWitch.MOD_ID);

    // Register sound events
    public static final RegistryObject<SoundEvent> WITCH_AMBIENT = registerSound("witch_ambient");
    public static final RegistryObject<SoundEvent> WITCH_SCREAM = registerSound("witch_scream");
    public static final RegistryObject<SoundEvent> WITCH_ATTACK = registerSound("witch_attack");

    /**
     * Registers a sound event with the given name.
     * The sound event is created with a ResourceLocation that combines the mod ID and the name.
     * @param name
     * @return
     */
    @SuppressWarnings("removal")
    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(TheWitch.MOD_ID, name)));
    }

    /**
     * Registers the SOUND_EVENTS DeferredRegister with the given event bus.
     * This method should be called in the mod's main class to ensure that the sounds are registered properly.
     * @param eventBus
     */
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
