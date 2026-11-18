package com.particlemuffler.utils;

import java.util.concurrent.ThreadLocalRandom;

public enum Tips {
    DISABLE, CHANGE_DENSITY, INV_BUTTON, INV_BUTTON_DISABLE, PREVIEW, UNMUFFLE, NO_ANCHORS,
    BLACKLIST, LEFT_BUTTONS, DARK_THEME, USE_ANCHORS, SET_ANCHORS, MODIFY_ANCHORS, MODIFY_ANCHORS_2,
    RESET_RECENT, EVERYTHING, MODS_MUFFLED;

    public String key() {
        return "particlemuffler.tip." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public static String randomTip() {
        Tips[] values = values();
        return values[ThreadLocalRandom.current().nextInt(values.length)].key();
    }
}
