package com.particlemuffler;

import com.particlemuffler.gui.MufflerScreen;
import com.particlemuffler.utils.DataManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only entry point. Because of {@code dist = Dist.CLIENT} this class is never loaded on a
 * dedicated server, and the mod registers nothing that the server would need to know about.
 */
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class ParticleMuffler {

    public ParticleMuffler(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::registerKeys);
        modEventBus.addListener(Config::onLoad);
        modEventBus.addListener(Config::onReload);
        NeoForge.EVENT_BUS.register(this);
    }

    private void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(Constants.MUFFLER_KEY);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        while (Constants.MUFFLER_KEY.consumeClick()) {
            MufflerScreen.open();
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        DataManager.loadData();
    }

    @SubscribeEvent
    public void onPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        DataManager.saveData();
    }
}
