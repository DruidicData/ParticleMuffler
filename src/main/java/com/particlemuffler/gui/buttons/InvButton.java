package com.particlemuffler.gui.buttons;

import com.particlemuffler.Constants;
import com.particlemuffler.gui.GuiColors;
import com.particlemuffler.gui.MufflerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Small button shown in the (creative) inventory. Hold the right mouse button on it to drag it around. */
public class InvButton extends AbstractButton {

    public boolean hold = false;

    public InvButton(int x, int y) {
        super(x, y, 11, 11, Component.translatable("particlemuffler.inventory.btn"));
    }

    @Override
    public void onPress() {
        MufflerScreen.open();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(Constants.texture(), getX(), getY(), 43f, 202f, 11, 11, 256, 256);
        if (isHovered() && !hold) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 300);
            graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + 5, getY() + height + 1, GuiColors.WHITE_TEXT);
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visible && button == 1 && isMouseOver(mouseX, mouseY)) {
            hold = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
