package com.atthnei.replenish_lite;

import net.minecraft.client.KeyMapping;

@FunctionalInterface
interface Action {
    boolean call();
}

public class ExtendedKeyBinding extends KeyMapping {
    private boolean wasKeyPressed = false;

    public ExtendedKeyBinding(String translationKey, int code, String category) {
        super(translationKey, code, category);
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
