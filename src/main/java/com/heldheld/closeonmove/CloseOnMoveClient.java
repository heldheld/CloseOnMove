package com.heldheld.closeonmove;

import com.heldheld.closeonmove.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloseOnMoveClient implements ClientModInitializer {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private Config config;

    // Dynamic key state tracking
    private Map<Integer, Boolean> keyStates = new HashMap<>();

    @Override
    public void onInitializeClient() {
        this.config = Config.getInstance();
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private boolean isClosableScreen(Screen screen) {
        // Debug logging to see what screen classes we're dealing with
        String screenClassName = screen.getClass().getSimpleName();
        String packageName = screen.getClass().getPackage().getName();
        //CloseOnMove.LOGGER.info("Current screen: " + screenClassName + " in package: " + packageName);

        // Use HandledScreen like the working mod - this covers inventory, chests, crafting tables, etc.
        if (screen instanceof HandledScreen) {
            //CloseOnMove.LOGGER.info("Detected HandledScreen: " + screenClassName);
            return true;
        }

        // Check for advancement screens using instanceof
        if (screen instanceof AdvancementsScreen) {
            //CloseOnMove.LOGGER.info("Detected advancement screen: " + screenClassName);
            return true;
        }

        // Check for book screens using instanceof
        if (screen instanceof BookScreen) {
            //CloseOnMove.LOGGER.info("Detected book screen: " + screenClassName);
            return true;
        }

        // Check package name as fallback for ingame screens
        if (packageName.contains("ingame")) {
            //CloseOnMove.LOGGER.info("Detected ingame screen: " + screenClassName);
            return true;
        }

        //CloseOnMove.LOGGER.info("Screen not closable: " + screenClassName);
        return false;
    }

    private void onClientTick(MinecraftClient client) {
        Screen currentScreen = client.currentScreen;

        if (currentScreen == null) {
            // Reset all key states when no screen is open
            keyStates.clear();
            return;
        }

        // Don't close screens when not in a world
        if (client.world == null) {
            keyStates.clear();
            return;
        }

        // Only close HandledScreen and advancement screens
        if (!isClosableScreen(currentScreen)) {
            keyStates.clear();
            return;
        }

        // Check if any configured movement key or mouse button was pressed
        List<Integer> closeKeys = config.getCloseKeys();
        boolean anyKeyPressed = false;

        for (int keyCode : closeKeys) {
            if (isInputPressedOnce(keyCode)) {
                anyKeyPressed = true;
                break;
            }
        }

        if (anyKeyPressed && !isTextFieldFocused(currentScreen)) {
            currentScreen.close();
        }
    }

    private boolean isInputPressedOnce(int keyCode) {
        boolean isCurrentlyPressed;

        // Check if this is a mouse button (GLFW mouse button codes are 0-7)
        if (isMouseButton(keyCode)) {
            isCurrentlyPressed = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
        } else {
            // It's a keyboard key
            isCurrentlyPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), keyCode);
        }

        boolean wasInputPreviouslyPressed = keyStates.getOrDefault(keyCode, false);

        if (isCurrentlyPressed && !wasInputPreviouslyPressed) {
            keyStates.put(keyCode, true);
            return true;
        } else if (!isCurrentlyPressed) {
            keyStates.put(keyCode, false);
        }
        return false;
    }

    /**
     * Check if the given code represents a mouse button
     * GLFW mouse button constants are typically 0-7
     */
    private boolean isMouseButton(int keyCode) {
        return keyCode >= GLFW.GLFW_MOUSE_BUTTON_1 && keyCode <= GLFW.GLFW_MOUSE_BUTTON_LAST;
    }

    private boolean isTextFieldFocused(Screen screen) {
        // Check for any focused text fields in the screen (including nested ones)
        if (hasAnyFocusedTextField(screen)) {
            return true;
        }

        // Special handling for screens that always have text input active
        String screenClassName = screen.getClass().getSimpleName().toLowerCase();
        if (screenClassName.contains("edit")) {
            return true;
        }

        return false;
    }

    private boolean hasAnyFocusedTextField(Screen screen) {
        return checkChildrenForFocusedTextField(screen.children());
    }

    private boolean checkChildrenForFocusedTextField(Iterable<?> children) {
        for (var child : children) {
            if (child instanceof TextFieldWidget textField && textField.isFocused()) {
                return true;
            }

            // Recursively check if this child has its own children
            try {
                Field childrenField = child.getClass().getDeclaredField("children");
                childrenField.setAccessible(true);
                Object childrenObj = childrenField.get(child);
                if (childrenObj instanceof Iterable<?> nestedChildren) {
                    if (checkChildrenForFocusedTextField(nestedChildren)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // Field doesn't exist or isn't accessible, continue
            }

            // Check for any field that might contain TextFieldWidget
            if (hasTextFieldInFields(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTextFieldInFields(Object obj) {
        Class<?> clazz = obj.getClass();

        // Check current class and all superclasses
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);

                    if (value instanceof TextFieldWidget textField && textField.isFocused()) {
                        return true;
                    }

                    // If it's a collection, check each element
                    if (value instanceof Iterable<?> iterable) {
                        for (Object item : iterable) {
                            if (item instanceof TextFieldWidget textField && textField.isFocused()) {
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Field not accessible or other error, continue
                }
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }
}