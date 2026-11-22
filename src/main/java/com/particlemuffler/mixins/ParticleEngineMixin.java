package com.particlemuffler.mixins;

import com.particlemuffler.utils.ParticleFilter;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Every particle spawned through {@code Level#addParticle}, level events, emitters and most mods
 * ends up in {@link ParticleEngine#createParticle}. Block breaking / hitting effects are handled
 * in {@link ClientLevelMixin}.
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
}
