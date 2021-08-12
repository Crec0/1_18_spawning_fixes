package me.crec.spawndebugging.mixin;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(SpawnHelper.class)
public abstract class SpawnDebuggingMixin {

    @Shadow
    private static BlockPos method_37843(World world, WorldChunk worldChunk, int i);

    @Shadow
    public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner) {
        boolean bl = true;

        for(int i = world.getTopY() - 16; i >= world.getBottomY(); i -= 16) {
            ChunkSection chunkSection = chunk.getSectionArray()[chunk.getSectionIndex(i)];
            if ((bl || chunkSection != WorldChunk.EMPTY_SECTION) && !chunkSection.isEmpty()) {
                bl = false;
                if (!(world.getRandom().nextFloat() > 0.24F)) {
                    BlockPos blockPos = SpawnHelper.method_37843(world, chunk, i);
                    spawnEntitiesInChunk(group, world, chunk, blockPos, checker, runner);
                }
            }
        }

    }
}

//0543e1ef5f0344e683c1b03f2bc3e8cd