package com.autopot.client;

import com.autopot.AutoPot;
import com.autopot.AutoPotConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.potion.Potion;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Optional;

public class AutoPotHandler {

    public enum PotionRequest {
        FIRE_RESISTANCE("Fire Resistance"),
        STRENGTH("Strength"),
        SPEED("Speed");

        private final String label;
        PotionRequest(String label) { this.label = label; }
        public String label() { return label; }
    }

    private final MinecraftClient client;
    private final Queue<PotionRequest> queue = new ArrayDeque<>();

    private int cooldownTicks = 0;
    private int returnSlot = -1;
    private Float storedPitch = null;

    public AutoPotHandler(MinecraftClient client) {
        this.client = client;
    }

    public void triggerThrows(AutoPotConfig config) {
        queue.clear();
        if (config.isUseFireResistance()) queue.add(PotionRequest.FIRE_RESISTANCE);
        if (config.isUseStrength())       queue.add(PotionRequest.STRENGTH);
        if (config.isUseSpeed())          queue.add(PotionRequest.SPEED);

        if (queue.isEmpty()) {
            notifyPlayer(Text.literal("AutoPot has no enabled potions."));
            return;
        }

        var player = client.player;
        if (player == null) return;

        if (config.isAutoLookDown()) {
            storedPitch = player.getPitch();
            player.setPitch(90f);
        }
    }

    public void tick(AutoPotConfig config) {
        var player = client.player;
        if (player == null) return;

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        if (queue.isEmpty()) {
            // Restore state if we stored pitch
            if (storedPitch != null) {
                player.setPitch(storedPitch);
                storedPitch = null;
            }
            if (returnSlot >= 0) {
                player.getInventory().selectedSlot = returnSlot;
                returnSlot = -1;
            }
            return;
        }

        PotionRequest request = queue.peek();
        int slot = findPotionSlot(request);

        if (slot == -1) {
            notifyPlayer(Text.literal("Missing " + request.label() + " potion in your hotbar."));
            queue.poll();
            return;
        }

        if (returnSlot < 0) {
            returnSlot = player.getInventory().selectedSlot;
        }

        usePotion(slot);
        queue.poll();
        cooldownTicks = 5; // small gap between throws
    }

    private void usePotion(int slot) {
        var player = client.player;
        if (player == null || client.interactionManager == null) return;

        player.getInventory().selectedSlot = slot;
        ActionResult result = client.interactionManager.interactItem(player, Hand.MAIN_HAND);
        if (result == ActionResult.FAIL) {
            AutoPot.LOGGER.warn("Failed to use potion in slot {}", slot);
        }
    }

    private int findPotionSlot(PotionRequest request) {
        var player = client.player;
        if (player == null) return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isMatchingPotion(stack, request)) return i;
        }
        return -1;
    }

    private boolean isMatchingPotion(ItemStack stack, PotionRequest request) {
        // Must be a splash or lingering potion
        if (stack.getItem() != Items.SPLASH_POTION && stack.getItem() != Items.LINGERING_POTION) {
            return false;
        }

        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (contents == null) return false;

        Optional<RegistryEntry<Potion>> potionOpt = contents.potion();
        if (potionOpt.isEmpty()) return false;

        Potion potion = potionOpt.get().value();

        return switch (request) {
            case FIRE_RESISTANCE -> potion == Potions.FIRE_RESISTANCE.value() || potion == Potions.LONG_FIRE_RESISTANCE.value();
            case STRENGTH        -> potion == Potions.STRENGTH.value() || potion == Potions.LONG_STRENGTH.value() || potion == Potions.STRONG_STRENGTH.value();
            case SPEED           -> potion == Potions.SWIFTNESS.value() || potion == Potions.LONG_SWIFTNESS.value() || potion == Potions.STRONG_SWIFTNESS.value();
        };
    }

    private void notifyPlayer(Text message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[AutoPot] ").append(message), false);
        }
        AutoPot.LOGGER.info(message.getString());
    }
}
