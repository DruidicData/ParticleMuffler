package com.particlemuffler;

import com.particlemuffler.utils.DataManager;
import com.particlemuffler.utils.ParticleLists;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

/** Client config (config/particlemuffler-client.toml). Mirrors Extreme Sound Muffler's options. */
public final class Config {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> forbiddenParticles;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> modsMuffled;
    public static final ModConfigSpec.BooleanValue lawfulAllList;
    public static final ModConfigSpec.DoubleValue defaultMuteDensity;
    public static final ModConfigSpec.BooleanValue leftButtons;
    public static final ModConfigSpec.BooleanValue showTip;
    public static final ModConfigSpec.BooleanValue useDarkTheme;

    public static final ModConfigSpec.BooleanValue disableInventoryButton;
    public static final ModConfigSpec.IntValue invButtonHorizontal;
    public static final ModConfigSpec.IntValue invButtonVertical;
    public static final ModConfigSpec.BooleanValue disableCreativeInventoryButton;
    public static final ModConfigSpec.IntValue creativeInvButtonHorizontal;
    public static final ModConfigSpec.IntValue creativeInvButtonVertical;

    public static final ModConfigSpec.BooleanValue disableAnchors;
    public static final ModConfigSpec.IntValue maxAnchorRange;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("General settings").push("general");
        forbiddenParticles = b
                .comment("Blacklisted particles - these never show up in the Recent list (matched with \"contains\").",
                        "Example: \"minecraft:block\", \"dust\"",
                        "Default: empty")
                .defineListAllowEmpty("forbiddenParticles", new ArrayList<>(), () -> "", o -> o instanceof String);
        modsMuffled = b
                .comment("Mod-wide muffling: every particle from these mods is reduced to the given density (0-100 percent).",
                        "Format \"modid:percent\". Example: \"minecraft:50\", \"somemod:0\"",
                        "Default: empty")
                .defineListAllowEmpty("modsMuffled", new ArrayList<>(), () -> "", o -> o instanceof String s && s.contains(":"));
        lawfulAllList = b
                .comment("Should the blacklisted particles also be hidden from the \"All\" list?",
                        "Default: false")
                .define("lawfulAllList", false);
        defaultMuteDensity = b
                .comment("Density (0 = no particles, 0.9 = 90% of particles) applied when the muffle button is pressed.",
                        "Default: 0")
                .defineInRange("defaultMuteDensity", 0D, 0D, 0.9D);
        leftButtons = b
                .comment("Move the muffle and preview buttons to the left side of the GUI.", "Default: false")
                .define("leftButtons", false);
        showTip = b
                .comment("Show tips in the Muffler screen?", "Default: true")
                .define("showTip", true);
        useDarkTheme = b
                .comment("Use the dark theme?", "Default: false")
                .define("useDarkTheme", false);
        b.pop();

        b.comment("Inventory button settings").push("inventory_button");
        disableInventoryButton = b
                .comment("Disable the Particle Muffler button in the player inventory?", "Default: false")
                .define("disableInventoryButton", false);
        invButtonHorizontal = b
                .comment("X position of the button in the player inventory.",
                        "You can change this in game by holding RMB over the button and dragging it around.",
                        "Default: 75")
                .defineInRange("invButtonX", 75, Integer.MIN_VALUE, Integer.MAX_VALUE);
        invButtonVertical = b
                .comment("Y position of the button in the player inventory.", "Default: 20")
                .defineInRange("invButtonY", 20, Integer.MIN_VALUE, Integer.MAX_VALUE);
        disableCreativeInventoryButton = b
                .comment("Disable the Particle Muffler button in the creative inventory?", "Default: false")
                .define("disableCreativeInventoryButton", false);
        creativeInvButtonHorizontal = b
                .comment("X position of the button in the creative inventory.", "Default: 2")
                .defineInRange("creativeInvButtonX", 2, Integer.MIN_VALUE, Integer.MAX_VALUE);
        creativeInvButtonVertical = b
                .comment("Y position of the button in the creative inventory.", "Default: 15")
                .defineInRange("creativeInvButtonY", 15, Integer.MIN_VALUE, Integer.MAX_VALUE);
        b.pop();

        b.comment("Anchor settings").push("anchors");
        disableAnchors = b
                .comment("Disable the Anchors (area based muffling)?", "Default: false")
                .define("disableAnchors", false);
        maxAnchorRange = b
                .comment("Maximum radius for Anchors.", "Default: 64")
                .defineInRange("anchorRange", 64, 1, 1024);
        b.pop();

        SPEC = b.build();
    }

    private Config() {}

    static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) refreshLists();
    }

    static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != SPEC) return;
        refreshLists();
        if (ParticleLists.anchorList.isEmpty()) DataManager.loadData();
    }

    private static void refreshLists() {
        ParticleLists.forbiddenParticles.clear();
        ParticleLists.forbiddenParticles.addAll(forbiddenParticles.get());
        ParticleLists.forbiddenCache.clear();

        ParticleLists.modsMuffled.clear();
        for (String entry : modsMuffled.get()) {
            int idx = entry.lastIndexOf(':');
            if (idx <= 0) continue;
            try {
                double pct = Double.parseDouble(entry.substring(idx + 1).trim());
                ParticleLists.modsMuffled.put(entry.substring(0, idx).trim().toLowerCase(java.util.Locale.ROOT),
                        Math.max(0D, Math.min(1D, pct / 100D)));
            } catch (NumberFormatException e) {
                Constants.LOG.warn("Particle Muffler: invalid modsMuffled entry '{}'", entry);
            }
        }
    }
}
