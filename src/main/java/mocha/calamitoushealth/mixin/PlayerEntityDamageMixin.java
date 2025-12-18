package mocha.calamitoushealth.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.attribute.EntityAttributes;
import mocha.calamitoushealth.RegenManager;
import mocha.calamitoushealth.ArmorPenaltyManager;
import mocha.calamitoushealth.Config;

@Mixin(PlayerEntity.class)
public class PlayerEntityDamageMixin {
    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        try {
            // only apply when damage was actually applied and the player can be damaged
            if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
            if (amount <= 0f) return;
            if (source == null) return;
            if (player.isInvulnerableTo(source) || player.isInvulnerable() || !player.canTakeDamage()) return;

            // reset the regen timer now that damage was applied
            RegenManager.resetRegenTimer(player);

            // filter environmental damage types (don't apply penalty for these)
            if (
                source.isOf(DamageTypes.CACTUS) ||
                source.isOf(DamageTypes.DROWN) ||
                source.isOf(DamageTypes.DRY_OUT) ||
                source.isOf(DamageTypes.FALL) ||
                source.isOf(DamageTypes.FREEZE) ||
                source.isOf(DamageTypes.HOT_FLOOR) ||
                source.isOf(DamageTypes.IN_FIRE) ||
                source.isOf(DamageTypes.IN_WALL) ||
                source.isOf(DamageTypes.LAVA) ||
                source.isOf(DamageTypes.ON_FIRE) ||
                source.isOf(DamageTypes.OUTSIDE_BORDER) ||
                source.isOf(DamageTypes.OUT_OF_WORLD) ||
                source.isOf(DamageTypes.STARVE) ||
                source.isOf(DamageTypes.SWEET_BERRY_BUSH) ||
                source.isOf(DamageTypes.WITHER)
            ) { return; }

            float baseDamage = amount;

            // reduce penalty by armor toughness (half effect here)
            float toughness = (float) player.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
            float penalty = Math.max(0f, (baseDamage - (toughness / 2f)) / 2f);
            if (penalty > 0f && Config.ARMOR_PENALTY) ArmorPenaltyManager.addPenalty(player, penalty);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
