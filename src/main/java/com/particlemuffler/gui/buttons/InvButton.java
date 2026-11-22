package com.particlemuffler.gui.buttons;

import com.particlemuffler.Constants;
import com.particlemuffler.gui.MufflerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Small button shown in the (creative) inventory. Hold the right mouse button on it to drag it around. */
public class InvButton extends AbstractButton {

    public boolean hold = false;

    private final Tooltip tooltip;

    public InvButton(int x, int y) {
        super(x, y, 11, 11, Component.translatable("particlemuffler.inventory.btn"));
        //standard vanilla tooltip box, the same one used for item names
        this.tooltip = Tooltip.create(getMessage());
        setTooltip(tooltip);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        MufflerScreen.open();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.texture(), getX(), getY(), 43f, 202f, 11, 11, 256, 256);
        //hide the tooltip while the button is being dragged around
        setTooltip(hold ? null : tooltip);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (visible && event.button() == 1 && isMouseOver(event.x(), event.y())) {
            hold = true;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
