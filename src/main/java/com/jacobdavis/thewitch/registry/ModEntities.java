package com.jacobdavis.thewitch.registry;

import com.jacobdavis.thewitch.entity.StalkerWitchEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "thewitch");
    public static final RegistryObject<EntityType<StalkerWitchEntity>> STALKER_WITCH = ENTITIES.register("stalker_witch", () -> EntityType.Builder.of(StalkerWitchEntity::new, MobCategory.MONSTER).sized(0.6f, 1.95f).build(new ResourceLocation("thewitch", "stalker_witch").toString()));
}

