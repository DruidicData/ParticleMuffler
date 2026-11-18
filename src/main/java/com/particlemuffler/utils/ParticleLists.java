package com.particlemuffler.utils;

import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Shared state, the particle equivalent of Extreme Sound Muffler's ISoundLists. */
public final class ParticleLists {
    /** Substrings from the config; matching particles are hidden from the Recent list. */
    public static final Set<String> forbiddenParticles = new HashSet<>();
    public static final Map<ResourceLocation, Boolean> forbiddenCache = new ConcurrentHashMap<>();
    /** mod id -> density (0..1) from the config. */
    public static final Map<String, Double> modsMuffled = new ConcurrentHashMap<>();
    /** Particles seen recently, oldest first. Guarded by itself. */
    public static final LinkedHashSet<ResourceLocation> recentParticles = new LinkedHashSet<>();
    /** Globally muffled particles: id -> density (0 = never shown, 0.9 = 90% shown). */
    public static final Map<ResourceLocation, Double> muffledParticles = new ConcurrentHashMap<>();
    public static final List<Anchor> anchorList = new ArrayList<>();

    private ParticleLists() {}

    public static void addRecent(ResourceLocation id) {
        synchronized (recentParticles) {
            // re-insert so the most recent one is always last; LinkedHashSet makes this O(1)
            recentParticles.remove(id);
            recentParticles.add(id);
        }
    }

    public static List<ResourceLocation> recentSnapshot() {
        synchronized (recentParticles) {
            return new ArrayList<>(recentParticles);
        }
    }

    public static void clearRecent() {
        synchronized (recentParticles) {
            recentParticles.clear();
        }
    }

    public static boolean isForbidden(ResourceLocation id) {
        if (forbiddenParticles.isEmpty()) return false;
        return forbiddenCache.computeIfAbsent(id, loc -> {
            String s = loc.toString();
            for (String fp : forbiddenParticles) {
                if (!fp.isEmpty() && s.contains(fp)) return true;
            }
            return false;
        });
    }
}
