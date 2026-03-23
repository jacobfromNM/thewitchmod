package com.jacobdavis.thewitch.entity;

import com.jacobdavis.thewitch.TheWitch;
import com.jacobdavis.thewitch.config.WitchModConfig;
import com.jacobdavis.thewitch.registry.ModSounds;
import com.jacobdavis.thewitch.world.WitchSpawnHandler;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class StalkerWitchEntity
extends Monster {
    private int stareTicks = 0;
    private int triggeredTicks = 0;
    private int existenceTicks = 0;
    private int ambientSoundCooldown = 0;
    private boolean initialized = false;
    private boolean isTriggered = false;
    private Vec3 teleportTarget = null;
    private int teleportPhaseTicks = 0;

    public StalkerWitchEntity(EntityType<? extends Monster> type, Level world) {
        super(type, world);
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 40.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.initialized) {
            if (this.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
                this.initialized = true;
            } else {
                return;
            }
        }
        if (this.level.isClientSide) {
            return;
        }
        ++this.existenceTicks;
        Player player = this.level.getNearestPlayer(this, 256.0);
        if (player != null) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, player.position());
            if (this.isTriggered) {
                ++this.triggeredTicks;
                if (this.triggeredTicks % 15 == 0) {
                    Vec3 targetPos;
                    Vec3 currentPos = this.position();
                    double distanceSqr = currentPos.distanceToSqr(targetPos = player.position());
                    double killZoneRadius = (Double) WitchModConfig.KILL_ZONE_RADIUS.get();
                    if (distanceSqr > killZoneRadius * killZoneRadius) {
                        Vec3 finalPos;
                        BlockPos checkPos;
                        int dy;
                        int aggressionRampTicks = (Integer) WitchModConfig.AGGRESSION_RAMP_TICKS.get();
                        double aggressionT = Math.min(1.0, (double) this.triggeredTicks / aggressionRampTicks);
                        double teleportScale = 0.5 + aggressionT * 0.42;
                        Vec3 direction = targetPos.subtract(currentPos).scale(teleportScale);
                        Vec3 nextStep = currentPos.add(direction);
                        double jitter = 1.5;
                        nextStep = nextStep.add((this.random.nextDouble() - 0.5) * jitter, 0.0, (this.random.nextDouble() - 0.5) * jitter);
                        BlockPos base = new BlockPos(nextStep);
                        boolean teleported = false;
                        for (dy = 0; dy <= 12; ++dy) {
                            checkPos = base.below(dy);
                            if (!this.level.getBlockState(checkPos).isAir() || !this.level.getBlockState(checkPos.below()).getMaterial().isSolid()) continue;
                            finalPos = new Vec3((double)checkPos.getX() + 0.5, (double)checkPos.getY(), (double)checkPos.getZ() + 0.5);
                            if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
                                TheWitch.LOGGER.info("[The Witch] Found a valid teleport position (downward scan): {}", (Object)finalPos);
                            }
                            this.level.playSound(null, checkPos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.0f, 1.0f);
                            this.teleportTo(finalPos.x, finalPos.y, finalPos.z);
                            teleported = true;
                            break;
                        }
                        if (!teleported) {
                            for (dy = 1; dy <= 12; ++dy) {
                                checkPos = base.above(dy);
                                if (!this.level.getBlockState(checkPos).isAir() || !this.level.getBlockState(checkPos.below()).getMaterial().isSolid()) continue;
                                finalPos = new Vec3((double)checkPos.getX() + 0.5, (double)checkPos.getY(), (double)checkPos.getZ() + 0.5);
                                if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
                                    TheWitch.LOGGER.info("[The Witch] Found a valid teleport position (upward scan): {}", (Object)finalPos);
                                }
                                this.level.playSound(null, checkPos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.0f, 1.0f);
                                this.teleportTo(finalPos.x, finalPos.y, finalPos.z);
                                break;
                            }
                        }
                    } else {
                        this.teleportTo(player.getX(), player.getY(), player.getZ());
                        this.doHurtTarget(player);
                        if (((Boolean)WitchModConfig.PLAY_ATTACK_SOUND.get()).booleanValue()) {
                            this.level.playSound(null, this.blockPosition(), ModSounds.WITCH_ATTACK.get(), SoundSource.HOSTILE, 0.8f, 1.0f);
                        }
                    }
                }
                if (this.triggeredTicks >= 600) {
                    this.discard();
                    if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
                        TheWitch.LOGGER.info("[The Witch] Despawning witch (triggered).");
                    }
                    this.teleportTarget = null;
                    return;
                }
            } else {
                if (player.hasLineOfSight(this) && this.isPlayerLookingAtMe(player)) {
                    ++this.stareTicks;
                    if (this.stareTicks >= (Integer)WitchModConfig.STARE_TICKS.get()) {
                        this.triggerScareSequence(player);
                    }
                } else {
                    this.stareTicks = 0;
                }
                if (((Boolean)WitchModConfig.PLAY_PROXIMITY_SOUND.get()).booleanValue() && (double)this.distanceTo(player) < 15.0 && this.ambientSoundCooldown <= 0) {
                    this.level.playSound(null, this.blockPosition(), ModSounds.WITCH_AMBIENT.get(), SoundSource.HOSTILE, 0.5f, 1.0f);
                    this.ambientSoundCooldown = 1200;
                }
            }
        }
        if (this.ambientSoundCooldown > 0) {
            --this.ambientSoundCooldown;
        }
        if (!this.isTriggered && this.existenceTicks >= 6000) {
            if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
                TheWitch.LOGGER.info("[The Witch] Despawning witch (not triggered).");
            }
            this.discard();
        }
    }

    private void triggerScareSequence(Player player) {
        this.isTriggered = true;
        this.triggeredTicks = 0;
        player.displayClientMessage(Component.literal("\u00a74The Witch approaches..."), true);
        this.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 2.0f, 1.0f);
        this.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0f, 1.0f);
        this.level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.5f, 1.0f);
        if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
            TheWitch.LOGGER.info("[The Witch] Starting the light break sequence...");
        }
        this.breakNearbyLights(player);
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 400, 2, false, false));
        if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
            TheWitch.LOGGER.info("[The Witch] Scare sequence completed.");
        }
    }

    private boolean isPlayerLookingAtMe(Player player) {
        double hitDistance;
        Vec3 end;
        Vec3 direction;
        Vec3 vec3 = player.getViewVector(1.0f).normalize();
        double dot = vec3.dot(direction = this.position().subtract(player.position()).normalize());
        if (dot <= 0.99) {
            return false;
        }
        double distance = this.distanceTo(player);
        Vec3 start = player.getEyePosition(1.0f);
        BlockHitResult rayTraceResult = this.level.clip(new ClipContext(start, end = start.add(vec3.scale(Math.min(distance * 1.5, 32.0))), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return rayTraceResult.getType() != HitResult.Type.BLOCK || !((hitDistance = start.distanceTo(rayTraceResult.getLocation())) < distance - 1.0);
    }

    private void breakNearbyLights(Player player) {
        BlockPos pos = player.blockPosition();
        int radius = (Integer)WitchModConfig.LIGHT_BREAK_RADIUS.get();
        BlockPos.betweenClosedStream(pos.offset(-radius, -20, -radius), pos.offset(radius, 20, radius)).filter(blockPos -> this.level.hasChunkAt(blockPos)).forEach(blockPos -> {
            BlockState state = this.level.getBlockState(blockPos);
            if (this.isBreakableLightBlock(state)) {
                this.level.destroyBlock(blockPos, false);
            }
        });
    }

    private boolean isBreakableLightBlock(BlockState state) {
        int lightLevel = state.getLightEmission();
        if (lightLevel <= 0) {
            return false;
        }
        Block block = state.getBlock();
        return block != Blocks.END_PORTAL && block != Blocks.END_GATEWAY && block != Blocks.NETHER_PORTAL && block != Blocks.BEACON && block != Blocks.LAVA && block != Blocks.FIRE && block != Blocks.SOUL_FIRE && block != Blocks.BEDROCK && block != Blocks.FURNACE && block != Blocks.BLAST_FURNACE && block != Blocks.SMOKER;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (source.getEntity() instanceof Player) {
            WitchSpawnHandler.resetTimer();
            if (result && ((Boolean)WitchModConfig.PLAY_SCREAM_SOUND.get()).booleanValue()) {
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.WITCH_SCREAM.get(), this.getSoundSource(), 0.7f, 1.0f);
            }
            if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
                TheWitch.LOGGER.info("[The Witch] Received damage from player - despawning.");
            }
            this.discard();
        }
        return result;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && ((Boolean)WitchModConfig.PLAY_ATTACK_SOUND.get()).booleanValue()) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.WITCH_ATTACK.get(), this.getSoundSource(), 0.8f, 1.0f);
        }
        if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
            TheWitch.LOGGER.info("[The Witch] Attacked target.");
        }
        return result;
    }

    @Override
    public Component getName() {
        return Component.literal("The Witch");
    }
}
