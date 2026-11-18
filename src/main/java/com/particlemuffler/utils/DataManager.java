package com.particlemuffler.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.particlemuffler.Config;
import com.particlemuffler.Constants;
import com.particlemuffler.gui.MufflerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Saves / loads the muffled lists as JSON in {@code <game dir>/ParticleMuffler/}.
 * Globally muffled particles are shared between worlds; Anchors are saved per world / server.
 */
public final class DataManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path ROOT = Minecraft.getInstance().gameDirectory.toPath().resolve("ParticleMuffler");

    private DataManager() {}

    public static void loadData() {
        MufflerScreen.setMuffling(true);

        ParticleLists.muffledParticles.clear();
        Map<String, Double> muffled = read(ROOT.resolve("particlesMuffled.json"), new TypeToken<Map<String, Double>>() {});
        if (muffled != null) {
            muffled.forEach((k, v) -> {
                ResourceLocation rl = ResourceLocation.tryParse(k);
                if (rl != null && v != null) ParticleLists.muffledParticles.put(rl, v);
            });
        }

        ParticleLists.anchorList.clear();
        List<Anchor> anchors = read(anchorFile(), new TypeToken<List<Anchor>>() {});
        ParticleLists.anchorList.addAll(anchors != null && anchors.size() == Constants.ANCHOR_COUNT ? anchors : Anchor.emptyList());

        saveData();
    }

    public static void saveData() {
        Map<String, Double> out = new java.util.TreeMap<>();
        ParticleLists.muffledParticles.forEach((k, v) -> out.put(k.toString(), v));
        write(ROOT.resolve("particlesMuffled.json"), out);

        if (!Config.disableAnchors.get() && !ParticleLists.anchorList.isEmpty()) {
            write(anchorFile(), ParticleLists.anchorList);
        }
    }

    private static Path anchorFile() {
        return ROOT.resolve(sanitize(getWorldName())).resolve("anchors.json");
    }

    private static String getWorldName() {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null) return "world_" + server.getWorldData().getLevelName().strip();
        ServerData data = mc.getCurrentServer();
        if (data != null) return "server_" + data.ip;
        return "unknown";
    }

    private static String sanitize(String name) {
        String s = name.replaceAll("[^A-Za-z0-9._ -]", "_").strip();
        return s.isEmpty() ? "unknown" : s;
    }

    private static <T> T read(Path file, TypeToken<T> type) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, type.getType());
        } catch (NoSuchFileException e) {
            Constants.LOG.info("Particle Muffler: {} not found, creating it", ROOT.relativize(file));
        } catch (Exception e) {
            Constants.LOG.error("Particle Muffler: error loading {}", file, e);
        }
        return null;
    }

    private static void write(Path file, Object data) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            Constants.LOG.error("Particle Muffler: error saving {}", file, e);
        }
    }
}
