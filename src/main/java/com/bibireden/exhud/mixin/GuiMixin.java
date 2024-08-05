package com.bibireden.exhud.mixin;

import com.bibireden.exhud.compat.CompatUtil;
import com.bibireden.exhud.ui.HealthBarTexture;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.exhud.ExHUD;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;

@Mixin(value = Gui.class, priority = 1)
abstract class GuiMixin {

	@Unique
	final float MAX_HEALTH_BAR_WIDTH = 78.0F;

	@Final
	@Shadow
	private Minecraft minecraft;
	
	@Shadow
	private int screenWidth;
	
	@Shadow
    private int screenHeight;
	
	@Shadow
	protected abstract Player getCameraPlayer();

	@Shadow protected abstract LivingEntity getPlayerVehicleWithHealth();

	@Shadow protected abstract int getVehicleMaxHearts(LivingEntity vehicle);

	// Removes vanilla mount health bar and renders our own.
	@Inject(method = "renderVehicleHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"), cancellable = true)
	private void exhud$renderVehicleHealth(GuiGraphics ctx, CallbackInfo ci) {
		if(!ExHUD.renderCustomHealthbar()) return;
		
		LivingEntity riddenEntity = this.getPlayerVehicleWithHealth();
		if (riddenEntity == null) return;

		int h = (int) Math.min(78.0F / riddenEntity.getMaxHealth() * riddenEntity.getHealth(), 78.0F);
		int x = this.screenWidth / 2 + 13;
		int y = this.screenHeight - 37;

		ctx.blit(ExHUD.GUI_HEALTH_BARS, x, y, 0, HealthBarTexture.Empty.getXPos(), 78, 8, 128, 64);
		
		if(Mth.ceil(riddenEntity.getAbsorptionAmount()) > 0) {
			ctx.blit(ExHUD.GUI_HEALTH_BARS, x, y, 0, 40, 78, 8, 128, 64);
		}

		ctx.blit(ExHUD.GUI_HEALTH_BARS, x, y, 0, ExHUD.healthbarTexture(riddenEntity).getXPos(), 78 - h, 8, 128, 64);
		
		String healthbar = ExHUD.FORMAT.format(riddenEntity.getHealth() + riddenEntity.getAbsorptionAmount()) + "/" + ExHUD.FORMAT.format(riddenEntity.getMaxHealth());
		float healthPos = ((float)this.screenWidth - (float)this.minecraft.font.width(healthbar)) * 0.5F;
		float s = 1.0F / 0.7F;

		this.exhud$renderEntityTemperature(ctx, riddenEntity, x, y);

		PoseStack matrices = ctx.pose();
		
		matrices.pushPose();
		matrices.scale(0.7F, 0.7F, 0.7F);
		
		ExHUD.drawBorderedText(ctx, this.minecraft.font, healthbar, s, healthPos + 56.0F, y + 1.5F, 0xFFFFFF, 0x000000);
		
		matrices.popPose();
		ci.cancel();
	}
	
	// Removes vanilla mount jump bar and renders our own.
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderJumpMeter(Lnet/minecraft/world/entity/PlayerRideableJumping;Lnet/minecraft/client/gui/GuiGraphics;I)V"))
	private void exhud$renderMountJumpBar(Gui instance, PlayerRideableJumping rideable, GuiGraphics ctx, int x, Operation<Void> original) {
		if (ExHUD.renderCustomJumpBar()) {
			if (this.minecraft.player != null) {
				float f = this.minecraft.player.getJumpRidingScale();
				int j = (int) (f * 183.0F);

				ctx.blit(ExHUD.GUI_LEVEL_BARS, x, this.screenHeight - 27, 0, 9, 182, 3, 256, 16);

				if (rideable.getJumpCooldown() > 0) {
					ctx.blit(ExHUD.GUI_LEVEL_BARS, x, this.screenHeight - 27, 0, 6, 182, 3, 256, 16);
				} else if(j > 0) {
					ctx.blit(ExHUD.GUI_LEVEL_BARS, x, this.screenHeight - 27, 0, 12, j, 3, 256, 16);
				}
			}
		}
		else {
			original.call(instance, rideable, ctx, x);
		}
	}
	
	// Removed vanilla experience bar and level text
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V"))
	private void exhud$renderExperienceBar(Gui instance, GuiGraphics ctx, int x, Operation<Void> original) {
		if (ExHUD.renderCustomExperienceBar()) {
			Player player = this.getCameraPlayer();

			int l = (int) (183.0F * player.experienceProgress);

			ctx.blit(ExHUD.GUI_LEVEL_BARS, (this.screenWidth / 2) - 91, this.screenHeight - 27, 0, 0, 182, 3, 256, 16);
			ctx.blit(ExHUD.GUI_LEVEL_BARS, (this.screenWidth / 2) - 91, this.screenHeight - 27, 0, 3, l, 3, 256, 16);

			if(player.experienceLevel > 0) {
				Font textRenderer = this.minecraft.font;

				String level = String.valueOf(player.experienceLevel);
				int levelPos = (this.screenWidth - textRenderer.width(level)) / 2;

				PoseStack matrices = ctx.pose();

				matrices.pushPose();

				ctx.drawString(textRenderer, level, (levelPos + 1), (this.screenHeight - 36), 0x000000, false);
				ctx.drawString(textRenderer, level, (levelPos - 1), (this.screenHeight - 36), 0x000000, false);
				ctx.drawString(textRenderer, level, levelPos, (this.screenHeight - 35), 0x000000, false);
				ctx.drawString(textRenderer, level, levelPos, (this.screenHeight - 37), 0x000000, false);
				ctx.drawString(textRenderer, level, levelPos, (this.screenHeight - 36), 8453920, false);

				matrices.popPose();
			}
		}
		else {
			original.call(instance, ctx, x);
		}
	}

	// Removes the vanilla armor bar.
	@ModifyVariable(method = "renderPlayerHealth", at = @At(value = "STORE", target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"), ordinal = 11)
	private int exhud$getArmor(int u) { return ExHUD.renderCustomUtilities() ? 0 : u; }
	
	// Removes the vanilla food and air bars and renders our own.
	@Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getPlayerVehicleWithHealth()Lnet/minecraft/world/entity/LivingEntity;"), cancellable = true)
	private void exhud$renderStatusBars(GuiGraphics ctx, CallbackInfo ci) {
		if(!ExHUD.renderCustomUtilities()) return;
		
		LivingEntity riddenEntity = this.getPlayerVehicleWithHealth();
		
		if(riddenEntity != null && this.getVehicleMaxHearts(riddenEntity) > 0) {
			ci.cancel();
			return;
		}
		
		Player player = this.getCameraPlayer();
		FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel() * 5;
        int airLevel = (int)(100.0F * Math.max((float)player.getAirSupply(), 0.0F) / (float)player.getMaxAirSupply());
        int armor = player.getArmorValue();
        boolean hunger = player.hasEffect(MobEffects.HUNGER);

        ctx.blit(ExHUD.VANILLA_GUI_ICONS_TEXTURE, (this.screenWidth / 2) + 12, this.screenHeight - 38, hunger ? 133 : 16, 27, 9, 9, 256, 256);
        ctx.blit(ExHUD.VANILLA_GUI_ICONS_TEXTURE, (this.screenWidth / 2) + 12, this.screenHeight - 38, hunger ? 88 : 52, 27, 9, 9, 256, 256);
        ctx.blit(ExHUD.VANILLA_GUI_ICONS_TEXTURE, (this.screenWidth / 2) + (airLevel < 100 ? 44 : 50), this.screenHeight - 38, 34, 9, 9, 9, 256, 256);
        
        if(airLevel < 100) {
        	ctx.blit(ExHUD.VANILLA_GUI_ICONS_TEXTURE, (this.screenWidth / 2) + (armor < 10 ? 66 : (armor < 100 ? 70 : 76)), this.screenHeight - 38, 16, 18, 9, 9, 256, 256);
        }
        
        var mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        var offHandStack = player.getItemInHand(InteractionHand.OFF_HAND);
        
        int itemFoodLevel;
		FoodProperties mainFoodProps = mainHandStack.getItem().getFoodProperties();
        if (mainFoodProps != null && mainHandStack.isEdible()) {
			itemFoodLevel = mainFoodProps.getNutrition();
		}
        else {
			FoodProperties offFoodProps = offHandStack.getItem().getFoodProperties();
			if (offFoodProps != null && offHandStack.isEdible()) {
				itemFoodLevel = offFoodProps.getNutrition();
			}
			else {
				itemFoodLevel = 0;
			}
		}
        int combinedFoodLevel = Math.min(100, foodLevel + (itemFoodLevel * 5));
        
        float s = 1.0F / 0.7F;

		PoseStack matrices = ctx.pose();
		
		matrices.pushPose();
		matrices.scale(0.7F, 0.7F, 0.7F);
		
		ExHUD.drawBorderedText(ctx, this.minecraft.font, "x" + armor, s, (this.screenWidth / 2.0F) + (airLevel < 100 ? 54.0F : 60.0F), this.screenHeight - 36.0F, 0xFFFFFF, 0x000000);
		
		if(ExHUD.enableFoodStat() && foodLevel < 100 && itemFoodLevel > 0) {
			int tick = (int)((System.currentTimeMillis() / 50L) % 20L);
	        int rate = (int)(((255.0F * Math.sin(Math.toRadians(18 * tick))) + 255.0F) * 0.5F);
	        int white = 0xFFFFFF;
	        int black = 0x000000;
	        
			if(rate > 8) {
				int alpha = rate << 24 & -white;
				
				ExHUD.drawBorderedText(ctx, this.minecraft.font, combinedFoodLevel + "%", s, (this.screenWidth / 2.0F) + 22.0F, this.screenHeight - 36.0F, white | alpha, black | alpha);
			}
		} else {
			ExHUD.drawBorderedText(ctx, this.minecraft.font, foodLevel + "%", s, (this.screenWidth / 2.0F) + 22.0F, this.screenHeight - 36.0F, 0xFFFFFF, 0x000000);
		}
		
		if(airLevel < 100) {
			ExHUD.drawBorderedText(ctx, this.minecraft.font, airLevel + "%", s, (this.screenWidth / 2.0F) + (armor < 10 ? 76.0F : (armor < 100 ? 80.0F : 86.0F)), this.screenHeight - 36.0F, 0xFFFFFF, 0x000000);
		}
		
		matrices.popPose();
		ci.cancel();
	}
	
	// Removes the vanilla health bar and renders our own.
	@Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
	private void exhud$renderHearts(GuiGraphics context, Player player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
		if(!ExHUD.renderCustomHealthbar()) return;

		int healthBarWidth = (int) Math.min(MAX_HEALTH_BAR_WIDTH / player.getMaxHealth() * player.getHealth(), MAX_HEALTH_BAR_WIDTH);

		context.blit(ExHUD.GUI_HEALTH_BARS, x, y + 2, 0, 0, (int) MAX_HEALTH_BAR_WIDTH, 8, 128, 64);
		context.blit(ExHUD.GUI_HEALTH_BARS, x, y + 2, 0, ExHUD.healthbarTexture(player).getXPos(), healthBarWidth, 8, 128, 64);

		this.exhud$renderEntityTemperature(context, player, x, y);

		if(absorption > 0) {
			context.blit(ExHUD.GUI_HEALTH_BARS, x, y + 2, 0, HealthBarTexture.Absorption.getXPos(), healthBarWidth, 8, 128, 64);
		}

		String healthbar = ExHUD.FORMAT.format(player.getHealth() + player.getAbsorptionAmount()) + "/" + ExHUD.FORMAT.format(player.getMaxHealth());
		float healthPos = ((2.0F * ((float)x + 91.0F)) - (float)this.minecraft.font.width(healthbar)) * 0.5F;
		float s = 1.0F / 0.7F;

		PoseStack matrices = context.pose();
		
		matrices.pushPose();
		matrices.scale(0.7F, 0.7F, 0.7F);
		
		ExHUD.drawBorderedText(context, this.minecraft.font, healthbar, s, healthPos - 48.0F, y + 3.5F, 0xFFFFFF, 0x000000);
		
		matrices.popPose();
		ci.cancel();
	}

	@Unique
	private void exhud$renderEntityTemperature(GuiGraphics ctx, LivingEntity entity, int x, int y) {
		if (CompatUtil.isThermooLoaded() && (entity.thermoo$isCold() || entity.thermoo$isWarm())) {
			HealthBarTexture texture = null;
			float afflictionWidth = 0;

			int minTemp = entity.thermoo$getMinTemperature();
			int maxTemp = entity.thermoo$getMaxTemperature();

			if (entity.thermoo$isCold() && minTemp != 0) {
				texture = HealthBarTexture.Cold;
				afflictionWidth = Math.min(MAX_HEALTH_BAR_WIDTH / Mth.abs(minTemp) * Mth.abs(entity.thermoo$getTemperature()), MAX_HEALTH_BAR_WIDTH);
			}
			else if (entity.thermoo$isWarm() && maxTemp != 0) {
				texture = HealthBarTexture.Warm;
				afflictionWidth = Math.min(MAX_HEALTH_BAR_WIDTH / maxTemp * entity.thermoo$getTemperature(), MAX_HEALTH_BAR_WIDTH);
			}

			if (texture != null) {
				ctx.blit(ExHUD.GUI_HEALTH_BARS, x, y + 2, 0, texture.getXPos(), (int) afflictionWidth, 8, 128, 64);
			}
		}
	}
}
