package me.crec.spawnboost;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;


public class SpawnBoost implements ModInitializer {

    private static final float SQRT_TWO_PI = MathHelper.sqrt(MathHelper.TAU);
    private static final float EULER_NUMBER = 2.7182818F;

    public static boolean vanillaPlusPlusSpawning = true;

    public static final float[] SPAWN_PERCENTAGES = Util.make(new float[17], (spawnchances) -> {
        for (int i = 0; i < spawnchances.length; i++) {
            spawnchances[i] = SpawnBoost.getChance(i - 8);
        }
    });

    @Override
    public void onInitialize() {}

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("brr")
                .executes(c -> {
                    vanillaPlusPlusSpawning = !vanillaPlusPlusSpawning;
                    c.getSource().getPlayer().sendMessage(new LiteralText("Vanilla++ " + (vanillaPlusPlusSpawning ? "brrr" : "not brrr")), false);
                    return 1;
                });
        dispatcher.register(literalArgumentBuilder);
    }

    private static float getChance(int val) {
        float multi = 4.0F;
        float sd = 3.19F;
        float mean = 0.0F;

        float d = (val - mean) / sd;
        return (float) (multi / (sd * SQRT_TWO_PI) * Math.pow(EULER_NUMBER, -1.0F / sd * d * d));
    }
}
