package me.crec.spawnboost.mixin;

import me.crec.spawnboost.SpawnBoost;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(SpawnHelper.class)
public abstract class SpawnBoostMixin {

    @Shadow
    private static BlockPos method_37843(World world, WorldChunk worldChunk, int i) {
        return (BlockPos) BlockPos.ZERO;
    }

    @Shadow
    public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, SpawnHelper.Checker checker, SpawnHelper.Runner runner) {
    }

    @Inject(method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V", at = @At("HEAD"), cancellable = true)
    private static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo ci) {
        if (SpawnBoost.vanillaPlusPlusSpawning) {
            for (ChunkSection chunkSection: chunk.getSectionArray()){
                if (chunkSection != WorldChunk.EMPTY_SECTION && !chunkSection.isEmpty()) {
                    int yOffset = chunkSection.getYOffset();
                    BlockPos blockPos = method_37843(world, chunk, yOffset);
                    int playerChunkY = world.getClosestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), -1.0D, false).getBlockY() >> 4;
                    if (!(world.getRandom().nextFloat() > getSpawnChance(yOffset, playerChunkY))) {
                        spawnEntitiesInChunk(group, world, chunk, blockPos, checker, runner);
                    }
                }
            }
            ci.cancel();
        }
    }

    private static float getSpawnChance(int subChunkOffset, int playerSubChunkY) {
        int adjusted = (subChunkOffset >> 4 - playerSubChunkY) >> 3;
        return adjusted >= -8 && adjusted <= 8 ? SpawnBoost.SPAWN_PERCENTAGES[adjusted + 8] : 0.0F;
    }
}