package com.jacobdavis.thewitch;

import com.jacobdavis.thewitch.entity.StalkerWitchEntity;
import com.jacobdavis.thewitch.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="thewitch", bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void onEntityAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.STALKER_WITCH.get(), StalkerWitchEntity.createAttributes().build());
    }
}

