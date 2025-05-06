package com.jacobdavis.thewitch.registry;

// Project Imports...
import com.jacobdavis.thewitch.TheWitch;
import com.jacobdavis.thewitch.entity.StalkerWitchEntity;

// Minecraft Imports...
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * This class handles the registration of custom entities for the mod.
 */
public class ModEntities {
        /**
         * Create a DeferredRegister for entity types.
         */
        public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister
                        .create(ForgeRegistries.ENTITY_TYPES, TheWitch.MOD_ID);

        /**
         * Register the Stalker Witch entity type.
         */
        @SuppressWarnings("removal")
        public static final RegistryObject<EntityType<StalkerWitchEntity>> STALKER_WITCH = ENTITIES.register(
                        "stalker_witch",
                        () -> EntityType.Builder.<StalkerWitchEntity>of(StalkerWitchEntity::new, MobCategory.MONSTER)
                                        .sized(0.6F, 1.95F) // Size of a standard Witch
                                        .build(new ResourceLocation(TheWitch.MOD_ID, "stalker_witch").toString()));
}
