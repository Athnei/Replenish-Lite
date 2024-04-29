package com.atthnei.replenish_lite;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.IntStream;

public class ReplenishLite implements ModInitializer {

    public static final String MOD_ID = "replenish-lite";

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
                client.options.useKey.setPressed(true);
                return true;
            }
            return false;
        });

        keyBinding.onKeyUp(() -> {
            client.options.useKey.setPressed(false);
            client.player.getInventory().selectedSlot = slotIndexBeforePress;
            return false;
        });
    }

    private int GetHotbarIndexWithFood(PlayerInventory inventory) {

        ItemStack currentSlotItem = inventory.getStack(inventory.selectedSlot);
        if (currentSlotItem.contains(DataComponentTypes.FOOD) && !IsFoodHarmful(currentSlotItem.getItem())) {
            return inventory.selectedSlot;
        }

        List<Integer> remainingInventorySlotIndexes = IntStream.rangeClosed(1, 9)
                .boxed()
                .filter(slot -> slot != inventory.selectedSlot)
                .toList();

        for (int inventorySlotIndex : remainingInventorySlotIndexes) {

            ItemStack itemStack = inventory.getStack(inventorySlotIndex);

            if (itemStack.contains(DataComponentTypes.FOOD) && !IsFoodHarmful(itemStack.getItem())) {
                return inventorySlotIndex;
            }
        }

        return -1;
    }

    private static final List<RegistryEntry<StatusEffect>> harmfulFoodEffects = List.of(
            StatusEffects.POISON,
            StatusEffects.WITHER,
            StatusEffects.HUNGER,
            StatusEffects.NAUSEA,
            StatusEffects.WEAKNESS,
            StatusEffects.BLINDNESS
    );

    private boolean IsFoodHarmful(Item item) {
        FoodComponent foodComponent = item.getDefaultStack().get(DataComponentTypes.FOOD);

        return foodComponent.effects().stream()
                .anyMatch(s -> harmfulFoodEffects.contains(s.effect()));
    }
}
