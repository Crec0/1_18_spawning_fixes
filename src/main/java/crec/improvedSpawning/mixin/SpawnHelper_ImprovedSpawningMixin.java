package crec.improvedSpawning.mixin;

import crec.improvedSpawning.ImprovedSpawning;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;


@Mixin(SpawnHelper.class)
public abstract class SpawnHelper_ImprovedSpawningMixin {

    @Unique
    private static BlockPos blockPos = null;

    @Shadow
    private static @Nullable BlockPos method_37843(World world, WorldChunk worldChunk, int i) {
        return null;
    }

    @Shadow
    public static void spawnEntitiesInChunk(
            SpawnGroup group,
            ServerWorld world,
            Chunk chunk,
            BlockPos pos,
            SpawnHelper.Checker checker,
            SpawnHelper.Runner runner
    ) {
    }

    @Inject(
            method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void selectSpawningPosInChunkOverride(
            SpawnGroup group,
            ServerWorld world,
            WorldChunk chunk,
            SpawnHelper.Checker checker,
            SpawnHelper.Runner runner,
            CallbackInfo ci
    ) {
        switch (ImprovedSpawning.activeAlgorithm) {
            case VANILLA_EMPTY_SUBCHUNK_OPTIMIZATION -> {
                for(int i = world.getTopY() - 16; i >= world.getBottomY(); i -= 16) {
                    ChunkSection chunkSection = chunk.getSectionArray()[chunk.getSectionIndex(i)];
                    if (chunkSection != WorldChunk.EMPTY_SECTION && !chunkSection.isEmpty()) {
                        if (!(world.getRandom().nextFloat() > 0.24F)) {
                            BlockPos blockPos = method_37843(world, chunk, i);
                            spawnEntitiesInChunk(group, world, chunk, blockPos, checker, runner);
                        }
                    }
                }
                ci.cancel();
            }
            case VANILLA_OLD_SPAWNING -> {
                blockPos = getSpawnPos(world, chunk);
                if (blockPos.getY() >= world.getBottomY() + 1) {
                    spawnEntitiesInChunk(group, world, chunk, blockPos, checker, runner);
                }
                ci.cancel();
            }
            case DISTRIBUTION_BASED_SPAWNING -> {
                for (ChunkSection chunkSection : chunk.getSectionArray()) {
                    if (chunkSection != WorldChunk.EMPTY_SECTION && !chunkSection.isEmpty()) {
                        int yOffset = chunkSection.getYOffset();
                        blockPos = method_37843(world, chunk, yOffset);
                        int playerChunkY = Objects.requireNonNull(world.getClosestPlayer(
                                blockPos.getX(),
                                blockPos.getY(),
                                blockPos.getZ(),
                                -1.0D,
                                false
                        )).getBlockY() >> 4;
                        if (!(world.getRandom().nextFloat() > getSpawnChance(yOffset, playerChunkY))) {
                            spawnEntitiesInChunk(group, world, chunk, blockPos, checker, runner);
                        }
                    }
                }
                ci.cancel();
            }
        }
    }

    @Redirect(
            method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/world/chunk/Chunk.sampleHeightmap(Lnet/minecraft/world/Heightmap$Type;II)I"
            )
    )
    private static int voidHeightmapCall(Chunk chunk, Heightmap.Type type, int x, int z) {
        if (ImprovedSpawning.activeAlgorithm == ImprovedSpawning.SpawningAlgorithm.VANILLA_OLD_SPAWNING && blockPos != null){
            return blockPos.getY();
        }
        return chunk.sampleHeightmap(type, x, z);
    }

    private static BlockPos getSpawnPos(ServerWorld world, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.getStartX() + world.random.nextInt(16);
        int j = chunkPos.getStartZ() + world.random.nextInt(16);
        int k = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, i, j) + 1;
        int l = MathHelper.nextBetween(world.random, world.getBottomY(), k);
        return new BlockPos(i, l, j);
    }

    private static float getSpawnChance(int subChunkOffset, int playerSubChunkY) {
        int chunkDiff = Math.abs((subChunkOffset >> 4 - playerSubChunkY) >> 3);
        return chunkDiff <= 8 ? ImprovedSpawning.SPAWN_PERCENTAGES[chunkDiff] : 0.0F;
    }
}