package com.particlemuffler.gui;

import com.particlemuffler.Config;
import com.particlemuffler.Constants;
import com.particlemuffler.gui.buttons.MuffledSlider;
import com.particlemuffler.utils.Anchor;
import com.particlemuffler.utils.DataManager;
import com.particlemuffler.utils.ParticleLists;
import com.particlemuffler.utils.Tips;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.particlemuffler.gui.GuiColors.*;

/** The main Particle Muffler screen (same layout as Extreme Sound Muffler's main screen). */
public class MufflerScreen extends Screen {

    private enum ListMode {
        RECENT("recent"), ALL("all"), MUFFLED("muffled");
        final String key;
        ListMode(String key) { this.key = "particlemuffler.main_screen.btn.csl." + key; }
        ListMode next() { return values()[(ordinal() + 1) % values().length]; }
    }

    private static final int X_SIZE = 256;
    private static final int Y_SIZE = 202;
    private static boolean isMuffling = true;
    private static ListMode listMode = ListMode.RECENT;

    private final int maxAnchorRange = Config.maxAnchorRange.get();
    private final boolean anchorsDisabled = Config.disableAnchors.get();
    private Component screenTitle;
    private Component tip;
    private int tipTicks;
    private int minYButton, maxYButton;
    private Button btnTMS, btnDelete, btnCSL, btnSetAnchor, btnEditAnchor, btnNext, btnPrev, btnAccept, btnCancel, btnAnchor;
    private EditBox searchBar, editAnchorTitleBar, editRadBar;
    private MuffledSlider firstRow, lastRow;
    @Nullable
    private Anchor anchor;

    private MufflerScreen() {
        super(Component.translatable("particlemuffler.main_screen.main_title"));
        this.screenTitle = getTitle();
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new MufflerScreen());
    }

    @Override
    protected void init() {
        super.init();
        minYButton = getY() + 46;
        maxYButton = getY() + 164;

        addButtons();
        addSideButtons();
        addAnchorButtons();
        addParticleListButtons();
        if (anchor != null) btnSetAnchor.active = true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        g.blit(Constants.texture(), getX(), getY(), 0, 0, X_SIZE, Y_SIZE); //main screen bounds
        renderSideScreen(g); //side screen, before the widgets
        super.render(g, mouseX, mouseY, partialTicks);
        g.drawCenteredString(font, screenTitle, getX() + 128, getY() + 8, WHITE_TEXT);
        renderTips(g);
        renderButtons(g, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        //no background: keep the world (and the particles) visible
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //Enter / numpad enter removes focus from the text fields
        if (keyCode == 257 || keyCode == 335) {
            searchBar.setFocused(false);
            editAnchorTitleBar.setFocused(false);
            editRadBar.setFocused(false);
            return true;
        }
        //close with the inventory key or the mod key when not typing
        if (!searchBar.isFocused() && !editAnchorTitleBar.isFocused() && !editRadBar.isFocused()
                && (minecraft.options.keyInventory.matches(keyCode, scanCode) || Constants.MUFFLER_KEY.matches(keyCode, scanCode))) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (searchBar.isFocused()) updateButtons();
        validateRadius();
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean result = super.charTyped(codePoint, modifiers);
        if (searchBar.isFocused()) updateButtons();
        return result;
    }

    private void validateRadius() {
        if (!editRadBar.getValue().isEmpty()) {
            int radius = parseInt(editRadBar.getValue());
            editRadBar.setTextColor(radius > maxAnchorRange || radius < 1 ? RED_TEXT : WHITE_TEXT);
        } else {
            editRadBar.setTextColor(WHITE_TEXT);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double dir = scrollX == 0 ? scrollY : scrollX;
        if (firstRow == null || dir == 0) return false;
        if ((dir > 0 && firstRow.getY() >= minYButton) || (dir < 0 && lastRow.getY() <= maxYButton)) return false;

        int step = (int) (14 * 9 * Mth.clamp(dir, -1, 1)); //a full page at a time
        for (GuiEventListener child : children()) {
            if (child instanceof MuffledSlider row) {
                row.setY(row.getY() + step);
                row.setRowVisible(row.getY() >= minYButton && row.getY() <= maxYButton);
            }
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) { //right click clears text fields
            if (searchBar.isMouseOver(mouseX, mouseY) || searchBar.isFocused()) {
                searchBar.setValue("");
                updateButtons();
                return true;
            }
            if (editAnchorTitleBar.visible && editAnchorTitleBar.isMouseOver(mouseX, mouseY)) {
                editAnchorTitleBar.setValue("");
                return true;
            }
            if (editRadBar.visible && editRadBar.isMouseOver(mouseX, mouseY)) {
                editRadBar.setValue("");
                return true;
            }
        } else {
            searchBar.setFocused(searchBar.isMouseOver(mouseX, mouseY));
            editAnchorTitleBar.setFocused(editAnchorTitleBar.visible && editAnchorTitleBar.isMouseOver(mouseX, mouseY));
            editRadBar.setFocused(editRadBar.visible && editRadBar.isMouseOver(mouseX, mouseY));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        DataManager.saveData();
        super.onClose();
    }

    //----------------------------------- Buttons init -----------------------------------//

    private void addButtons() {
        //change particles list (Recent / All / Muffled)
        btnCSL = addWidget(Button.builder(Component.translatable(listMode.key), b -> {
            listMode = listMode.next();
            b.setMessage(Component.translatable(listMode.key));
            updateButtons();
        }).bounds(getX() + 13, getY() + 181, 52, 13).build());
        //search bar
        String oldSearch = searchBar != null ? searchBar.getValue() : "";
        searchBar = addRenderableWidget(new EditBox(font, getX() + 74, getY() + 183, 119, 13, Component.empty()));
        searchBar.setBordered(false);
        searchBar.setValue(oldSearch);
        //toggle muffling on / off
        btnTMS = addWidget(Button.builder(Component.empty(), b -> setMuffling(!isMuffling))
                .bounds(getX() + 229, getY() + 180, 17, 17).build());
        //delete muffled list, or the recent list while holding shift
        btnDelete = addWidget(Button.builder(Component.empty(), b -> {
            if (hasShiftDown()) {
                ParticleLists.clearRecent();
            } else if (anchor == null) {
                ParticleLists.muffledParticles.clear();
            } else {
                anchor.deleteAnchor();
                screenTitle = Component.literal(anchor.getName());
                hideSideButtons();
                btnSetAnchor.active = true;
            }
            updateButtons();
        }).bounds(getX() + 205, getY() + 180, 17, 17).build());
        //previous / next page
        btnPrev = addWidget(Button.builder(Component.empty(), b -> mouseScrolled(0D, 0D, 1D, 0D)).bounds(getX() + 10, getY() + 22, 13, 20).build());
        btnNext = addWidget(Button.builder(Component.empty(), b -> mouseScrolled(0D, 0D, -1D, 0D)).bounds(getX() + 233, getY() + 22, 13, 20).build());
    }

    private void addSideButtons() {
        //set the anchor at the player's position
        btnSetAnchor = addWidget(Button.builder(Component.empty(), b -> {
            if (anchor != null) {
                anchor.setAnchor(Math.min(16, maxAnchorRange));
                updateButtons();
            }
        }).bounds(getX() + 261, getY() + 62, 11, 11).build());
        btnSetAnchor.active = false;
        //edit anchor name / radius
        btnEditAnchor = addWidget(Button.builder(Component.empty(), b -> editTitle()).bounds(getX() + 275, getY() + 62, 11, 11).build());
        btnEditAnchor.active = false;
        editAnchorTitleBar = addRenderableWidget(new EditBox(font, getX() + 302, btnEditAnchor.getY() + 20, 84, 11, Component.empty()));
        editAnchorTitleBar.visible = false;
        editRadBar = addRenderableWidget(new EditBox(font, getX() + 302, editAnchorTitleBar.getY() + 15, 30, 11, Component.empty()));
        editRadBar.setFilter(s -> s.matches("[0-9]{0,4}"));
        editRadBar.visible = false;
        btnAccept = addRenderableWidget(Button.builder(Component.translatable("particlemuffler.main_screen.btn.accept"), b -> {
            if (!editAnchorTitleBar.getValue().isEmpty() && !editRadBar.getValue().isEmpty() && anchor != null) {
                anchor.editAnchor(editAnchorTitleBar.getValue(), Mth.clamp(parseInt(editRadBar.getValue()), 1, maxAnchorRange));
                screenTitle = Component.literal(editAnchorTitleBar.getValue());
                editTitle();
            }
        }).bounds(getX() + 259, editRadBar.getY() + 15, 40, 20).build());
        btnAccept.visible = false;
        btnCancel = addRenderableWidget(Button.builder(Component.translatable("particlemuffler.main_screen.btn.cancel"), b -> editTitle())
                .bounds(getX() + 300, editRadBar.getY() + 15, 40, 20).build());
        btnCancel.visible = false;
    }

    private void addAnchorButtons() {
        int bx = getX() + 30;
        String[] disabledMsg = {"-", "D", "i", "s", "a", "b", "l", "e", "d", "-"};
        for (int i = 0; i < Constants.ANCHOR_COUNT; i++) {
            if (anchorsDisabled) {
                btnAnchor = Button.builder(Component.literal(disabledMsg[i]), b -> {}).bounds(bx, getY() + 24, 16, 16).build();
                btnAnchor.active = false;
            } else {
                final int id = i;
                btnAnchor = Button.builder(Component.literal(String.valueOf(i)), b -> selectAnchor(id))
                        .bounds(bx, getY() + 24, 16, 16).build();
            }
            addRenderableWidget(btnAnchor).setAlpha(0);
            bx += 20;
        }
    }

    private void selectAnchor(int id) {
        if (ParticleLists.anchorList.size() <= id) return;
        Anchor clicked = ParticleLists.anchorList.get(id);
        hideSideButtons();
        if (anchor == clicked) { //clicking the selected anchor goes back to the main list
            anchor = null;
            screenTitle = getTitle();
        } else {
            anchor = clicked;
            if (anchor.getRadius() > maxAnchorRange) anchor.setRadius(maxAnchorRange);
            screenTitle = Component.literal(anchor.getName());
            btnSetAnchor.active = true;
        }
        updateButtons();
    }

    private void addParticleListButtons() {
        int by = minYButton;
        int bx = Config.leftButtons.get() ? getX() + 38 : getX() + 11;
        firstRow = null;
        lastRow = null;

        Map<ResourceLocation, Double> muffled = currentMuffledMap();
        List<ResourceLocation> list = new ArrayList<>();
        switch (listMode) {
            case RECENT -> {
                list.addAll(ParticleLists.recentSnapshot());
                if (anchor != null) list.add(Constants.EVERYTHING);
                Collections.reverse(list); //newest first
            }
            case ALL -> {
                list.addAll(BuiltInRegistries.PARTICLE_TYPE.keySet());
                Collections.sort(list);
            }
            case MUFFLED -> {
                list.addAll(muffled.keySet());
                Collections.sort(list);
            }
        }

        //hide blacklisted particles
        if (listMode == ListMode.RECENT || (listMode == ListMode.ALL && Config.lawfulAllList.get())) {
            list.removeIf(ParticleLists::isForbidden);
        }

        String search = searchBar.getValue().toLowerCase(Locale.ROOT);
        int row = 0;
        for (ResourceLocation particle : list) {
            if (!search.isEmpty() && !particle.toString().contains(search)) continue;
            Double density = muffled.get(particle);
            int bg = row++ % 2 == 0 ? DARK_BG : BRIGHT_BG;
            MuffledSlider slider = new MuffledSlider(bx, by, bg, particle, density == null ? 1D : density, density != null, this);
            addRenderableWidget(slider);
            slider.setRowVisible(by <= maxYButton);
            by += slider.getHeight();
            if (firstRow == null) firstRow = slider;
            lastRow = slider;
        }
    }

    private void updateButtons() {
        for (GuiEventListener w : List.copyOf(children())) {
            if (w instanceof MuffledSlider) removeWidget(w);
        }
        addParticleListButtons();
    }

    //----------------------------------- Rendering -----------------------------------//

    private void renderButtons(GuiGraphics g, int mouseX, int mouseY) {
        //--------------- toggle muffling ---------------//
        if (isMuffling) { //draw a "/" over the button while particles are being muffled
            g.blit(Constants.texture(), btnTMS.getX() + 1, btnTMS.getY(), 54F, 202F, 15, 15, X_SIZE, 256);
        }
        if (btnTMS.isMouseOver(mouseX, mouseY)) {
            renderButtonTooltip(g, Component.translatable(isMuffling ? "particlemuffler.main_screen.btn.tms.stop" : "particlemuffler.main_screen.btn.tms.start"), btnTMS);
        }

        //--------------- delete ---------------//
        Component message = Component.translatable(anchor == null ? "particlemuffler.main_screen.btn.delete.particles" : "particlemuffler.main_screen.btn.delete.anchor");
        if (hasShiftDown()) {
            g.blit(Constants.texture(), btnDelete.getX() + 2, btnDelete.getY() + 1, 54F, 217F, 13, 13, X_SIZE, 256);
            message = Component.translatable("particlemuffler.main_screen.btn.delete.list");
        }
        if (btnDelete.isMouseOver(mouseX, mouseY)) renderButtonTooltip(g, message, btnDelete);

        //--------------- search bar hint ---------------//
        if (!searchBar.isFocused() && searchBar.getValue().isEmpty()) {
            Component hint = Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
            g.drawString(font, hint, searchBar.getX() + 1, searchBar.getY() + 1, -1);
        }

        //--------------- list mode ---------------//
        if (firstRow == null) {
            Component text = Component.translatable("particlemuffler.main_screen.empty").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
            g.drawCenteredString(font, text, getX() + 128, getY() + 101, WHITE_TEXT);
        }
        String csl = btnCSL.getMessage().getString();
        g.drawString(font, csl, (int) (btnCSL.getX() + btnCSL.getWidth() / 2F - font.width(csl) / 2F), btnCSL.getY() + 3, 0, false);
        if (btnCSL.isMouseOver(mouseX, mouseY)) {
            renderButtonTooltip(g, Component.translatable("particlemuffler.main_screen.btn.csl.tooltip", csl), btnCSL);
        }

        //--------------- pages ---------------//
        if (btnNext.isMouseOver(mouseX, mouseY)) renderButtonTooltip(g, Component.translatable("particlemuffler.main_screen.btn.next"), btnNext);
        if (btnPrev.isMouseOver(mouseX, mouseY)) renderButtonTooltip(g, Component.translatable("particlemuffler.main_screen.btn.previous"), btnPrev);

        //--------------- anchors ---------------//
        if (anchorsDisabled && mouseX > getX() + 30 && mouseX < btnAnchor.getX() + btnAnchor.getWidth()
                && mouseY > btnAnchor.getY() && mouseY < btnAnchor.getY() + btnAnchor.getHeight()) {
            g.renderTooltip(font, Component.translatable("particlemuffler.main_screen.btn.anchors.disabled"), getX() + 60, getY() + 40);
        }

        for (GuiEventListener widget : children()) {
            if (widget instanceof MuffledSlider slider) {
                if (slider.visible && anchor != null && !anchor.isSet() && slider.getToggleButton().isMouseOver(mouseX, mouseY)) {
                    renderButtonTooltip(g, Component.translatable("particlemuffler.main_screen.btn.anchors.set_message"), slider.getToggleButton());
                }
            } else if (!anchorsDisabled && widget instanceof AbstractWidget btn && btn.getMessage().getString().matches("[0-9]")) {
                int id = Integer.parseInt(btn.getMessage().getString());
                if (id < ParticleLists.anchorList.size()) {
                    boolean set = ParticleLists.anchorList.get(id).isSet();
                    btn.setMessage(Component.literal(String.valueOf(id)).withStyle(set ? ChatFormatting.GREEN : ChatFormatting.WHITE));
                    if (anchor != null && anchor.getAnchorId() == id) {
                        g.blit(Constants.texture(), btn.getX() - 5, btn.getY() - 2, 71F, 202F, 27, 22, X_SIZE, 256); //selected anchor
                    }
                }
            }
        }

        //--------------- side screen ---------------//
        if (editRadBar.visible && (editRadBar.isHovered() || editRadBar.isFocused())) {
            renderButtonTooltip(g, Component.translatable("particlemuffler.main_screen.btn.anchors.set_range", maxAnchorRange), editRadBar);
        }
        if (editAnchorTitleBar.visible && (editAnchorTitleBar.isHovered() || editAnchorTitleBar.isFocused())) {
            renderButtonTooltip(g, Component.translatable("particlemuffler.main_screen.btn.anchors.set_title"), editAnchorTitleBar);
        }
        if (anchor != null && btnSetAnchor.isMouseOver(mouseX, mouseY)) {
            renderButtonTooltip(g, Component.translatable("particlemuffler.main_screen.btn.anchors.set"), btnSetAnchor);
        }
        if (anchor != null && btnEditAnchor.active && btnEditAnchor.isMouseOver(mouseX, mouseY)) {
            renderButtonTooltip(g, Component.translatable("particlemuffler.main_screen.btn.anchors.edit"), btnEditAnchor);
        }
    }

    private void renderSideScreen(GuiGraphics g) {
        if (anchor == null) return;
        int x = btnSetAnchor.getX();
        int y = btnSetAnchor.getY();
        String dimensionName = anchor.getDimension() != null ? anchor.getDimension().getPath() : "";
        String radius = anchor.getRadius() == 0 ? "" : String.valueOf(anchor.getRadius());
        int stringW = Math.max(font.width(Component.translatable("particlemuffler.main_screen.side_screen.dimension", dimensionName)), 80);

        g.fill(x - 5, y - 57, x + stringW + 7, y + 17, WHITE_BG); //light border
        g.fill(x - 5, y - 56, x + stringW + 6, y + 16, DARK_BG); //dark background
        g.drawString(font, Component.translatable("particlemuffler.main_screen.side_screen.x", anchor.getX()), x + 1, y - 50, WHITE_TEXT);
        g.drawString(font, Component.translatable("particlemuffler.main_screen.side_screen.y", anchor.getY()), x + 1, y - 40, WHITE_TEXT);
        g.drawString(font, Component.translatable("particlemuffler.main_screen.side_screen.z", anchor.getZ()), x + 1, y - 30, WHITE_TEXT);
        g.drawString(font, Component.translatable("particlemuffler.main_screen.side_screen.radius", radius), x + 1, y - 20, WHITE_TEXT);
        g.drawString(font, Component.translatable("particlemuffler.main_screen.side_screen.dimension", dimensionName), x + 1, y - 10, WHITE_TEXT);
        g.blit(Constants.texture(), x, y, 11, 11, 0F, 202F, 32, 32, 256, 256); //"set anchor" marker button

        btnEditAnchor.active = anchor.isSet();
        if (anchor.isSet()) {
            g.blit(Constants.texture(), btnEditAnchor.getX(), btnEditAnchor.getY(), 32F, 213F, 11, 11, X_SIZE, 256); //edit button
        }

        if (editRadBar.visible) {
            y = editAnchorTitleBar.getY();
            int right = editAnchorTitleBar.getX() + editAnchorTitleBar.getWidth() + 4;
            g.fill(x - 6, y - 5, right, btnAccept.getY() + 24, WHITE_BG);
            g.fill(x - 5, y - 4, right - 1, btnAccept.getY() + 23, DARK_BG);
            g.drawString(font, Component.translatable("particlemuffler.main_screen.side_screen.title"), x - 2, y + 1, WHITE_TEXT);
            g.drawString(font, Component.translatable("particlemuffler.main_screen.side_screen.radius_edit"), x - 2, editRadBar.getY() + 1, WHITE_TEXT);
        }
    }

    private void renderButtonTooltip(GuiGraphics g, Component message, AbstractWidget button) {
        int shift = button == btnCSL ? 25 : 0;
        int tx = button.getX() - ((font.width(message) - shift) / 2);
        int ty = button == btnPrev || button == btnNext ? button.getY() - 1 : button.getY() + button.getHeight() + 16;
        g.renderTooltip(font, message, tx, ty);
    }

    private void renderTips(GuiGraphics g) {
        if (!Config.showTip.get()) return;
        if (tip == null || tipTicks++ % 500 == 0) {
            tip = Component.translatable("particlemuffler.main_screen.tip", Component.translatable(Tips.randomTip()));
            tipTicks = 1;
        }
        int h = font.lineHeight * font.split(tip, 245).size() + 215;
        g.fill(getX() - 2, getY() + 208, getX() + 257, getY() + h + 2, DARK_BG);
        g.fill(getX() - 1, getY() + 209, getX() + 256, getY() + h + 1, GOLD_BG);
        g.fill(getX(), getY() + 210, getX() + 255, getY() + h, DARK_BG);
        g.drawWordWrap(font, tip, getX() + 5, getY() + 213, 245, WHITE_TEXT);
    }

    //----------------------------------- Other functions -----------------------------------//

    private void hideSideButtons() {
        btnSetAnchor.active = false;
        btnEditAnchor.active = false;
        editAnchorTitleBar.visible = false;
        editRadBar.visible = false;
        btnAccept.visible = false;
        btnCancel.visible = false;
    }

    private void editTitle() {
        if (anchor == null) return;
        boolean show = !editAnchorTitleBar.visible;
        editAnchorTitleBar.setValue(anchor.getName());
        editRadBar.setValue(String.valueOf(anchor.getRadius()));
        editAnchorTitleBar.visible = show;
        editRadBar.visible = show;
        btnAccept.visible = show;
        btnCancel.visible = show;
        editRadBar.setTextColor(WHITE_TEXT);
    }

    private Map<ResourceLocation, Double> currentMuffledMap() {
        return anchor == null ? ParticleLists.muffledParticles : anchor.getMuffledParticles();
    }

    public boolean removeParticleMuffled(ResourceLocation particle) {
        if (anchor != null) {
            if (!anchor.isSet()) return false;
            anchor.removeParticle(particle);
        } else {
            ParticleLists.muffledParticles.remove(particle);
        }
        return true;
    }

    public boolean addParticleMuffled(ResourceLocation particle, double density) {
        if (anchor != null) {
            if (!anchor.isSet()) return false;
            anchor.addParticle(particle, density);
        } else {
            ParticleLists.muffledParticles.put(particle, density);
        }
        return true;
    }

    public void replaceDensity(ResourceLocation particle, double density) {
        if (anchor != null) {
            anchor.replaceParticle(particle, density);
        } else {
            ParticleLists.muffledParticles.replace(particle, density);
        }
    }

    public boolean isShowingMuffledList() {
        return listMode == ListMode.MUFFLED;
    }

    public static boolean isMuffling() {
        return isMuffling;
    }

    public static void setMuffling(boolean muffling) {
        isMuffling = muffling;
    }

    private int getX() {
        return (this.width - X_SIZE) / 2;
    }

    private int getY() {
        return (this.height - Y_SIZE) / 2;
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
