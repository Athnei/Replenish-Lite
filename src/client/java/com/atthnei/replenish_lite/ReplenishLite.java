package com.atthnei.replenish_lite;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

    private void onEndTick(Minecraft client) {
        KeyBindingPressed(client, replenishKeyBinding);
    }


    private void KeyBindingPressed(Minecraft client, ExtendedKeyBinding keyBinding) {
        assert client.player != null;

        keyBinding.onKeyDown(() -> {
            Inventory inventory = client.player.getInventory();

            slotIndexBeforePress = inventory.selected;
            int hotbarIndex = GetHotbarIndexWithFood(inventory);

            if (hotbarIndex != -1) {
                inventory.selected = hotbarIndex;
                client.options.keyUse.setDown(true);
                return true;
            }
            return false;
        });

        keyBinding.onKeyUp(() -> {
            client.options.keyUse.setDown(false);
            client.player.getInventory().selected = slotIndexBeforePress;
            return false;
        });
    }

    private int GetHotbarIndexWithFood(Inventory inventory) {

        ItemStack currentSlotItem = inventory.getItem(inventory.selected);
        if (currentSlotItem.has(DataComponents.FOOD) && !IsFoodHarmful(currentSlotItem.getItem())) {
            return inventory.selected;
        }

        List<Integer> remainingInventorySlotIndexes = IntStream.rangeClosed(1, 9)
                .boxed()
                .filter(slot -> slot != inventory.selected)
                .toList();

        for (int inventorySlotIndex : remainingInventorySlotIndexes) {

            var itemStack = inventory.getItem(inventorySlotIndex);

            if (itemStack.has(DataComponents.FOOD) && !IsFoodHarmful(itemStack.getItem())) {
                return inventorySlotIndex;
            }
        }

        return -1;
    }

    private static final List<Holder<MobEffect>> harmfulFoodEffects = List.of(
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.HUNGER,
            MobEffects.CONFUSION,
            MobEffects.WEAKNESS,
            MobEffects.BLINDNESS
    );

    private boolean IsFoodHarmful(Item item) {
        FoodProperties foodComponent = item.getDefaultInstance().get(DataComponents.FOOD);

        return foodComponent.effects().stream()
                .anyMatch(s -> harmfulFoodEffects.contains(s.effect()));
    }
}
