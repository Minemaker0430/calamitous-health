package mocha.calamitoushealth;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.Difficulty;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.UUID;

public class ArmorPenaltyManager {
    private static final Map<PlayerEntity, Float> PENALTIES = new WeakHashMap<>();
    private static final Map<PlayerEntity, Integer> REGEN_TICKS = new WeakHashMap<>();

    private static final UUID MODIFIER_UUID_BASE = UUID.fromString("2b4d6f2a-7f6a-4d9a-9b6f-9c3a8b3d6f1a");

    public static void addPenalty(PlayerEntity player, float amount) {
        if (player == null || amount <= 0f) return;
        if (getRegenTicksForDifficulty(player.getWorld().getDifficulty()) == 0) {
            PENALTIES.remove(player);
            removeModifier(player);
            return;
        }

        EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (inst == null) return;

        // compute new penalty and clamp so armor won't go below 0
        float current = PENALTIES.getOrDefault(player, 0f);
        float updated = current + amount;
        try {
            double currentEffective = player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
            float allowed = (float) currentEffective + current; // armor without our modifier
            if (allowed < 0f) allowed = 0f;
            if (updated > allowed) updated = allowed;
        } catch (Throwable t) {}

        PENALTIES.put(player, updated);
        REGEN_TICKS.put(player, 0);

        applyModifier(player, updated);
    }

    public static void tickPlayer(PlayerEntity player) {
        if (player == null) return;
        if (player.getWorld() == null || player.getWorld().isClient) return;
        float penalty = PENALTIES.getOrDefault(player, 0f);
        if (penalty <= 0f) return;

        int regenTicks = getRegenTicksForDifficulty(player.getWorld().getDifficulty());
        if (regenTicks <= 0) return; // regen turned off for this difficulty

        int ticks = REGEN_TICKS.getOrDefault(player, 0) + 1;
        if (ticks > regenTicks) {
            penalty -= 0.5f;
            ticks = 0;
            if (penalty <= 0f) {
                PENALTIES.remove(player);
                removeModifier(player);
            } else {
                PENALTIES.put(player, penalty);
                applyModifier(player, penalty);
            }
        }

        if (player.getHealth() <= 0.f) { // remove the modifier if the player dies
            PENALTIES.remove(player);
            removeModifier(player);
        }

        REGEN_TICKS.put(player, ticks);
    }

    private static int getRegenTicksForDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return Config.PENALTY_REGEN_EASY;
            case NORMAL:
                return Config.PENALTY_REGEN_NORMAL;
            case HARD:
                return Config.PENALTY_REGEN_HARD;
            default:
                return 0; // disabled completely in peaceful since you can't die to anything other than the environment
        }
    }

    private static void applyModifier(PlayerEntity player, float penalty) {
        try {
            EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            if (inst == null) return;
            if (penalty <= 0f) return;

            UUID modUuid = UUID.nameUUIDFromBytes((MODIFIER_UUID_BASE.toString() + player.getUuidAsString()).getBytes());
            // remove existing
            if (inst.getModifier(modUuid) != null) inst.removeModifier(modUuid);

            // clamp modifier so effective armor does not go below zero
            double currentEffective = player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
            float previous = PENALTIES.getOrDefault(player, 0f);
            float allowed = (float) currentEffective + previous;
            if (allowed < 0f) allowed = 0f;
            float toApply = Math.min(penalty, allowed);
            if (toApply <= 0f) return;

            // subtract penalty from armor by applying negative modifier
            inst.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(modUuid, "calamity_armor_penalty", -toApply, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADDITION));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void removeModifier(PlayerEntity player) {
        try {
            EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            if (inst == null) return;
            UUID modUuid = UUID.nameUUIDFromBytes((MODIFIER_UUID_BASE.toString() + player.getUuidAsString()).getBytes());
            if (inst.getModifier(modUuid) != null) inst.removeModifier(modUuid);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
