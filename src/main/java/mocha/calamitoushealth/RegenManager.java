package mocha.calamitoushealth;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;

import java.util.Map;
import java.util.WeakHashMap;

public class RegenManager {
    private static final Map<PlayerEntity, Integer> REGEN_TICKS = new WeakHashMap<>();
    private static final Map<PlayerEntity, Integer> REGEN_CURRENT_DELAY = new WeakHashMap<>();

    static {
        Config.load();
    }

    public static void resetRegenTimer(PlayerEntity player) {
        if (player == null) return;
        REGEN_CURRENT_DELAY.put(player, Config.REGEN_INITIAL_DELAY);
        REGEN_TICKS.put(player, 0);
    }

    public static void tickPlayer(PlayerEntity player) {
        if (player == null) return;
        if (player.getWorld() == null || player.getWorld().isClient) return;
        if (!player.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) return;

        // If player is at max health, reset delay and skip regen
        if (!(player.getHealth() < player.getMaxHealth())) {
            resetRegenTimer(player);
            return;
        }

        int currentDelay = REGEN_CURRENT_DELAY.getOrDefault(player, Config.REGEN_INITIAL_DELAY);
        int ticks = REGEN_TICKS.getOrDefault(player, 0) + 1;

        if (ticks >= currentDelay) {
            if (player.isAlive()) {
                player.heal(1.f);
                int nextDelay = Math.max(Config.REGEN_MIN_DELAY, (int)(currentDelay / Config.REGEN_DELAY_SCALE));
                REGEN_CURRENT_DELAY.put(player, nextDelay);
            }
            ticks = 0;
        }

        REGEN_TICKS.put(player, ticks);
    }
}
