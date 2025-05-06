package com.jacobdavis.thewitch.entity;

// Project imports...
import com.jacobdavis.thewitch.world.WitchSpawnHandler;
import com.jacobdavis.thewitch.TheWitch;
import com.jacobdavis.thewitch.config.WitchModConfig;
import com.jacobdavis.thewitch.registry.ModSounds;

// Minecraft imports...
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * The primary Stalker Witch entity in the game.
 */
public class StalkerWitchEntity extends Monster {
    private int stareTicks = 0; //
    private int triggeredTicks = 0;
    private int existenceTicks = 0;
    private int ambientSoundCooldown = 0;
    private boolean initialized = false;
    private boolean isTriggered = false;

    /**
     * Constructor for the Stalker Witch entity.
     *
     * @param type  The entity type.
     * @param world The world in which the entity exists.
     */
    public StalkerWitchEntity(EntityType<? extends Monster> type, Level world) {
        super(type, world);
        this.setPersistenceRequired(); // So it doesn't despawn randomly
    }

    /**
     * Called when the entity is first spawned.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); // Helps with water behavior - aka, she doens't sink.
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
    }

    /**
     * Creates the attributes for the Stalker Witch entity.
     *
     * @return The attribute supplier builder.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D);
    }

    /**
     * Called every tick to update the witch's state.
     */
    @Override
    public void tick() {
        super.tick();

        if (!initialized) {
            if (this.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
                initialized = true;
            } else {
                return;
            }
        }

        if (this.level.isClientSide)
            return;

        existenceTicks++; // Always increment

        Player player = this.level.getNearestPlayer(this, 256);
        if (player != null) {
            this.lookAt(EntityAnchorArgument.Anchor.EYES, player.position());

            if (isTriggered) {
                this.getNavigation().moveTo(player, WitchModConfig.WITCH_SPEED.get());
                triggeredTicks++;

                if (this.distanceToSqr(player) < 2.5D) {
                    this.doHurtTarget(player);
                }

                // Despawn after 20 seconds if triggered
                if (triggeredTicks >= 400) {
                    this.discard();
                    if (WitchModConfig.ENABLE_LOGGING.get()) TheWitch.LOGGER.info("[The Witch] Despawning witch (triggered).");
                    return;
                }

            } else {
                if (player.hasLineOfSight(this) && isPlayerLookingAtMe(player)) {
                    stareTicks++;
                    if (stareTicks >= WitchModConfig.STARE_TICKS.get()) {
                        triggerScareSequence(player);
                    }
                } else {
                    stareTicks = 0;
                }

                if (WitchModConfig.PLAY_PROXIMITY_SOUND.get()) {
                    if (this.distanceTo(player) < 15.0 && ambientSoundCooldown <= 0) {
                        this.level.playSound(null, this.blockPosition(), ModSounds.WITCH_AMBIENT.get(),
                                SoundSource.HOSTILE, 0.5F, 1.0F);
                        ambientSoundCooldown = 1200;
                    }
                }
            }
        }

        if (ambientSoundCooldown > 0) {
            ambientSoundCooldown--;
        }

        // Despawn after 5 minutes if NOT triggered
        if (!isTriggered && existenceTicks >= 6000) {
            if (WitchModConfig.ENABLE_LOGGING.get()) TheWitch.LOGGER.info("[The Witch] Despawning witch (not triggered).");
            this.discard();
        }
    }

    /**
     * Called from the tick() method to start the scare sequence.
     * 
     * @param player The player who triggered the scare sequence.
     */
    private void triggerScareSequence(Player player) {
        isTriggered = true;
        triggeredTicks = 0;
        player.displayClientMessage(Component.literal("§4The Witch approaches..."), true);

        // Play sound effects (Thunderclap and Warden heartbeat)
        this.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 2.0F, 1.0F);
        this.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0F, 1.0F);
        this.level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.5F, 1.0F);
        
        // Break nearby light sources
        breakNearbyLights(player);

        // Apply darkness effect
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 400, 2, false, false));
        if (WitchModConfig.ENABLE_LOGGING.get()) TheWitch.LOGGER.info("[The Witch] Scare sequence completed.");
    }

    /**
     * Checks if the player is looking at the witch.
     *
     * @param player The player to check.
     * @return True if the player is looking at the witch, false otherwise.
     */
    private boolean isPlayerLookingAtMe(Player player) {
        // First, check if player is facing the witch with the dot product...
        Vec3 vec3 = player.getViewVector(1.0F).normalize();
        Vec3 direction = this.position().subtract(player.position()).normalize();
        double dot = vec3.dot(direction);

        // If not generally looking in that direction, return false immediately
        if (dot <= 0.99D)
            return false;

        // Add a raycast to check if the player is looking directly at this entity
        double distance = this.distanceTo(player);
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 end = start.add(vec3.scale(Math.min(distance * 1.5, 32.0D)));

        // Perform raycast to check what the player is actually looking at
        var rayTraceResult = this.level.clip(new ClipContext(
                start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player));

        // If the raycast hit a block before reaching the witch, player isn't looking at
        // the witch
        if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
            // Calculate distance to the hit block
            double hitDistance = start.distanceTo(rayTraceResult.getLocation());

            // If the hit block is closer than the witch, player isn't looking at the witch
            if (hitDistance < distance - 1.0D) {
                return false;
            }
        }

        return true;
    }

    /**
     * Breaks nearby light sources within a specified radius.
     */
    private void breakNearbyLights(Player player) {
        BlockPos pos = player.blockPosition();
        int radius = WitchModConfig.LIGHT_BREAK_RADIUS.get();

        BlockPos.betweenClosedStream(
                pos.offset(-radius, -20, -radius),
                pos.offset(radius, 20, radius)).forEach(blockPos -> {
                    BlockState state = this.level.getBlockState(blockPos);
                    if (isBreakableLightBlock(state)) {
                        this.level.destroyBlock(blockPos, false);
                    }
                });
    }

    /**
     * Checks if the block state is a breakable light block.
     *
     * @param state The block state to check.
     * @return True if the block is breakable, false otherwise.
     */
    private boolean isBreakableLightBlock(BlockState state) {
        return state.is(Blocks.TORCH)
                || state.is(Blocks.WALL_TORCH)
                || state.is(Blocks.SOUL_TORCH)
                || state.is(Blocks.SOUL_WALL_TORCH)
                || state.is(Blocks.REDSTONE_TORCH)
                || state.is(Blocks.REDSTONE_WALL_TORCH)
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN)
                || state.is(Blocks.CAMPFIRE)
                || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.END_ROD)
                || state.is(Blocks.GLOWSTONE)
                || state.is(Blocks.SEA_LANTERN)
                || state.is(Blocks.SHROOMLIGHT)
                || state.is(Blocks.JACK_O_LANTERN)
                || state.is(Blocks.VERDANT_FROGLIGHT)
                || state.is(Blocks.PEARLESCENT_FROGLIGHT)
                || state.is(Blocks.OCHRE_FROGLIGHT)
                || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.LARGE_AMETHYST_BUD)
                || state.is(Blocks.MEDIUM_AMETHYST_BUD)
                || state.is(Blocks.SMALL_AMETHYST_BUD)
                || state.is(Blocks.GLOW_LICHEN)
                || state.is(Blocks.GLOWSTONE);
    }

    /**
     * Called when the entity is hurt.
     *
     * @param source The damage source.
     * @param amount The amount of damage.
     * @return True if the entity was hurt, false otherwise.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);

        if (source.getEntity() instanceof Player) {
            WitchSpawnHandler.resetTimer(); // Reset the spawn timer

            // Scream sound - play directly to the targeted player
            if (result && WitchModConfig.PLAY_SCREAM_SOUND.get()) {
                // Play attack sound
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.WITCH_SCREAM.get(), // or create a new attack sound
                        this.getSoundSource(),
                        0.7F, // Volume
                        1.0F // Pitch
                );
            }
            if (WitchModConfig.ENABLE_LOGGING.get()) TheWitch.LOGGER.info("[The Witch] Received damage from player - despawning.");
            this.discard(); // Despawn
        }
        return result;
    }

    /**
     * Called when the entity is hurt by a target.
     *
     * @param target The target entity.
     * @return True if the target was hurt, false otherwise.
     */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);

        if (result && WitchModConfig.PLAY_ATTACK_SOUND.get()) {
            // Play attack sound
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.WITCH_ATTACK.get(), // or create a new attack sound
                    this.getSoundSource(),
                    0.8F, 1.0F);
        }
        if (WitchModConfig.ENABLE_LOGGING.get()) TheWitch.LOGGER.info("[The Witch] Attacked target.");
        return result;
    }

    /**
     * Provides a nice display name for the entity.
     */
    @Override
    public Component getDisplayName() {
        return Component.literal("The Witch");
    }

}