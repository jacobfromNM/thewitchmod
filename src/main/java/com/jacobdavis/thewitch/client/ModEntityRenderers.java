package com.jacobdavis.thewitch.client;

import com.jacobdavis.thewitch.entity.StalkerWitchEntity;
import com.jacobdavis.thewitch.registry.ModEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid="thewitch", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class ModEntityRenderers {

    @Mod.EventBusSubscriber(modid="thewitch", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
    public static class ClientSetup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> EntityRenderers.register(ModEntities.STALKER_WITCH.get(), context -> new MobRenderer<StalkerWitchEntity, HumanoidModel<StalkerWitchEntity>>(context, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5f){
                private final ResourceLocation WITCH_TEXTURE = new ResourceLocation("thewitch", "textures/entity/witch.png");
                private final ResourceLocation EYES_TEXTURE = new ResourceLocation("thewitch", "textures/entity/witch_eyes.png");
                {
                    this.addLayer(new EyesLayer(this));
                }

                public ResourceLocation getTextureLocation(StalkerWitchEntity entity) {
                    return this.WITCH_TEXTURE;
                }

                class EyesLayer<T extends StalkerWitchEntity>
                extends RenderLayer<T, HumanoidModel<T>> {
                    private final RenderType EYES;

                    public EyesLayer(RenderLayerParent<T, HumanoidModel<T>> parent) {
                        super(parent);
                        this.EYES = RenderType.eyes(EYES_TEXTURE);
                    }

                    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                        VertexConsumer vertexconsumer = buffer.getBuffer(this.EYES);
                        ((HumanoidModel)this.getParentModel()).renderToBuffer(poseStack, vertexconsumer, 0xF00000, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
                    }
                }
            }));
        }
    }
}

