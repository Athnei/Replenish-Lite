package com.atthnei.replenish_lite;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

@FunctionalInterface
interface Action {
    boolean call();
}

public class ExtendedKeyMapping extends KeyMapping {
    private boolean wasKeyPressed = false;

    public ExtendedKeyMapping(String translationKey, InputConstants.Type type, int code, Category category) {
        super(translationKey, type, code, category);
    }

    public void onKeyDown(Action action) {
        if (this.isDown() && !wasKeyPressed) {
            wasKeyPressed = action.call();
        }
    }

    public void onKeyUp(Action action) {
        if (!this.isDown() && wasKeyPressed) {
            wasKeyPressed = action.call();
        }
    }
}
