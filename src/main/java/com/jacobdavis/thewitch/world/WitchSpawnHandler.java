package com.jacobdavis.thewitch.world;

// Project Imports...
import com.jacobdavis.thewitch.TheWitch;
import com.jacobdavis.thewitch.config.WitchModConfig;
import com.jacobdavis.thewitch.entity.StalkerWitchEntity;
import com.jacobdavis.thewitch.registry.ModEntities;

// Minecraft Imports...
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Java Imports...
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * WitchSpawnHandler class for managing the spawning of the witch.
 * This class handles the logic for spawning the witch in the game world.
 */
@Mod.EventBusSubscriber(modid="thewitch")
public class WitchSpawnHandler {
    private static int tickCounter = 0;
    private static int spawnCycleCount = 0;
    private static int booksSpawned = 0;
    private static final List<String[]> shuffledJournals = new ArrayList<String[]>();
    private static final Random random = new Random();

    /**
     * Get the spawn interval in ticks from the mod config.
     *
     * @return The spawn interval in ticks, based on the value pulled from the config.
     */
    private static int getSpawnInterval() {
        return (Integer)WitchModConfig.SPAWN_INTERVAL_TICKS.get();
    }

    /**
     * This method is called every server tick.
     * It checks if the spawn interval has been reached and attempts to spawn a witch
     * near players.
     *
     * @param event
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (++tickCounter >= WitchSpawnHandler.getSpawnInterval()) {
            tickCounter = 0;
            MinecraftServer server = event.getServer();
            ServerLevel level = server.getLevel(Level.OVERWORLD);
            if (level != null) {
                for (ServerPlayer player : level.players()) {
                    boolean alreadyExists = level.getEntities(ModEntities.STALKER_WITCH.get(), player.getBoundingBox().inflate(64.0), entity -> true).size() > 0;
                    if (alreadyExists) continue;
                    boolean spawned = false;
                    for (int attempt = 0; attempt < 30 && !spawned; ++attempt) {
                        spawned = WitchSpawnHandler.trySpawnWitchNearPlayer(player, level);
                        if (spawned) continue;
                    }
                    if (spawned || !((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) continue;
                    TheWitch.LOGGER.info("[The Witch] All spawn attempts failed. Will retry at the next interval.");
                }
            }
        }
    }

    /**
     * Try to spawn a witch near the player.
     *
     * @param player
     * @param level
     * @return true if the witch was spawned, false otherwise.
     */
    private static boolean trySpawnWitchNearPlayer(ServerPlayer player, ServerLevel level) {
        WitchSpawnHandler.initializeJournals();
        int radiusMin = 20;
        int radiusMax = 80;
        double angle = random.nextDouble() * 2.0 * Math.PI;
        int offsetX = (int)(Math.cos(angle) * (double)(radiusMin + random.nextInt(radiusMax - radiusMin)));
        int offsetZ = (int)(Math.sin(angle) * (double)(radiusMin + random.nextInt(radiusMax - radiusMin)));
        BlockPos basePos = player.blockPosition().offset(offsetX, 0, offsetZ);
        BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, basePos);
        boolean isSkyVisible = level.canSeeSky(spawnPos);
        boolean isSolidGround = level.getBlockState(spawnPos.below()).getMaterial().isSolid();
        boolean isLiquid = level.getBlockState(spawnPos).getMaterial().isLiquid();
        if (!isSkyVisible || !isSolidGround || isLiquid) {
            return false;
        }
        if (!WitchSpawnHandler.verifyGroundForRitual(level, spawnPos)) {
            return false;
        }
        if (!WitchSpawnHandler.canPlaceAllCandles(level, spawnPos)) {
            return false;
        }
        WitchSpawnHandler.clearAreaAboveGround(level, spawnPos);
        if (((Boolean)WitchModConfig.SPAWN_CANDLES.get()).booleanValue()) {
            WitchSpawnHandler.placeRitualCandles(level, spawnPos);
        }
        if (++spawnCycleCount % 2 == 0 && booksSpawned < 5) {
            WitchSpawnHandler.dropLoreBooks(level, spawnPos);
            ++booksSpawned;
            player.displayClientMessage(Component.literal("\u00a74Something lost calls to you..."), true);
            if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
                TheWitch.LOGGER.info("[The Witch] Spawned lore book drop at " + String.valueOf(spawnPos));
            }
            return true;
        }
        StalkerWitchEntity witch = (StalkerWitchEntity)ModEntities.STALKER_WITCH.get().create(level);
        if (witch != null) {
            witch.moveTo(spawnPos, 0.0f, 0.0f);
            level.addFreshEntity(witch);
            if (random.nextFloat() < 0.7f) {
                level.setWeatherParameters(0, 6000, true, true);
                if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
                    TheWitch.LOGGER.info("[The Witch] Thunderstorm has begun!");
                }
            }
            player.displayClientMessage(Component.literal("\u00a74An antiquated being is watching..."), true);
            if (((Boolean)WitchModConfig.ENABLE_LOGGING.get()).booleanValue()) {
                TheWitch.LOGGER.info("[The Witch] Successfully spawned witch at " + String.valueOf(spawnPos));
            }
            return true;
        }
        return false;
    }

    /**
     * Verify that the area has suitable ground for the ritual.
     * Checks if there's solid ground at the appropriate positions.
     *
     * @param level
     * @param spawnPos
     * @return true if the ground is suitable, false otherwise.
     */
    private static boolean verifyGroundForRitual(ServerLevel level, BlockPos spawnPos) {
        if (!level.getBlockState(spawnPos.below()).getMaterial().isSolid()) {
            return false;
        }
        for (BlockPos pos : getCandlePositions(spawnPos)) {
            BlockPos below = pos.below();
            if (level.getBlockState(below).getMaterial().isSolid()) continue;
            return false;
        }
        return true;
    }

    /**
     * Check if all candles can be placed in their intended positions.
     *
     * @param level
     * @param spawnPos
     * @return true if all candles can be placed, false otherwise.
     */
    private static boolean canPlaceAllCandles(ServerLevel level, BlockPos spawnPos) {
        for (BlockPos pos : getCandlePositions(spawnPos)) {
            BlockPos below = pos.below();
            if (!level.getBlockState(below).getMaterial().isSolid()) {
                return false;
            }
            BlockState currentState = level.getBlockState(pos);
            if (currentState.getMaterial().isReplaceable() || currentState.isAir()) continue;
            return false;
        }
        return true;
    }

    /**
     * Clear area but preserve the ground layer.
     *
     * @param level
     * @param spawnPos
     */
    private static void clearAreaAboveGround(ServerLevel level, BlockPos spawnPos) {
        int clearRadius = 3;
        for (int dx = -clearRadius; dx <= clearRadius; ++dx) {
            for (int dz = -clearRadius; dz <= clearRadius; ++dz) {
                for (int dy = 0; dy <= 2; ++dy) {
                    BlockPos clearPos = spawnPos.offset(dx, dy, dz);
                    if (dy <= 0 && level.getBlockState(clearPos.below()).getMaterial().isSolid()) continue;
                    level.setBlockAndUpdate(clearPos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    /**
     * Place candles in a cross formation, the pentagram wasn't working.
     *
     * @param level
     * @param spawnPos
     */
    private static void placeRitualCandles(ServerLevel level, BlockPos spawnPos) {
        for (BlockPos pos : getCandlePositions(spawnPos)) {
            BlockPos below = pos.below();
            if (!level.getBlockState(below).getMaterial().isSolid()) continue;
            int candleCount = 1 + random.nextInt(4);
            BlockState candleState = Blocks.RED_CANDLE.defaultBlockState()
                    .setValue(CandleBlock.LIT, true)
                    .setValue(CandleBlock.CANDLES, candleCount);
            level.setBlockAndUpdate(pos, candleState);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, (double)pos.getX() + 0.5, (double)pos.getY() + 0.2, (double)pos.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.01);
        }
    }

    /**
     * Get the positions where candles should be placed in a cross formation.
     *
     * @param spawnPos
     * @return An array of BlockPos representing the candle positions.
     */
    private static BlockPos[] getCandlePositions(BlockPos spawnPos) {
        return new BlockPos[]{spawnPos.north(1), spawnPos.north(2), spawnPos.south(1), spawnPos.east(1), spawnPos.west(1)};
    }

    /**
     * Initialize the journals and shuffle them.
     * This method is called once to set up the journal data.
     */
    private static void initializeJournals() {
        if (!shuffledJournals.isEmpty()) {
            return;
        }
        List<String[]> journals = List.of(new String[]{"Journal I", "Mother\u2019s cough has worsened.\nNo healer in the village will help without emeralds.\nBut I\u2019ve found a tome in the woods \u2014 strange, old, beautiful.\nIf I can learn... perhaps I can save her.\nShe is my light, she must be kept alive."}, new String[]{"Journal II", "This tome saved mother - she glows with life. I healed a child today.\nThe fever broke before dawn.\nThey say I\u2019m blessed.\nThe book... it sings of more.\nOf how to command life itself.\nI feel a desire to listen..."}, new String[]{"Journal III", "The sky burned red.\nThey dragged Mother from our home.\nShe screamed as they lit the thatch, cursing her.\nI was in the woods\u2014gathering mushrooms. I was too late.\nThey say I\u2019ve cursed the village.\nThey fear what they do not understand. I've become exiled."}, new String[]{"Journal IV", "I walk now where the sun doesn\u2019t reach.\nTheir fires took everything.\nI don\u2019t dream anymore.\nThe dark, the cold welcomes me.\nI see the world as it truly is\u2014\nCruel, and dim.\nI have found new light in the shadows. My new home - I shall protect it."}, new String[]{"Journal V", "I see - they build again.\nThey forget the blood beneath the roots, my scars.\nI am still here.\nThey look into the trees and feel fear,\nbut do not know why.\nLet them see.\nI will remind them. I will take their light."});
        shuffledJournals.addAll(journals);
        Collections.shuffle(shuffledJournals);
    }

    /**
     * Drop the lore books at the specified position.
     *
     * @param level
     * @param centerPos
     */
    private static void dropLoreBooks(ServerLevel level, BlockPos centerPos) {
        if (booksSpawned >= shuffledJournals.size()) {
            return;
        }
        String[] bookData = shuffledJournals.get(booksSpawned);
        String title = bookData[0];
        String content = bookData[1];
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = new CompoundTag();
        tag.putString("title", title);
        tag.putString("author", "Unknown");
        ListTag pages = new ListTag();
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(content))));
        tag.put("pages", pages);
        book.setTag(tag);
        ItemEntity entity = new ItemEntity(level, (double)centerPos.getX() + 0.5, (double)(centerPos.getY() + 1), (double)centerPos.getZ() + 0.5, book);
        entity.setUnlimitedLifetime();
        entity.setInvulnerable(true);
        level.addFreshEntity(entity);
    }

    /**
     * This method is used to reset the tick counter.
     */
    public static void resetTimer() {
        tickCounter = 0;
    }
}
