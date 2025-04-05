package me.cioco.antihit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChangeNameTagColor {

    private static Formatting nameTagColor = Formatting.GREEN;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("antihit")
                .then(ClientCommandManager.argument("color", StringArgumentType.word())
                        .executes(ChangeNameTagColor::changeNameTagColor)));
    }

    private static int changeNameTagColor(CommandContext<FabricClientCommandSource> context) {
        String colorArg = StringArgumentType.getString(context, "color");

        try {
            nameTagColor = Formatting.valueOf(colorArg.toUpperCase());
            context.getSource().sendFeedback(Text.literal("Name tag color changed to " + colorArg).formatted(Formatting.GREEN));
        } catch (IllegalArgumentException e) {
            context.getSource().sendFeedback(Text.literal("Invalid color!").formatted(Formatting.RED));
        }

        return 1;
    }

    public static Formatting getNameTagColor() {
        return nameTagColor;
    }
}
