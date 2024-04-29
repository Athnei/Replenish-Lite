package com.atthnei.replenish_lite;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Type;

@FunctionalInterface
interface Action {
    boolean call();
}

public class ExtendedKeyBinding extends KeyBinding {
    private boolean wasKeyPressed = false;

    public ExtendedKeyBinding(String translationKey, int code, String category) {
        super(translationKey, code, category);
    }

    public ExtendedKeyBinding(String translationKey, Type type, int code, String category) {
        super(translationKey, type, code, category);
    }

    public void onKeyDown(Action action) {
        if (this.isPressed() && !wasKeyPressed) {
            wasKeyPressed = action.call();
        }
    }

    public void onKeyUp(Action action) {
        if (!this.isPressed() && wasKeyPressed) {
            wasKeyPressed = action.call();
        }
    }
}
