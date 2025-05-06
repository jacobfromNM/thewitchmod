package com.jacobdavis.thewitch.client;

// Mod Imports...
import com.jacobdavis.thewitch.entity.StalkerWitchEntity;
import com.jacobdavis.thewitch.registry.ModEntities;

// Minecraft Imports...
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * ModEntityRenderers class for registering entity renderers.
 * This class is responsible for setting up the rendering of custom entities.
 */
@Mod.EventBusSubscriber(modid = "thewitch", bus = Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {

    /**
     * Registers the entity renderers for the mod.
     * This method is called during the client setup phase.
     *
     * @param event The FMLClientSetupEvent.
     */
    @EventBusSubscriber(modid = "thewitch", bus = Bus.MOD, value = Dist.CLIENT)
    public static class ClientSetup {

        /**
         * Registers the Stalker Witch entity renderer.
         * This method sets up the rendering for the Stalker Witch entity.
         *
         * @param event The FMLClientSetupEvent.
         */
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                EntityRenderers.register(ModEntities.STALKER_WITCH.get(),
                        context -> new MobRenderer<StalkerWitchEntity, HumanoidModel<StalkerWitchEntity>>(
                                context,
                                new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                                0.5f) {

                            // Define the texture locations for the Stalker Witch
                            @SuppressWarnings("removal")
                            private final ResourceLocation WITCH_TEXTURE = new ResourceLocation("thewitch",
                                    "textures/entity/witch.png");
                            @SuppressWarnings("removal")
                            private final ResourceLocation EYES_TEXTURE = new ResourceLocation("thewitch",
                                    "textures/entity/witch_eyes.png");

                            {
                                // Add glowing eyes layer
                                this.addLayer(new EyesLayer<>(this));
                            }

                            @Override
                            public ResourceLocation getTextureLocation(StalkerWitchEntity entity) {
                                return WITCH_TEXTURE;
                            }

                            // Inner class for glowing eyes
                            class EyesLayer<T extends StalkerWitchEntity> extends
                                    net.minecraft.client.renderer.entity.layers.RenderLayer<T, HumanoidModel<T>> {
                                private final RenderType EYES = RenderType.eyes(EYES_TEXTURE);

                                public EyesLayer(RenderLayerParent<T, HumanoidModel<T>> parent) {
                                    super(parent);
                                }

                                // Override the render method to apply the glowing eyes effect...
                                @Override
                                public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                        T entity,
                                        float limbSwing, float limbSwingAmount, float partialTicks,
                                        float ageInTicks, float netHeadYaw, float headPitch) {
                                    VertexConsumer vertexconsumer = buffer.getBuffer(EYES);
                                    this.getParentModel().renderToBuffer(poseStack, vertexconsumer, 15728640,
                                            OverlayTexture.NO_OVERLAY,
                                            1.0F, 1.0F, 1.0F, 1.0F); // full brightness
                                }
                            }
                        });
            });
        }

    }
}
