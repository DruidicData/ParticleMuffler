package com.particlemuffler.gui.buttons;

import com.particlemuffler.Config;
import com.particlemuffler.Constants;
import com.particlemuffler.gui.GuiColors;
import com.particlemuffler.gui.MufflerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * One row in the muffler list: the particle name, a density slider (shown once muffled),
 * the muffle/unmuffle toggle and the preview button.
 */
public class MuffledSlider extends AbstractWidget {

    private static boolean showSlider = false;
    private final Font font = Minecraft.getInstance().font;
    private final Identifier particle;
    private final MufflerScreen screen;
    private final int bg;
    private double sliderValue;
    private boolean muffled;
    private Button btnToggle;
    private PreviewButton btnPreview;

    public MuffledSlider(int x, int y, int bg, Identifier particle, double sliderValue, boolean muffled, MufflerScreen screen) {
        super(x, y, 205, 14, Component.literal(particle.toString()));
        this.bg = bg;
        this.particle = particle;
        this.sliderValue = sliderValue;
        this.muffled = muffled;
        this.screen = screen;
        createToggleButton();
        btnPreview = new PreviewButton(btnToggle.getX() + 13, getY(), particle);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        //row highlight
        g.fill(getX(), getY() - 1, getX() + width + 1, getY() + height - 2, bg);
        drawGradient(g);
        //--------------- buttons ---------------//
        float v = muffled ? 202F : 213F;
        g.blit(RenderPipelines.GUI_TEXTURED, Constants.texture(), btnToggle.getX(), btnToggle.getY(), 43F, v, 11, 11, 256, 256); //muffle button
        if (btnPreview.active) {
            g.blit(RenderPipelines.GUI_TEXTURED, Constants.texture(), btnPreview.getX(), btnPreview.getY(), 32F, 202F, 11, 11, 256, 256); //preview button
        }
        //--------------- tooltips ---------------//
        if (btnToggle.isMouseOver(mouseX, mouseY)) {
            renderButtonTooltip(g, btnToggle, Component.translatable(muffled ? "particlemuffler.slider.btn.unmuffle" : "particlemuffler.slider.btn.muffle"));
        }
        if (btnPreview.active && btnPreview.isMouseOver(mouseX, mouseY)) {
            renderButtonTooltip(g, btnPreview, Component.translatable("particlemuffler.slider.btn.preview"));
        }
        drawMessage(g);
    }

    private void renderButtonTooltip(GuiGraphicsExtractor g, AbstractButton button, Component message) {
        g.setTooltipForNextFrame(font, message, button.getX() - (font.width(message) / 2), button.getY() - 1);
    }

    private void drawMessage(GuiGraphicsExtractor g) {
        int color = muffled ? GuiColors.AQUA_TEXT : GuiColors.WHITE_TEXT;
        if (showSlider && isFocused() && isHovered) {
            g.centeredText(font, Component.translatable("particlemuffler.slider.btn.density", (int) Math.round(sliderValue * 100)),
                    getX() + (width / 2), getY() + 2, GuiColors.AQUA_TEXT);
        } else if (particle.equals(Constants.EVERYTHING)) {
            g.text(font, Component.translatable("particlemuffler.slider.title.everything"), getX() + 2, getY() + 2, color, true);
        } else {
            String msg = getMessage().getString();
            int textWidth = font.width(msg);
            if (isHovered && textWidth > width - 4) {
                //show the full name over the buttons when hovered
                g.fill(getX() + width + 1, getY() - 1, getX() + textWidth + 5, getY() + font.lineHeight + 3, bg);
            } else {
                msg = font.plainSubstrByWidth(msg, width - 4);
            }
            g.text(font, msg, getX() + 2, getY() + 2, color, true);
        }
    }

    /** Draws the red -> green gradient showing the current density. */
    private void drawGradient(GuiGraphicsExtractor g) {
        if (muffled) {
            g.blit(RenderPipelines.GUI_TEXTURED, Constants.texture(), getX(), getY() - 1, 0, 234, (int) (sliderValue * (width - 6)) + 5, height + 1, 256, 256);
            if (isHovered) {
                g.blit(RenderPipelines.GUI_TEXTURED, Constants.texture(), getX() + (int) (sliderValue * (width - 6)) + 1, getY() + 1, 32F, 224F, 5, 9, 256, 256); //knob
            }
        }
    }

    public void setRowVisible(boolean b) {
        this.visible = b;
        btnToggle.visible = b;
        btnPreview.visible = b;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        btnToggle.setY(y);
        btnPreview.setY(y);
    }

    public Button getToggleButton() {
        return btnToggle;
    }

    private void createToggleButton() {
        int x = Config.leftButtons.get() ? getX() - 26 : getX() + width + 4;
        btnToggle = Button.builder(Component.empty(), b -> {
            if (muffled) {
                if (screen.removeParticleMuffled(particle)) {
                    muffled = false;
                    if (screen.isShowingMuffledList()) setRowVisible(false);
                }
            } else {
                double value = Config.defaultMuteDensity.get();
                if (screen.addParticleMuffled(particle, value)) {
                    sliderValue = value;
                    muffled = true;
                }
            }
        }).bounds(x, getY(), 11, 11).build();
    }

    private void changeSliderValue(double mouseX) {
        setSliderValue((mouseX - (getX() + 4)) / (width - 8));
    }

    private void setSliderValue(double value) {
        sliderValue = Mth.clamp(value, 0.0D, 0.9D);
        screen.replaceDensity(particle, sliderValue);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        if (muffled) changeSliderValue(event.x());
        super.onDrag(event, dragX, dragY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        //the screen only sends clicks to the widget under the cursor, so the muffle and preview
        //buttons (drawn next to the row) count as part of it
        return super.isMouseOver(mouseX, mouseY)
                || (visible && (btnToggle.isMouseOver(mouseX, mouseY) || btnPreview.isMouseOver(mouseX, mouseY)));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.visible) {
            if (btnToggle.isMouseOver(event.x(), event.y())) return btnToggle.mouseClicked(event, doubleClick);
            if (btnPreview.isMouseOver(event.x(), event.y())) return btnPreview.mouseClicked(event, doubleClick);

            if (isHovered && muffled) {
                changeSliderValue(event.x());
                showSlider = true;
                setFocused(true);
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        setFocused(false);
        return super.mouseReleased(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, muffled
                ? Component.translatable("particlemuffler.slider.btn.density", (int) Math.round(sliderValue * 100))
                : Component.literal(particle.toString()));
    }
}
