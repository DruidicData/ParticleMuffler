package com.particlemuffler.utils;

import com.particlemuffler.Config;
import com.particlemuffler.gui.MufflerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.concurrent.ThreadLocalRandom;

/** Decides whether a particle is allowed to spawn. Called from {@code ParticleEngineMixin}. */
public final class ParticleFilter {

    /** Set while the preview button spawns particles, so previews are never muffled or recorded. */
    private static boolean bypass = false;

    private ParticleFilter() {}

    /** @return true if the particle should be spawned. */
    public static boolean shouldSpawn(ParticleType<?> type, double x, double y, double z) {
        if (bypass) return true;

        Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
        if (id == null) return true;

        if (!ParticleLists.isForbidden(id)) {
            ParticleLists.addRecent(id);
        }

        if (!MufflerScreen.isMuffling()) return true;

        double density = getDensity(id, x, y, z);
        if (density >= 1D) return true;
        if (density <= 0D) return false;
        return ThreadLocalRandom.current().nextDouble() < density;
    }

    /** @return the fraction (0..1) of particles of this type that may spawn at that position. */
    private static double getDensity(Identifier id, double x, double y, double z) {
        Double global = ParticleLists.muffledParticles.get(id);
        if (global != null) return global;

        if (!Config.disableAnchors.get()) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                double anchor = Anchor.getDensity(id, level.dimension().identifier(), x, y, z);
                if (anchor < 1D) return anchor;
            }
        }

        if (!ParticleLists.modsMuffled.isEmpty()) {
            Double mod = ParticleLists.modsMuffled.get(id.getNamespace());
            if (mod != null) return mod;
        }
        return 1D;
    }

    public static void runBypassed(Runnable action) {
        boolean old = bypass;
        bypass = true;
        try {
            action.run();
        } finally {
            bypass = old;
        }
    }
}
