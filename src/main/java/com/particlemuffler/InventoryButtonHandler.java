package com.particlemuffler;

import com.particlemuffler.gui.buttons.InvButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Adds the Particle Muffler button to the survival and creative inventory using NeoForge screen
 * events (no mixins needed). Hold the right mouse button on it to drag it to a new position.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class InventoryButtonHandler {

    private static final Map<Screen, InvButton> BUTTONS = new WeakHashMap<>();

    private InventoryButtonHandler() {}

    @SubscribeEvent
    static void onInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        boolean creative = screen instanceof CreativeModeInventoryScreen;
        if (!(screen instanceof InventoryScreen) && !creative) return;
        if (creative ? Config.disableCreativeInventoryButton.get() : Config.disableInventoryButton.get()) {
            BUTTONS.remove(screen);
            return;
        }
        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) screen;
        InvButton button = new InvButton(container.getGuiLeft() + offsetX(creative), container.getGuiTop() + offsetY(creative));
        event.addListener(button);
        BUTTONS.put(screen, button);
    }

    @SubscribeEvent
    static void onRender(ScreenEvent.Render.Pre event) {
        InvButton button = BUTTONS.get(event.getScreen());
        if (button == null) return;
        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) event.getScreen();
        boolean creative = container instanceof CreativeModeInventoryScreen;
        button.visible = !creative || ((CreativeModeInventoryScreen) container).isInventoryOpen();

        if (button.hold) {
            button.setX(event.getMouseX() - 5);
            button.setY(event.getMouseY() - 5);
        } else {
            //recalculated every frame so it follows the recipe book
            button.setX(container.getGuiLeft() + offsetX(creative));
            button.setY(container.getGuiTop() + offsetY(creative));
        }
    }

    @SubscribeEvent
    static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getButton() != 1) return;
        InvButton button = BUTTONS.get(event.getScreen());
        if (button == null || !button.hold) return;
        button.hold = false;

        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) event.getScreen();
        int x = (int) event.getMouseX() - 5 - container.getGuiLeft();
        int y = (int) event.getMouseY() - 5 - container.getGuiTop();
        if (container instanceof CreativeModeInventoryScreen) {
            Config.creativeInvButtonHorizontal.set(x);
            Config.creativeInvButtonVertical.set(y);
        } else {
            Config.invButtonHorizontal.set(x);
            Config.invButtonVertical.set(y);
        }
        Config.invButtonHorizontal.save(); // saves the whole client config file
        event.setCanceled(true);
    }

    private static int offsetX(boolean creative) {
        return creative ? Config.creativeInvButtonHorizontal.get() : Config.invButtonHorizontal.get();
    }

    private static int offsetY(boolean creative) {
        return creative ? Config.creativeInvButtonVertical.get() : Config.invButtonVertical.get();
    }
}
