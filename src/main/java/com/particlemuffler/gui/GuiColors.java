package com.particlemuffler.gui;

import net.minecraft.util.FastColor;

public final class GuiColors {
    public static final int WHITE_TEXT = 0xffffffff;
    public static final int AQUA_TEXT = 0xff00ffff;
    public static final int RED_TEXT = 0xffdd0303;
    public static final int DARK_BG = FastColor.ARGB32.color(255, 0, 0, 0);
    public static final int BRIGHT_BG = FastColor.ARGB32.color(200, 50, 50, 50);
    public static final int GOLD_BG = FastColor.ARGB32.color(100, 255, 215, 0);
    public static final int WHITE_BG = FastColor.ARGB32.color(255, 255, 255, 255);

    private GuiColors() {}
}
