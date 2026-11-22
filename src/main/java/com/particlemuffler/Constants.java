package com.particlemuffler;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {
    public static final String MOD_ID = "particlemuffler";
    public static final Logger LOG = LoggerFactory.getLogger("Particle Muffler");

    /** Pseudo particle id used by Anchors to muffle every particle inside their area. */
    public static final Identifier EVERYTHING = Identifier.fromNamespaceAndPath("pm", "everything");

    public static final KeyMapping MUFFLER_KEY = new KeyMapping(
            "key.particlemuffler.open_gui",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.MISC);

    public static final int ANCHOR_COUNT = 10;

    private Constants() {}

    public static String lang(String key) {
        return MOD_ID + "." + key;
    }

    public static Identifier texture() {
        String texture = Config.useDarkTheme.get() ? "textures/gui/pm_gui_dark.png" : "textures/gui/pm_gui.png";
        return Identifier.fromNamespaceAndPath(MOD_ID, texture);
    }
}
