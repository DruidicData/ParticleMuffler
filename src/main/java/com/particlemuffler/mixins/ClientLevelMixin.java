package com.particlemuffler.mixins;

import com.particlemuffler.utils.ParticleFilter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Block breaking and block hitting effects build their particles directly instead of going through
 * {@code ParticleEngine#createParticle}, so they are filtered here as {@code minecraft:block}.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "addDestroyBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"), cancellable = true)
    private void particlemuffler$filterDestroy(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!ParticleFilter.shouldSpawn(ParticleTypes.BLOCK, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)) {
            ci.cancel();
        }
    }

    //the two-argument overload delegates to this one
    @Inject(method = "addBreakingBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/phys/HitResult;)V",
            at = @At("HEAD"), cancellable = true)
    private void particlemuffler$filterCrack(BlockPos pos, Direction direction, @Nullable HitResult hitResult, CallbackInfo ci) {
        if (!ParticleFilter.shouldSpawn(ParticleTypes.BLOCK, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)) {
            ci.cancel();
        }
    }
}
