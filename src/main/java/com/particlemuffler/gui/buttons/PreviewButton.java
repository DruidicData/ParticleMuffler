package com.particlemuffler.gui.buttons;

import com.particlemuffler.Constants;
import com.particlemuffler.utils.ParticleFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

/**
 * The particle equivalent of the "play sound" button: spawns a short burst of the particle in an
 * arc in front of the player (to the left and right of the GUI) so you can see what it looks like.
 */
public class PreviewButton extends AbstractButton {

    @Nullable
    private final ParticleOptions options;

    PreviewButton(int x, int y, ResourceLocation particle) {
        super(x, y, 10, 10, CommonComponents.EMPTY);
        this.setAlpha(0);
        ParticleType<?> type = particle.equals(Constants.EVERYTHING) ? null : BuiltInRegistries.PARTICLE_TYPE.get(particle);
        this.options = type == null ? null : optionsFor(type);
        this.active = options != null;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // the texture is drawn by MuffledSlider
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.isMouseOver(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onPress() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || options == null) return;

        RandomSource random = player.getRandom();
        double eyeX = player.getX(), eyeY = player.getEyeY(), eyeZ = player.getZ();
        float yaw = player.getYRot();

        ParticleFilter.runBypassed(() -> {
            for (int i = 0; i < 24; i++) {
                // alternate between the left and right side of the screen so the particles are not hidden by the GUI
                float side = (i % 2 == 0 ? 1 : -1) * (28f + random.nextFloat() * 14f);
                float angle = (yaw + side) * Mth.DEG_TO_RAD;
                double dist = 2.2D + random.nextDouble() * 0.6D;
                double px = eyeX - Mth.sin(angle) * dist;
                double pz = eyeZ + Mth.cos(angle) * dist;
                double py = eyeY - 0.4D + random.nextDouble() * 0.8D;
                try {
                    mc.particleEngine.createParticle(options, px, py, pz,
                            (random.nextDouble() - 0.5D) * 0.04D, random.nextDouble() * 0.04D, (random.nextDouble() - 0.5D) * 0.04D);
                } catch (Exception e) {
                    Constants.LOG.debug("Particle Muffler: could not preview {}", options.getType(), e);
                    return;
                }
            }
        });
    }

    /** Builds default options for particle types that need extra data. Returns null if unknown. */
    @Nullable
    private static ParticleOptions optionsFor(ParticleType<?> type) {
        if (type instanceof SimpleParticleType simple) return simple;
        if (type == ParticleTypes.BLOCK) return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRASS_BLOCK.defaultBlockState());
        if (type == ParticleTypes.BLOCK_MARKER) return new BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.BARRIER.defaultBlockState());
        if (type == ParticleTypes.FALLING_DUST) return new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());
        if (type == ParticleTypes.DUST_PILLAR) return new BlockParticleOption(ParticleTypes.DUST_PILLAR, Blocks.STONE.defaultBlockState());
        if (type == ParticleTypes.ITEM) return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.DIAMOND));
        if (type == ParticleTypes.DUST) return DustParticleOptions.REDSTONE;
        if (type == ParticleTypes.DUST_COLOR_TRANSITION) return DustColorTransitionOptions.SCULK_TO_REDSTONE;
        if (type == ParticleTypes.ENTITY_EFFECT) return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF8030FF);
        if (type == ParticleTypes.SCULK_CHARGE) return new SculkChargeParticleOptions(0f);
        if (type == ParticleTypes.SHRIEK) return new ShriekParticleOption(0);
        return null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.translatable("particlemuffler.slider.btn.preview"));
    }
}
