package me.crec.spawndebugging.mixin;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;



@Mixin(SpawnHelper.class)
public abstract class SpawnDebuggingMixin {
    @Shadow
    private static BlockPos method_37843(World world, WorldChunk worldChunk, int i) {
        return null;
    }

    @Shadow
    public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, SpawnHelper.Checker checker, SpawnHelper.Runner runner) {}

    /**
     * @author Crec0
     * @reason mojank jank
     */
    @Overwrite
    public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner) {
        for (int i = world.getTopY() - 16; i >= world.getBottomY(); i -= 16) {
            ChunkSection chunkSection = chunk.getSectionArray()[chunk.getSectionIndex(i)];
            if (chunkSection != WorldChunk.EMPTY_SECTION && !chunkSection.isEmpty()) {
                BlockPos blockPos = method_37843(world, chunk, i);
                int playerChunkY = world.getClosestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), -1.0D, false).getBlockY() >> 4;
                if (!(world.getRandom().nextFloat() > getSpawnChance((i >> 4 - playerChunkY) >> 3))) {
                    spawnEntitiesInChunk(group, world, chunk, blockPos, checker, runner);
                }
            }
        }
    }

    private static float getSpawnChance(int offset){
        return Math.max(0.0F, (1 - offset * offset) / 4.0F);
    }
}