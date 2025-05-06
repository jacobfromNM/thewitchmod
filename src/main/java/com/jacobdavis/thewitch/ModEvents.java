package com.jacobdavis.thewitch;

// Project imports...
import com.jacobdavis.thewitch.registry.ModEntities;
import com.jacobdavis.thewitch.entity.StalkerWitchEntity;

// Minecraft imports...
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * This class handles mod events, such as registering entity attributes.
 */
@Mod.EventBusSubscriber(modid = TheWitch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    /**
     * Registers entity attributes for the Stalker Witch.
     *
     * @param event The event containing the entity attribute creation data.
     */
    @SubscribeEvent
    public static void onEntityAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.STALKER_WITCH.get(), StalkerWitchEntity.createAttributes().build());
    }
}
