package com.bibireden.exhud;

import java.text.DecimalFormat;

import com.bibireden.exhud.config.ExHUDConfig;

import com.bibireden.exhud.ui.HealthBarTexture;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class ExHUD implements ClientModInitializer {
	public static final String MODID = "exhud";
	public static final ResourceLocation GUI_HEALTH_BARS = new ResourceLocation(MODID, "textures/gui/health_bars.png");
	public static final ResourceLocation GUI_LEVEL_BARS = new ResourceLocation(MODID, "textures/gui/level_bars.png");
	public static final ResourceLocation VANILLA_GUI_ICONS_TEXTURE = new ResourceLocation("textures/gui/icons.png");
	public static final DecimalFormat FORMAT = new DecimalFormat("#.##");


	public static boolean renderCustomHealthbar() {
		return ExHUDConfig.renderCustomHealthbar;
	}
	
	public static boolean renderCustomUtilities() {
		return ExHUDConfig.renderCustomUtilities;
	}

	public static boolean renderCustomJumpBar() { return ExHUDConfig.renderCustomJumpBar; }

	public static boolean renderCustomExperienceBar() { return ExHUDConfig.renderCustomExperienceBar; }
	
	public static boolean enableFoodStat() { return ExHUDConfig.enableFoodStat; }
	
	public static HealthBarTexture healthbarTexture(final LivingEntity livingEntity) {
        return (
			livingEntity.hasEffect(MobEffects.POISON) ? HealthBarTexture.Poisoned
			: (livingEntity.hasEffect(MobEffects.WITHER) ? HealthBarTexture.Withered
			: (livingEntity.isFreezing() ? HealthBarTexture.Freezing
			: HealthBarTexture.Normal))
		);
	}
	
	public static void drawBorderedText(GuiGraphics ui, Font font, String text, float s, float x, float y, int colour, int borderColour) {
		ui.drawString(font, text, (int)(s * (x - 0.25F)), (int)(s * y), borderColour, false);
		ui.drawString(font, text, (int) (s * x), (int) (s * (y - 0.25F)), borderColour, false);
		ui.drawString(font, text, (int) (s * (x + 0.25F)), (int) (s * y), borderColour, false);
		ui.drawString(font, text, (int) (s * x), (int) (s * (y + 0.25F)), borderColour, false);
		ui.drawString(font, text, (int)(s * x), (int)(s * y), colour, false);
	}

	@Override
	public void onInitializeClient() {
		MidnightConfig.init(MODID, ExHUDConfig.class);
	}
}
