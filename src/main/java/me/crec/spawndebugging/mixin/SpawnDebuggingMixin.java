package me.crec.spawndebugging.mixin;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(SpawnHelper.class)
public class SpawnDebuggingMixin {

    @Unique
    private static final String SPAWN_ENTITIES_IN_CHUNK = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V";

    @Unique
    private static boolean blField;

    @Unique
    private static ChunkSection chunkSectionField;

    @Inject(
            method = SPAWN_ENTITIES_IN_CHUNK,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;getSectionIndex(I)I",
                    shift = At.Shift.BY,
                    by = 3
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void captureVariables(SpawnGroup group, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo ci, boolean bl, int i, ChunkSection chunkSection){
        blField = bl;
        chunkSectionField = chunkSection;
    }

    @Redirect(method = SPAWN_ENTITIES_IN_CHUNK,
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/world/chunk/ChunkSection.isEmpty()Z"
            )
    )
    private static boolean falsifyIsEmpty(ChunkSection chunkSection){
        return true;
    }

    @ModifyVariable(
            method = SPAWN_ENTITIES_IN_CHUNK,
            at = @At("LOAD"),
            index = 5
    )

    private static boolean setCondition(boolean bl){
        if (chunkSectionField != null){
            return !(chunkSectionField != WorldChunk.EMPTY_SECTION && !chunkSectionField.isEmpty());
        }
        return true;
    }

    @ModifyVariable(
            method = SPAWN_ENTITIES_IN_CHUNK,
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;getSectionIndex(I)I")),
            at = @At(value = "JUMP", ordinal = 0),
            index = 5
    )
    private static boolean returnBooleanField(boolean bl){
        return blField;
    }
}