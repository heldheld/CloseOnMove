package com.heldheld.closeonmove.config;

import com.heldheld.closeonmove.CloseOnMove;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration screen for CloseOnMove mod
 * Compatible with Minecraft 1.21.9 and Mod Menu API
 */
public class ConfigMenu extends Screen {
    private final Screen parent;
    private final Config config;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    // Temporary storage for keybinds during editing
    private final int[] tempKeys = new int[10];
    private int waitingForKeySlot = -1;
    private boolean waitingForRelease = false; // Wait for initial click release

    // Track key states to prevent multiple triggers
    private final boolean[] keyPressed = new boolean[350];
    private final boolean[] mousePressed = new boolean[8];

    // Keybind buttons
    private final ButtonWidget[] keybindButtons = new ButtonWidget[10];

    public ConfigMenu(Screen parent) {
        super(Text.literal("CloseOnMove Settings"));
        this.parent = parent;
        this.config = Config.getInstance();

        initializeTempKeys();
    }

    private void initializeTempKeys() {
        // Initialize all slots as unbound
        for (int i = 0; i < 10; i++) {
            tempKeys[i] = -1;
        }

        // Load current keys from config
        List<Integer> currentKeys = config.getCloseKeys();
        for (int i = 0; i < Math.min(currentKeys.size(), 10); i++) {
            tempKeys[i] = currentKeys.get(i);
        }

        // Set defaults if no keys are configured
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
        this.layout.addHeader(this.title, this.textRenderer);

        GridWidget gridWidget = this.layout.addBody(new GridWidget());
        gridWidget.getMainPositioner().marginX(8).marginY(4);
        GridWidget.Adder adder = gridWidget.createAdder(2);

        // Add keybind buttons in a 2-column layout
        for (int i = 0; i < 10; i++) {
            final int slot = i;
            keybindButtons[i] = ButtonWidget.builder(
                    getKeyButtonText(slot),
                    button -> {
                        // Only start binding if not already waiting for input
                        if (waitingForKeySlot == -1) {
                            startKeyBinding(slot);
                        }
                    }
            ).width(145).build();

            adder.add(keybindButtons[i]);
        }

        // Add bottom buttons
        GridWidget bottomButtons = this.layout.addFooter(new GridWidget());
        bottomButtons.getMainPositioner().marginX(8);
        GridWidget.Adder bottomAdder = bottomButtons.createAdder(3);

        bottomAdder.add(ButtonWidget.builder(
                ScreenTexts.CANCEL,
                button -> onCancelClick()
        ).width(80).build());

        bottomAdder.add(ButtonWidget.builder(
                Text.literal("Save & Quit"),
                button -> onSaveClick()
        ).width(80).build());

        bottomAdder.add(ButtonWidget.builder(
                Text.literal("Reset"),
                button -> onResetClick()
        ).width(80).build());

        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, 0, 0, this.width, this.height, 0.5f, 0.25f);
        this.layout.forEachChild(this::addDrawableChild);
    }

    private Text getKeyButtonText(int slot) {
        String keyName;
        if (tempKeys[slot] == -1) {
            keyName = "Not Bound";
        } else {
            keyName = config.getKeyName(tempKeys[slot]);
        }

        if (waitingForKeySlot == slot) {
            return Text.literal("")
                    .append(Text.literal("> ").styled(style -> style.withColor(Formatting.YELLOW)))
                    .append(Text.literal(keyName).styled(style -> style.withUnderline(true).withColor(Formatting.WHITE)))
                    .append(Text.literal(" <").styled(style -> style.withColor(Formatting.YELLOW)));
        } else {
            return Text.literal(keyName);
        }
    }

    private void startKeyBinding(int slot) {
        // Don't start binding if already waiting - this prevents button re-triggering
        if (waitingForKeySlot != -1) {
            return;
        }
        waitingForKeySlot = slot;
        waitingForRelease = true; // Wait for the button click to be released
        // Reset left mouse button state to prevent immediate trigger
        mousePressed[GLFW.GLFW_MOUSE_BUTTON_LEFT] = true;
        refreshKeybindButtons();
    }

    private void refreshKeybindButtons() {
        for (int i = 0; i < 10; i++) {
            if (keybindButtons[i] != null) {
                keybindButtons[i].setMessage(getKeyButtonText(i));
            }
        }
    }

    private void handleKeyInput() {
        if (waitingForKeySlot == -1 || this.client == null || this.client.getWindow() == null) {
            return;
        }

        long handle = this.client.getWindow().getHandle();

        // If waiting for release, check if left mouse button is released
        if (waitingForRelease) {
            if (GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
                waitingForRelease = false;
            }
        }

        // Check keyboard keys (GLFW key range is 32-348, but we'll use safe common range)
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            boolean isPressed = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;

            if (isPressed && !keyPressed[key]) {
                keyPressed[key] = true;

                // Escape clears the binding slot
                if (key == GLFW.GLFW_KEY_ESCAPE) {
                    tempKeys[waitingForKeySlot] = -1;
                    waitingForKeySlot = -1;
                    waitingForRelease = false;
                    refreshKeybindButtons();
                    return;
                }

                // Assign the key
                tempKeys[waitingForKeySlot] = key;
                waitingForKeySlot = -1;
                waitingForRelease = false; // Reset the flag even though we didn't use mouse
                refreshKeybindButtons();
                return;
            } else if (!isPressed) {
                keyPressed[key] = false;
            }
        }

        // Check mouse buttons
        for (int button = 0; button < 8; button++) {
            boolean isPressed = GLFW.glfwGetMouseButton(handle, button) == GLFW.GLFW_PRESS;

            if (isPressed && !mousePressed[button]) {
                mousePressed[button] = true;

                // Assign the mouse button (don't check if clicking on button anymore)
                tempKeys[waitingForKeySlot] = button;
                waitingForKeySlot = -1;
                refreshKeybindButtons();
                return;
            } else if (!isPressed) {
                mousePressed[button] = false;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render screen background first
        super.render(context, mouseX, mouseY, delta);

        // Handle key input for binding
        handleKeyInput();

        // Draw subtitle text directly below title
        Text subtitleText = Text.literal("Keys (or mouse buttons) that exit the GUI's:");
        int subtitleX = this.width / 2;
        int subtitleY = 25; // Just below the title at y=20
        context.drawCenteredTextWithShadow(this.textRenderer, subtitleText, subtitleX, subtitleY, 0xFFAAAAAA);
    }

    private void onCancelClick() {
        waitingForKeySlot = -1;
        close();
    }

    private void onSaveClick() {
        // Convert temp keys to list, excluding unbound keys
        List<Integer> keysToSave = new ArrayList<>();
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
        refreshKeybindButtons();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // Don't close on Escape if waiting for key input
        return waitingForKeySlot == -1;
    }

    @Override
    public void close() {
        waitingForKeySlot = -1;
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}