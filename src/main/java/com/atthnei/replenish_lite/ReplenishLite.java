package com.atthnei.replenish_lite;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class ReplenishLite implements ModInitializer {

    public static final String MOD_ID = "replenish-lite";

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final ExtendedKeyBinding replenishKeyBinding = (ExtendedKeyBinding) KeyBindingHelper.registerKeyBinding(new ExtendedKeyBinding(
            MOD_ID + ".key.replenish-key",
            GLFW.GLFW_KEY_R,
            MOD_ID + ".key.categories"));

    private int slotIndexBeforePress = 0;

    @Override
    public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(MinecraftClient client) {
        KeyBindingPressed(client, replenishKeyBinding);
    }

    private void KeyBindingPressed(MinecraftClient client, ExtendedKeyBinding keyBinding) {
        assert client.player != null;

        keyBinding.onKeyDown(() -> {
            PlayerInventory inventory = client.player.getInventory();

            slotIndexBeforePress = inventory.selectedSlot;
            int hotbarIndex = GetHotbarIndexWithFood(inventory);

            if (hotbarIndex != -1) {
                inventory.selectedSlot = hotbarIndex;
                client.options.keyUse.setPressed(true);
                return true;
            }
            return false;
        });

        keyBinding.onKeyUp(() -> {
            client.options.keyUse.setPressed(false);
            client.player.getInventory().selectedSlot = slotIndexBeforePress;
            return false;
        });
    }

    private int GetHotbarIndexWithFood(PlayerInventory inventory) {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inventory.getStack(i);

            if (itemStack.isFood()) {
                return i;
            }
        }

        return -1;
    }
}
