package com.heldheld.closeonmove.config;

import com.heldheld.closeonmove.CloseOnMove;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ConfigMenu extends Screen {
    private final Screen parent;
    private final Config config;
    private final int[] tempKeys = new int[10]; // 10 slots for keys
    private int waitingForKeySlot = -1; // -1 = not waiting, 0-9 = slot index

    // Entry list widget for keybind buttons
    private KeybindListWidget keybindList;

    // Bottom buttons
    private ButtonWidget cancelButton;
    private ButtonWidget saveQuitButton;
    private ButtonWidget resetButton;

    protected ConfigMenu(Screen parent) {
        super(Text.literal("CloseOnMove Settings"));
        this.parent = parent;
        this.config = Config.getInstance();

        // Initialize temp keys from config
        initializeTempKeys();
    }

    private void initializeTempKeys() {
        // Fill with -1 (unbound) first
        for (int i = 0; i < 10; i++) {
            tempKeys[i] = -1;
        }

        // Get current keys from config and assign to first available slots
        var currentKeys = config.getCloseKeys();
        for (int i = 0; i < Math.min(currentKeys.size(), 10); i++) {
            tempKeys[i] = currentKeys.get(i);
        }

        // If no keys configured, set defaults
        if (currentKeys.isEmpty()) {
            tempKeys[0] = GLFW.GLFW_KEY_W;
            tempKeys[1] = GLFW.GLFW_KEY_A;
            tempKeys[2] = GLFW.GLFW_KEY_S;
            tempKeys[3] = GLFW.GLFW_KEY_D;
            tempKeys[4] = GLFW.GLFW_KEY_SPACE;
        }
    }

    @Override
    protected void init() {
        // Create the keybind list widget
        keybindList = new KeybindListWidget(this.client, this.width, this.height - 80, 40, 25);

        addDrawableChild(keybindList);

        // Bottom buttons - properly centered
        int bottomButtonWidth = 80;
        int bottomButtonSpacing = 15;
        int totalBottomWidth = (bottomButtonWidth * 3) + (bottomButtonSpacing * 2);
        int bottomStartX = (width - totalBottomWidth) / 2;
        int bottomY = height - 30;

        // Cancel button
        cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button -> {
                    onCancelClick();
                })
                .dimensions(bottomStartX, bottomY, bottomButtonWidth, 20)
                .build();
        addDrawableChild(cancelButton);

        // Save & Quit button
        saveQuitButton = ButtonWidget.builder(Text.literal("Save & Quit"), button -> {
                    onSaveQuitClick();
                })
                .dimensions(bottomStartX + bottomButtonWidth + bottomButtonSpacing, bottomY, bottomButtonWidth, 20)
                .build();
        addDrawableChild(saveQuitButton);

        // Reset button
        resetButton = ButtonWidget.builder(Text.literal("Reset"), button -> {
                    onResetClick();
                })
                .dimensions(bottomStartX + (bottomButtonWidth + bottomButtonSpacing) * 2, bottomY, bottomButtonWidth, 20)
                .build();
        addDrawableChild(resetButton);
    }

    private Text getKeyButtonText(int slot) {
        String keyName;
        if (tempKeys[slot] == -1) {
            keyName = "Not Bound";
        } else {
            keyName = config.getKeyName(tempKeys[slot]);
        }

        if (waitingForKeySlot == slot) {
            // Create text with yellow brackets and underlined white key name
            return Text.literal("")
                    .append(Text.literal(">").styled(style -> style.withColor(Formatting.YELLOW)))
                    .append(Text.literal(" "))
                    .append(Text.literal(keyName).styled(style -> style.withUnderline(true).withColor(Formatting.WHITE)))
                    .append(Text.literal(" "))
                    .append(Text.literal("<").styled(style -> style.withColor(Formatting.YELLOW)));
        } else {
            return Text.literal(keyName);
        }
    }

    private void startKeyBinding(int slot) {
        waitingForKeySlot = slot;
        refreshKeybindList();
    }

    private void refreshKeybindList() {
        // Refresh all entries in the list
        for (KeybindListWidget.ListEntry entry : keybindList.children()) {
            if (entry instanceof KeybindListWidget.KeybindEntry) {
                ((KeybindListWidget.KeybindEntry) entry).refreshButton();
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForKeySlot != -1) {
            // Escape cancels key binding and sets to "Not Bound"
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                tempKeys[waitingForKeySlot] = -1; // Set to "Not Bound"
                waitingForKeySlot = -1;
                refreshKeybindList();
                return true;
            }

            // Assign the key
            tempKeys[waitingForKeySlot] = keyCode;
            waitingForKeySlot = -1;
            refreshKeybindList();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // If we're waiting for a key binding and the click is on the keybind list area
        if (waitingForKeySlot != -1 && keybindList.isMouseOver(mouseX, mouseY)) {
            // Check if the click is on one of the keybind buttons
            boolean clickedOnKeybindButton = false;
            for (KeybindListWidget.ListEntry entry : keybindList.children()) {
                if (entry instanceof KeybindListWidget.KeybindEntry) {
                    KeybindListWidget.KeybindEntry keybindEntry = (KeybindListWidget.KeybindEntry) entry;
                    if (keybindEntry.isMouseOverButton(mouseX, mouseY)) {
                        clickedOnKeybindButton = true;
                        break;
                    }
                }
            }

            // If clicked on a keybind button while waiting, assign the mouse button
            if (clickedOnKeybindButton) {
                tempKeys[waitingForKeySlot] = button;
                waitingForKeySlot = -1;
                refreshKeybindList();
                return true;
            }
        }

        // Cancel key binding if clicking elsewhere
        if (waitingForKeySlot != -1 && !keybindList.isMouseOver(mouseX, mouseY)) {
            waitingForKeySlot = -1;
            refreshKeybindList();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Let super.render handle background rendering and all widgets automatically
        super.render(context, mouseX, mouseY, delta);

        // Draw title on top (after super.render to ensure it's drawn above everything)
        // Fixed: Added alpha channel (0xFF) to make text visible in 1.21.6
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("CloseOnMove Settings").styled(style -> style.withColor(Formatting.WHITE)),
                width / 2, 20, 0xFFFFFFFF); // Changed from Formatting.WHITE.getColorValue() to 0xFFFFFFFF
    }

    private void onCancelClick() {
        waitingForKeySlot = -1; // Cancel any pending key binding
        close();
    }

    private void onSaveQuitClick() {
        // Convert temp keys to list, excluding unbound (-1) keys
        java.util.List<Integer> keysToSave = new java.util.ArrayList<>();
        for (int key : tempKeys) {
            if (key != -1) {
                keysToSave.add(key);
            }
        }

        config.setCloseKeys(keysToSave);
        close();
    }

    private void onResetClick() {
        // Clear all slots
        for (int i = 0; i < 10; i++) {
            tempKeys[i] = -1;
        }

        // Set defaults
        tempKeys[0] = GLFW.GLFW_KEY_W;
        tempKeys[1] = GLFW.GLFW_KEY_A;
        tempKeys[2] = GLFW.GLFW_KEY_S;
        tempKeys[3] = GLFW.GLFW_KEY_D;
        tempKeys[4] = GLFW.GLFW_KEY_SPACE;

        waitingForKeySlot = -1;
        refreshKeybindList();
    }

    @Override
    public void close() {
        waitingForKeySlot = -1; // Cancel any pending key binding
        this.client.setScreen(this.parent);
    }

    // Custom ElementListWidget for keybind entries
    private class KeybindListWidget extends ElementListWidget<KeybindListWidget.ListEntry> {
        public KeybindListWidget(net.minecraft.client.MinecraftClient client, int width, int height, int top, int itemHeight) {
            super(client, width, height, top, itemHeight);

            // Add header entry first
            this.addEntry(new HeaderEntry());

            // Add entries for 5 rows (each row contains 2 buttons)
            for (int i = 0; i < 5; i++) {
                this.addEntry(new KeybindEntry(i * 2, i * 2 + 1)); // Pass two slot indices
            }
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // Call the parent render method to draw the entries
            super.renderWidget(context, mouseX, mouseY, delta);
        }

        @Override
        protected int getScrollbarX() {
            // Position scrollbar on the right side
            return this.getX() + this.getWidth() - 6;
        }

        // Base class for list entries
        public abstract class ListEntry extends ElementListWidget.Entry<ListEntry> {
        }

        // Header entry for the label text
        public class HeaderEntry extends ListEntry {
            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                // Fixed: Added alpha channel (0xFF) to make text visible in 1.21.6
                context.drawCenteredTextWithShadow(ConfigMenu.this.textRenderer,
                        Text.literal("Keys (or mouse buttons) that exit the GUI's:").styled(style -> style.withColor(Formatting.GRAY)),
                        x + entryWidth / 2, y + (entryHeight - 9) / 2, 0xFFAAAAAA); // Changed from Formatting.GRAY.getColorValue() to 0xFFAAAAAA
            }

            @Override
            public List<? extends Element> children() {
                return List.of();
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return List.of();
            }
        }

        public class KeybindEntry extends ListEntry {
            private final int leftSlot;
            private final int rightSlot;
            private final ButtonWidget leftButton;
            private final ButtonWidget rightButton;

            public KeybindEntry(int leftSlot, int rightSlot) {
                this.leftSlot = leftSlot;
                this.rightSlot = rightSlot;

                this.leftButton = ButtonWidget.builder(getKeyButtonText(leftSlot), button -> {
                            startKeyBinding(leftSlot);
                        })
                        .dimensions(0, 0, 145, 20)
                        .build();

                this.rightButton = ButtonWidget.builder(getKeyButtonText(rightSlot), button -> {
                            startKeyBinding(rightSlot);
                        })
                        .dimensions(0, 0, 145, 20)
                        .build();
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                // Center the buttons properly within the entry
                int buttonWidth = 145;
                int buttonSpacing = 10;
                int totalButtonWidth = (buttonWidth * 2) + buttonSpacing;
                int startX = x + (entryWidth - totalButtonWidth) / 2;
                int buttonY = y + (entryHeight - 20) / 2;

                // Left button
                leftButton.setX(startX);
                leftButton.setY(buttonY);
                leftButton.render(context, mouseX, mouseY, tickDelta);

                // Right button
                rightButton.setX(startX + buttonWidth + buttonSpacing);
                rightButton.setY(buttonY);
                rightButton.render(context, mouseX, mouseY, tickDelta);
            }

            @Override
            public List<? extends Element> children() {
                return List.of(leftButton, rightButton);
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return List.of(leftButton, rightButton);
            }

            public void refreshButton() {
                leftButton.setMessage(getKeyButtonText(leftSlot));
                rightButton.setMessage(getKeyButtonText(rightSlot));
            }

            public boolean isMouseOverButton(double mouseX, double mouseY) {
                return leftButton.isMouseOver(mouseX, mouseY) || rightButton.isMouseOver(mouseX, mouseY);
            }
        }
    }

    // ModMenu integration
    public static class ModMenuIntegration implements ModMenuApi {
        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return ConfigMenu::new;
        }
    }
}