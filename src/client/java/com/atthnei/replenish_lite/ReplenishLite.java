package com.atthnei.replenish_lite;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.IntStream;

public class ReplenishLite implements ClientModInitializer {

    public static final String MOD_ID = "replenish-lite";

    KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "category")
    );

    private final ExtendedKeyMapping replenishKeyBinding = (ExtendedKeyMapping) KeyMappingHelper.registerKeyMapping(new ExtendedKeyMapping(
            "key." + MOD_ID + ".replenish-key",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY));

    private int slotIndexBeforePress = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(Minecraft client) {
        KeyBindingPressed(client, replenishKeyBinding);
    }


    private void KeyBindingPressed(@NonNull Minecraft client, @NonNull ExtendedKeyMapping keyBinding) {
        assert client.player != null;

        keyBinding.onKeyDown(() -> {
            Inventory inventory = client.player.getInventory();

            slotIndexBeforePress = inventory.getSelectedSlot();
            int hotbarIndex = GetHotbarIndexWithFood(inventory);

            if (hotbarIndex != -1) {
                inventory.setSelectedSlot(hotbarIndex);
                client.options.keyUse.setDown(true);
                return true;
            }
            return false;
        });

        keyBinding.onKeyUp(() -> {
            client.options.keyUse.setDown(false);
            client.player.getInventory().setSelectedSlot(slotIndexBeforePress);
            return false;
        });
    }

    private int GetHotbarIndexWithFood(@NonNull Inventory inventory) {

        ItemStack currentSlotItem = inventory.getItem(inventory.getSelectedSlot());
        if (currentSlotItem.has(DataComponents.FOOD) && !IsFoodHarmful(currentSlotItem.getItem())) {
            return inventory.getSelectedSlot();
        }

        List<Integer> remainingInventorySlotIndexes = IntStream.rangeClosed(1, 9)
                .boxed()
                .filter(slot -> slot != inventory.getSelectedSlot())
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
            MobEffects.NAUSEA,
            MobEffects.WEAKNESS,
            MobEffects.BLINDNESS
    );

    private boolean IsFoodHarmful(@NonNull Item item) {
        Consumable foodComponent = item.getDefaultInstance().get(DataComponents.CONSUMABLE);

        if (foodComponent != null) {
            return foodComponent.onConsumeEffects()
                    .stream()
                    .filter(ApplyStatusEffectsConsumeEffect.class::isInstance)
                    .map(ApplyStatusEffectsConsumeEffect.class::cast)
                    .flatMap(statusEffect -> statusEffect.effects().stream())
                    .anyMatch(s -> harmfulFoodEffects.contains(s.getEffect()));
        }

        return false;
    }
}
