package me.cioco.antihit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FriendManager implements ModInitializer {

    private static final List<String> friendsList = new ArrayList<>();
    private static final Path friendsListFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "friend_list.json").toPath();

    private static KeyBinding addFriendKeyBinding;

    private static KeyBinding removeFriendKeyBinding;

    private static boolean allowAttacksOnFriends = false;

    private static KeyBinding toggleFriendshipKeyBinding;

    @Override
    public void onInitialize() {

        addFriendKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.friendlist.addfriend",
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.friendlist"
        ));

        removeFriendKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.friendlist.removefriend",
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.friendlist"
        ));

        toggleFriendshipKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.friendlist.togglefriendship",
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.friendlist"
        ));

        loadFriendsList();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == Hand.MAIN_HAND && player != null && entity instanceof PlayerEntity) {
                PlayerEntity targetPlayer = (PlayerEntity) entity;
                if (isFriend(targetPlayer) && !allowAttacksOnFriends) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity target = getTargetedPlayer();
            if (target != null) {
                if (addFriendKeyBinding.wasPressed()) {
                    addFriend(target);
                } else if (removeFriendKeyBinding.wasPressed()) {
                    removeFriend(target);
                } else if (toggleFriendshipKeyBinding.wasPressed()) {
                    allowAttacksOnFriends = !allowAttacksOnFriends;
                    String message = allowAttacksOnFriends ? "Attacks on friends are now allowed." : "Attacks on friends are now blocked.";
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§b" + message));
                }
            }
        });

    }

    private static PlayerEntity getTargetedPlayer() {
        if (MinecraftClient.getInstance().crosshairTarget instanceof EntityHitResult) {
            Entity targetEntity = ((EntityHitResult) MinecraftClient.getInstance().crosshairTarget).getEntity();

            if (targetEntity instanceof PlayerEntity) {
                return (PlayerEntity) targetEntity;
            }
        }
        return null;
    }

    private static void addFriend(PlayerEntity player) {
        String playerName = player.getName().getString();
        if (!friendsList.contains(playerName)) {
            friendsList.add(playerName);
            String message = "Added " + playerName + " to friends list.";
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§a" + message));
            saveFriendsList();
        } else {
            String message = playerName + " is already a friend.";
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§e" + message));
        }
    }

    private static void removeFriend(PlayerEntity player) {
        String playerName = player.getName().getString();
        if (friendsList.contains(playerName)) {
            friendsList.remove(playerName);
            String message = "Removed " + playerName + " from friends list.";
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§c" + message));
            saveFriendsList();
        }else {
            String message = playerName + " is not added as a friend.";
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§e" + message));
        }
    }

    public static boolean isFriend(PlayerEntity player) {
        return friendsList.contains(player.getName().getString());
    }

    private static void loadFriendsList() {
        try {
            if (Files.exists(friendsListFile)) {
                friendsList.addAll(Files.readAllLines(friendsListFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveFriendsList() {
        try {
            Files.write(friendsListFile, friendsList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}