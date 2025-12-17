package mocha.calamitoushealth;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {
    // Defaults
    public static int REGEN_INITIAL_DELAY = 60;
    public static int REGEN_MIN_DELAY = 20;
    public static float REGEN_DELAY_SCALE = 1.1F;
    public static int PENALTY_REGEN_EASY = 0;
    public static int PENALTY_REGEN_NORMAL = 10;
    public static int PENALTY_REGEN_HARD = 20;
    public static boolean ARMOR_PENALTY = true;
    public static boolean HUNGER_INDEPENDENT_REGEN = true;

    private static final String CONFIG_PATH = "config/calamitous-health.json";

    public static void load() {
        try {
            File cfg = new File(CONFIG_PATH);
            if (!cfg.exists()) {
                // create parent dir if needed
                cfg.getParentFile().mkdirs();
                String defaultJson = getDefaultJson();
                Files.write(cfg.toPath(), defaultJson.getBytes(StandardCharsets.UTF_8));
            }

            String json = Files.readString(cfg.toPath(), StandardCharsets.UTF_8);
            // parse values (simple, tolerant parser expecting flat JSON)
            REGEN_INITIAL_DELAY = parseInt(json, "initial_regen_delay", REGEN_INITIAL_DELAY);
            REGEN_MIN_DELAY = parseInt(json, "minimum_regen_delay", REGEN_MIN_DELAY);
            REGEN_DELAY_SCALE = parseFloat(json, "regen_delay_scaling", REGEN_DELAY_SCALE);
            PENALTY_REGEN_EASY = parseInt(json, "armor_penalty_regen_easy", PENALTY_REGEN_EASY);
            PENALTY_REGEN_NORMAL = parseInt(json, "armor_penalty_regen_normal", PENALTY_REGEN_NORMAL);
            PENALTY_REGEN_HARD = parseInt(json, "armor_penalty_regen_hard", PENALTY_REGEN_HARD);
            ARMOR_PENALTY = parseBool(json, "armor_penalty_enabled", ARMOR_PENALTY);
            HUNGER_INDEPENDENT_REGEN = parseBool(json, "hunger_independent_regen", HUNGER_INDEPENDENT_REGEN);
        } catch (IOException e) {
            // if config can't be read, leave defaults and print stack for debugging
            e.printStackTrace();
        }
    }

    private static boolean parseBool(String json, String key, boolean def) {
        // regex: "KEY"\s*:\s*([0-9]+(?:\.[0-9]+)?)
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(json);
        if (m.find()) {
            try {
                return Boolean.parseBoolean(m.group(1));
            } catch (Exception ignored) {}
        }
        return def;
    }

    private static int parseInt(String json, String key, int def) {
        Double v = parseNumber(json, key);
        return v == null ? def : v.intValue();
    }

    private static float parseFloat(String json, String key, float def) {
        Double v = parseNumber(json, key);
        return v == null ? def : v.floatValue();
    }

    private static Double parseNumber(String json, String key) {
        // regex: "KEY"\s*:\s*([0-9]+(?:\.[0-9]+)?)
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(json);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static String getDefaultJson() {
        return "{\n" +
                "  \"initial_regen_delay\": " + REGEN_INITIAL_DELAY + ",\n" +
                "  \"minimum_regen_delay\": " + REGEN_MIN_DELAY + ",\n" +
                "  \"regen_delay_scaling\": " + REGEN_DELAY_SCALE + ",\n" +
                "  \"armor_penalty_regen_easy\": " + PENALTY_REGEN_EASY + ",\n" +
                "  \"armor_penalty_regen_normal\": " + PENALTY_REGEN_NORMAL + ",\n" +
                "  \"armor_penalty_regen_hard\": " + PENALTY_REGEN_HARD + "\n" +
                "  \"armor_penalty_enabled\": " + ARMOR_PENALTY + "\n" +
                "  \"hunger_independent_regen\": " + HUNGER_INDEPENDENT_REGEN + "\n" +
                "}\n";
    }
}
