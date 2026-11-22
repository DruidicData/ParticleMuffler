package com.particlemuffler.utils;

import com.particlemuffler.Config;
import com.particlemuffler.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.util.*;

/**
 * A ranged muffler: particles spawned within {@link #radius} blocks of the Anchor (in the same
 * dimension) use the Anchor's own muffled list.
 * Only simple fields are serialized so the save file stays plain JSON.
 */
public class Anchor {

    private final int id;
    private String name;
    private boolean set;
    private int x, y, z;
    private String dimension;
    private int radius;
    private Map<String, Double> muffledParticles = new TreeMap<>();

    // runtime caches, rebuilt lazily (transient = not saved)
    private transient Map<Identifier, Double> cache;
    private transient Identifier dimensionCache;

    public Anchor(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static List<Anchor> emptyList() {
        List<Anchor> list = new ArrayList<>();
        for (int i = 0; i < Constants.ANCHOR_COUNT; i++) list.add(new Anchor(i, "Anchor " + i));
        return list;
    }

    // ---------------------------------------------------------------- getters

    public int getAnchorId() { return id; }
    public String getName() { return name; }
    public int getRadius() { return radius; }
    public boolean isSet() { return set; }
    public String getX() { return set ? String.valueOf(x) : ""; }
    public String getY() { return set ? String.valueOf(y) : ""; }
    public String getZ() { return set ? String.valueOf(z) : ""; }

    public BlockPos getAnchorPos() {
        return set ? new BlockPos(x, y, z) : null;
    }

    public Identifier getDimension() {
        if (dimensionCache == null && dimension != null) dimensionCache = Identifier.tryParse(dimension);
        return dimensionCache;
    }

    public Map<Identifier, Double> getMuffledParticles() {
        if (cache == null) {
            Map<Identifier, Double> map = new HashMap<>();
            if (muffledParticles == null) muffledParticles = new TreeMap<>();
            muffledParticles.forEach((k, v) -> {
                Identifier rl = Identifier.tryParse(k);
                if (rl != null) map.put(rl, v);
            });
            cache = Collections.unmodifiableMap(map);
        }
        return cache;
    }

    // ---------------------------------------------------------------- mutators

    public void setRadius(int radius) { this.radius = radius; }

    public void addParticle(Identifier particle, double density) {
        muffledParticles.put(particle.toString(), density);
        cache = null;
    }

    public void replaceParticle(Identifier particle, double density) {
        muffledParticles.replace(particle.toString(), density);
        cache = null;
    }

    public void removeParticle(Identifier particle) {
        muffledParticles.remove(particle.toString());
        cache = null;
    }

    public void setAnchor(int defaultRadius) {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        BlockPos pos = player.blockPosition();
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.set = true;
        this.dimension = player.level().dimension().identifier().toString();
        this.dimensionCache = null;
        if (this.radius == 0) this.radius = defaultRadius;
    }

    public void deleteAnchor() {
        name = "Anchor " + id;
        set = false;
        x = y = z = 0;
        dimension = null;
        dimensionCache = null;
        radius = 0;
        muffledParticles.clear();
        cache = null;
    }

    public void editAnchor(String title, int radius) {
        this.name = title;
        this.radius = radius;
    }

    // ---------------------------------------------------------------- lookup

    /**
     * @return the lowest density any Anchor covering this position assigns to the particle, or 1 if none.
     */
    public static double getDensity(Identifier particle, Identifier levelDim, double px, double py, double pz) {
        double result = 1D;
        int maxRange = Config.maxAnchorRange.get();
        for (Anchor anchor : ParticleLists.anchorList) {
            if (!anchor.set || !levelDim.equals(anchor.getDimension())) continue;
            int r = Math.min(anchor.radius, maxRange);
            double dx = px - (anchor.x + 0.5D), dy = py - (anchor.y + 0.5D), dz = pz - (anchor.z + 0.5D);
            if (dx * dx + dy * dy + dz * dz > (double) r * r) continue;

            Map<Identifier, Double> muffled = anchor.getMuffledParticles();
            if (muffled.isEmpty()) continue;
            Double d = muffled.get(Constants.EVERYTHING);
            if (d == null) d = muffled.get(particle);
            if (d != null && d < result) result = d;
        }
        return result;
    }
}
