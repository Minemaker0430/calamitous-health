package mocha.calamitoushealth.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import mocha.calamitoushealth.Config;
import mocha.calamitoushealth.RegenManager;
import mocha.calamitoushealth.ArmorPenaltyManager;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
	@Inject(at = @At("HEAD"), method = "update")
	private void init(PlayerEntity player, CallbackInfo info) {
		if (Config.HUNGER_INDEPENDENT_REGEN) RegenManager.tickPlayer(player);
		if (Config.ARMOR_PENALTY) ArmorPenaltyManager.tickPlayer(player);
	}

	// Prevent HungerManager.update from calling PlayerEntity.heal(float)
	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V"))
	private void suppressHungerHeal(PlayerEntity player, float amount) {
		if (!Config.HUNGER_INDEPENDENT_REGEN) player.heal(amount);
	}

	// Prevent exhaustion being added from HungerManager.update (healing-based exhaustion)
	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"))
	private void suppressHungerAddExhaustion(HungerManager manager, float amount) {
		if (!Config.HUNGER_INDEPENDENT_REGEN) manager.addExhaustion(amount);
	}

}