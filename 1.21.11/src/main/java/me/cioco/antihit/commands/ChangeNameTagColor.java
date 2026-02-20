package me.cioco.antihit.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ChangeNameTagColor {

    private static Formatting nameTagColor = Formatting.GREEN;
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("antihit_config.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static {
        load();
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("antihit")
                .then(ClientCommandManager.argument("color", StringArgumentType.word())
                        .executes(ChangeNameTagColor::changeNameTagColor)));
    }

    private static int changeNameTagColor(CommandContext<FabricClientCommandSource> context) {
        String colorArg = StringArgumentType.getString(context, "color");

        try {
            nameTagColor = Formatting.valueOf(colorArg.toUpperCase());
            save();
            context.getSource().sendFeedback(Text.literal("Name tag color changed to " + colorArg).formatted(nameTagColor));
        } catch (IllegalArgumentException e) {
            context.getSource().sendFeedback(Text.literal("Invalid color!").formatted(Formatting.RED));
        }

        return 1;
    }

    public static Formatting getNameTagColor() {
        return nameTagColor;
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(new ConfigData(nameTagColor.getName()), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void load() {
        if (!CONFIG_FILE.exists()) return;

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null && data.color != null) {
                nameTagColor = Formatting.byName(data.color.toLowerCase());
                if (nameTagColor == null) nameTagColor = Formatting.GREEN;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ConfigData {
        String color;
        ConfigData(String color) { this.color = color; }
    }
}