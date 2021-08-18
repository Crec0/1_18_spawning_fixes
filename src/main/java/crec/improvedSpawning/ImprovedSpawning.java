package crec.improvedSpawning;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;


public class ImprovedSpawning implements ModInitializer {

    private static final float SQRT_TWO_PI = MathHelper.sqrt(MathHelper.TAU);
    private static final float EULER_NUMBER = 2.7182818F;

    public static SpawningAlgorithm activeAlgorithm = SpawningAlgorithm.VANILLA;

    public static final float[] SPAWN_PERCENTAGES = Util.make(new float[9], (spawnChances) -> {
        for (int i = 0; i < spawnChances.length; i++) {
            spawnChances[i] = ImprovedSpawning.getChance(i);
        }
    });

    @Override
    public void onInitialize() {}

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("improvedSpawning");

        for (SpawningAlgorithm algorithm: SpawningAlgorithm.values()){
            literalArgumentBuilder
                    .then(CommandManager.literal(algorithm.name)
                    .executes(context -> {
                        activeAlgorithm = algorithm;
                        sendMessageToPlayer(getPlayer(context), stringify("Algorithm changed to", algorithm.repr));
                        return 1;
                    }));
        }
        literalArgumentBuilder
                .executes(context -> {
                    sendMessageToPlayer(getPlayer(context), stringify("Current active algorithm:", activeAlgorithm.repr));
                    return 1;
                });
        dispatcher.register(literalArgumentBuilder);
    }

    private static LiteralText stringify(String... args){
        LiteralText output = new LiteralText("");
        for (String arg: args){
            output.append(arg + " ");
        }
        return output;
    }

    private static PlayerEntity getPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return context.getSource().getPlayer();
    }

    private static void sendMessageToPlayer(PlayerEntity playerEntity, LiteralText text) {
        if (playerEntity != null){
            playerEntity.sendMessage(text, false);
        }
    }

    private static float getChance(int val) {
        float multi = 4.0F;
        float sd = 3.19F;
        float mean = 0.0F;

        float d = (val - mean) / sd;
        return (float) (multi / (sd * SQRT_TWO_PI) * Math.pow(EULER_NUMBER, -1.0F / sd * d * d));
    }

    public enum SpawningAlgorithm {
        VANILLA("Vanilla", "vanilla"),
        VANILLA_EMPTY_SUBCHUNK_OPTIMIZATION("Vanilla empty subchunk optimization", "vanillaEmptySubchunkOptimization"),
        DISTRIBUTION_BASED_SPAWNING("Normal distribution spawning", "normalDistributionSpawning"),
        VANILLA_OLD_SPAWNING("Old heightmap based spawning", "oldVanilla");

        private final String repr;
        private final String name;

        SpawningAlgorithm(String repr, String name){
            this.repr = repr;
            this.name = name;
        }
    }
}