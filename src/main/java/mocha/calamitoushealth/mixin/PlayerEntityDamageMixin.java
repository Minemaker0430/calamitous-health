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
    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // reset the regen timer so regeneration waits the initial delay after being damaged
        RegenManager.resetRegenTimer((PlayerEntity)(Object)this);

        // apply armor penalty for non-environmental damage
        try {
            PlayerEntity player = (PlayerEntity)(Object)this;
            if (!player.canTakeDamage()) return;
            if (source == null) return;
            if (
                source.isOf(DamageTypes.DROWN) ||
                source.isOf(DamageTypes.CACTUS) ||
                source.isOf(DamageTypes.DROWN) ||
                source.isOf(DamageTypes.DRY_OUT) ||
                source.isOf(DamageTypes.FALL) ||
                source.isOf(DamageTypes.HOT_FLOOR) ||
                source.isOf(DamageTypes.IN_FIRE) ||
                source.isOf(DamageTypes.IN_WALL) ||
                source.isOf(DamageTypes.LAVA) ||
                source.isOf(DamageTypes.ON_FIRE) ||
                source.isOf(DamageTypes.OUTSIDE_BORDER) ||
                source.isOf(DamageTypes.OUT_OF_WORLD) ||
                source.isOf(DamageTypes.STARVE) ||
                source.isOf(DamageTypes.SWEET_BERRY_BUSH)
            ) { return; }

            float baseDamage = amount;

            // reduce penalty by armor toughness
            float toughness = (float) player.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
            float penalty = Math.max(0f, baseDamage - (toughness / 2.f));
            if (penalty > 0f && Config.ARMOR_PENALTY) ArmorPenaltyManager.addPenalty(player, penalty);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
