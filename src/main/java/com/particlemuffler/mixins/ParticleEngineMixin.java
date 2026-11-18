package com.particlemuffler.mixins;

import com.particlemuffler.utils.ParticleFilter;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Every particle spawned through {@code Level#addParticle}, {@code LevelRenderer} level events,
 * emitters and most mods ends up in {@link ParticleEngine#createParticle}. Block breaking / hitting
 * effects bypass it and build their particles directly, so those two methods are hooked too.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void particlemuffler$filterParticle(ParticleOptions options, double x, double y, double z,
                                                double xSpeed, double ySpeed, double zSpeed,
                                                CallbackInfoReturnable<Particle> cir) {
        if (!ParticleFilter.shouldSpawn(options.getType(), x, y, z)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "destroy(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"), cancellable = true)
    private void particlemuffler$filterDestroy(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!ParticleFilter.shouldSpawn(ParticleTypes.BLOCK, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)) {
            ci.cancel();
        }
    }

    @Inject(method = "crack(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V",
            at = @At("HEAD"), cancellable = true)
    private void particlemuffler$filterCrack(BlockPos pos, Direction side, CallbackInfo ci) {
        if (!ParticleFilter.shouldSpawn(ParticleTypes.BLOCK, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)) {
            ci.cancel();
        }
    }
}
