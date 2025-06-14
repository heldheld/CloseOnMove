package com.heldheld.closeonmove.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Config {
    private static final String CONFIG_FILE_NAME = "closeonmove.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Config INSTANCE;

    // Default keys: W, A, S, D, SPACE
    private List<Integer> CloseKeys = Arrays.asList(
            GLFW.GLFW_KEY_W,
            GLFW.GLFW_KEY_A,
            GLFW.GLFW_KEY_S,
            GLFW.GLFW_KEY_D,
            GLFW.GLFW_KEY_SPACE
    );

    // Map for key code to name conversion
    private static final Map<Integer, String> KEY_NAMES = new HashMap<>();
    private static final Map<String, Integer> NAME_TO_KEY = new HashMap<>();

    static {
        // Initialize key mappings - Letters
        addKeyMapping(GLFW.GLFW_KEY_A, "A");
        addKeyMapping(GLFW.GLFW_KEY_B, "B");
        addKeyMapping(GLFW.GLFW_KEY_C, "C");
        addKeyMapping(GLFW.GLFW_KEY_D, "D");
        addKeyMapping(GLFW.GLFW_KEY_E, "E");
        addKeyMapping(GLFW.GLFW_KEY_F, "F");
        addKeyMapping(GLFW.GLFW_KEY_G, "G");
        addKeyMapping(GLFW.GLFW_KEY_H, "H");
        addKeyMapping(GLFW.GLFW_KEY_I, "I");
        addKeyMapping(GLFW.GLFW_KEY_J, "J");
        addKeyMapping(GLFW.GLFW_KEY_K, "K");
        addKeyMapping(GLFW.GLFW_KEY_L, "L");
        addKeyMapping(GLFW.GLFW_KEY_M, "M");
        addKeyMapping(GLFW.GLFW_KEY_N, "N");
        addKeyMapping(GLFW.GLFW_KEY_O, "O");
        addKeyMapping(GLFW.GLFW_KEY_P, "P");
        addKeyMapping(GLFW.GLFW_KEY_Q, "Q");
        addKeyMapping(GLFW.GLFW_KEY_R, "R");
        addKeyMapping(GLFW.GLFW_KEY_S, "S");
        addKeyMapping(GLFW.GLFW_KEY_T, "T");
        addKeyMapping(GLFW.GLFW_KEY_U, "U");
        addKeyMapping(GLFW.GLFW_KEY_V, "V");
        addKeyMapping(GLFW.GLFW_KEY_W, "W");
        addKeyMapping(GLFW.GLFW_KEY_X, "X");
        addKeyMapping(GLFW.GLFW_KEY_Y, "Y");
        addKeyMapping(GLFW.GLFW_KEY_Z, "Z");

        // Numbers
        addKeyMapping(GLFW.GLFW_KEY_0, "0");
        addKeyMapping(GLFW.GLFW_KEY_1, "1");
        addKeyMapping(GLFW.GLFW_KEY_2, "2");
        addKeyMapping(GLFW.GLFW_KEY_3, "3");
        addKeyMapping(GLFW.GLFW_KEY_4, "4");
        addKeyMapping(GLFW.GLFW_KEY_5, "5");
        addKeyMapping(GLFW.GLFW_KEY_6, "6");
        addKeyMapping(GLFW.GLFW_KEY_7, "7");
        addKeyMapping(GLFW.GLFW_KEY_8, "8");
        addKeyMapping(GLFW.GLFW_KEY_9, "9");

        // Function Keys
        addKeyMapping(GLFW.GLFW_KEY_F1, "F1");
        addKeyMapping(GLFW.GLFW_KEY_F2, "F2");
        addKeyMapping(GLFW.GLFW_KEY_F3, "F3");
        addKeyMapping(GLFW.GLFW_KEY_F4, "F4");
        addKeyMapping(GLFW.GLFW_KEY_F5, "F5");
        addKeyMapping(GLFW.GLFW_KEY_F6, "F6");
        addKeyMapping(GLFW.GLFW_KEY_F7, "F7");
        addKeyMapping(GLFW.GLFW_KEY_F8, "F8");
        addKeyMapping(GLFW.GLFW_KEY_F9, "F9");
        addKeyMapping(GLFW.GLFW_KEY_F10, "F10");
        addKeyMapping(GLFW.GLFW_KEY_F11, "F11");
        addKeyMapping(GLFW.GLFW_KEY_F12, "F12");
        addKeyMapping(GLFW.GLFW_KEY_F13, "F13");
        addKeyMapping(GLFW.GLFW_KEY_F14, "F14");
        addKeyMapping(GLFW.GLFW_KEY_F15, "F15");
        addKeyMapping(GLFW.GLFW_KEY_F16, "F16");
        addKeyMapping(GLFW.GLFW_KEY_F17, "F17");
        addKeyMapping(GLFW.GLFW_KEY_F18, "F18");
        addKeyMapping(GLFW.GLFW_KEY_F19, "F19");
        addKeyMapping(GLFW.GLFW_KEY_F20, "F20");
        addKeyMapping(GLFW.GLFW_KEY_F21, "F21");
        addKeyMapping(GLFW.GLFW_KEY_F22, "F22");
        addKeyMapping(GLFW.GLFW_KEY_F23, "F23");
        addKeyMapping(GLFW.GLFW_KEY_F24, "F24");
        addKeyMapping(GLFW.GLFW_KEY_F25, "F25");

        // Arrow Keys
        addKeyMapping(GLFW.GLFW_KEY_UP, "Up");
        addKeyMapping(GLFW.GLFW_KEY_DOWN, "Down");
        addKeyMapping(GLFW.GLFW_KEY_LEFT, "Left");
        addKeyMapping(GLFW.GLFW_KEY_RIGHT, "Right");

        // Special Keys
        addKeyMapping(GLFW.GLFW_KEY_SPACE, "Space");
        addKeyMapping(GLFW.GLFW_KEY_ENTER, "Enter");
        addKeyMapping(GLFW.GLFW_KEY_TAB, "Tab");
        addKeyMapping(GLFW.GLFW_KEY_BACKSPACE, "Backspace");
        addKeyMapping(GLFW.GLFW_KEY_DELETE, "Delete");
        addKeyMapping(GLFW.GLFW_KEY_INSERT, "Insert");
        addKeyMapping(GLFW.GLFW_KEY_HOME, "Home");
        addKeyMapping(GLFW.GLFW_KEY_END, "End");
        addKeyMapping(GLFW.GLFW_KEY_PAGE_UP, "Page Up");
        addKeyMapping(GLFW.GLFW_KEY_PAGE_DOWN, "Page Down");

        // Modifier Keys
        addKeyMapping(GLFW.GLFW_KEY_LEFT_SHIFT, "Left Shift");
        addKeyMapping(GLFW.GLFW_KEY_RIGHT_SHIFT, "Right Shift");
        addKeyMapping(GLFW.GLFW_KEY_LEFT_CONTROL, "Left Control");
        addKeyMapping(GLFW.GLFW_KEY_RIGHT_CONTROL, "Right Control");
        addKeyMapping(GLFW.GLFW_KEY_LEFT_ALT, "Left Alt");
        addKeyMapping(GLFW.GLFW_KEY_RIGHT_ALT, "Right Alt");
        addKeyMapping(GLFW.GLFW_KEY_LEFT_SUPER, "Left Win");
        addKeyMapping(GLFW.GLFW_KEY_RIGHT_SUPER, "Right Win");
        addKeyMapping(GLFW.GLFW_KEY_MENU, "Menu");

        // Lock Keys
        addKeyMapping(GLFW.GLFW_KEY_CAPS_LOCK, "Caps Lock");
        addKeyMapping(GLFW.GLFW_KEY_SCROLL_LOCK, "Scroll Lock");
        addKeyMapping(GLFW.GLFW_KEY_NUM_LOCK, "Num Lock");

        // Keypad Numbers
        addKeyMapping(GLFW.GLFW_KEY_KP_0, "Keypad 0");
        addKeyMapping(GLFW.GLFW_KEY_KP_1, "Keypad 1");
        addKeyMapping(GLFW.GLFW_KEY_KP_2, "Keypad 2");
        addKeyMapping(GLFW.GLFW_KEY_KP_3, "Keypad 3");
        addKeyMapping(GLFW.GLFW_KEY_KP_4, "Keypad 4");
        addKeyMapping(GLFW.GLFW_KEY_KP_5, "Keypad 5");
        addKeyMapping(GLFW.GLFW_KEY_KP_6, "Keypad 6");
        addKeyMapping(GLFW.GLFW_KEY_KP_7, "Keypad 7");
        addKeyMapping(GLFW.GLFW_KEY_KP_8, "Keypad 8");
        addKeyMapping(GLFW.GLFW_KEY_KP_9, "Keypad 9");

        // Keypad Operations
        addKeyMapping(GLFW.GLFW_KEY_KP_DECIMAL, "Keypad .");
        addKeyMapping(GLFW.GLFW_KEY_KP_DIVIDE, "Keypad /");
        addKeyMapping(GLFW.GLFW_KEY_KP_MULTIPLY, "Keypad *");
        addKeyMapping(GLFW.GLFW_KEY_KP_SUBTRACT, "Keypad -");
        addKeyMapping(GLFW.GLFW_KEY_KP_ADD, "Keypad +");
        addKeyMapping(GLFW.GLFW_KEY_KP_ENTER, "Keypad Enter");
        addKeyMapping(GLFW.GLFW_KEY_KP_EQUAL, "Keypad =");

        // Punctuation and Symbols
        addKeyMapping(GLFW.GLFW_KEY_SEMICOLON, ";");
        addKeyMapping(GLFW.GLFW_KEY_EQUAL, "=");
        addKeyMapping(GLFW.GLFW_KEY_COMMA, ",");
        addKeyMapping(GLFW.GLFW_KEY_MINUS, "-");
        addKeyMapping(GLFW.GLFW_KEY_PERIOD, ".");
        addKeyMapping(GLFW.GLFW_KEY_SLASH, "/");
        addKeyMapping(GLFW.GLFW_KEY_GRAVE_ACCENT, "`");
        addKeyMapping(GLFW.GLFW_KEY_LEFT_BRACKET, "[");
        addKeyMapping(GLFW.GLFW_KEY_BACKSLASH, "\\");
        addKeyMapping(GLFW.GLFW_KEY_RIGHT_BRACKET, "]");
        addKeyMapping(GLFW.GLFW_KEY_APOSTROPHE, "'");

        // Print Screen and Pause
        addKeyMapping(GLFW.GLFW_KEY_PRINT_SCREEN, "Print Screen");
        addKeyMapping(GLFW.GLFW_KEY_PAUSE, "Pause");

        // World Keys (for international keyboards)
        addKeyMapping(GLFW.GLFW_KEY_WORLD_1, "World 1");
        addKeyMapping(GLFW.GLFW_KEY_WORLD_2, "World 2");

        // Mouse Buttons
        addKeyMapping(GLFW.GLFW_MOUSE_BUTTON_LEFT, "Left Button");
        addKeyMapping(GLFW.GLFW_MOUSE_BUTTON_RIGHT, "Right Button");
        addKeyMapping(GLFW.GLFW_MOUSE_BUTTON_MIDDLE, "Middle Button");
        addKeyMapping(GLFW.GLFW_MOUSE_BUTTON_4, "Back Button");
        addKeyMapping(GLFW.GLFW_MOUSE_BUTTON_5, "Forward Button");
        addKeyMapping(GLFW.GLFW_MOUSE_BUTTON_6, "Button 6");
        addKeyMapping(GLFW.GLFW_MOUSE_BUTTON_7, "Button 7");
        addKeyMapping(GLFW.GLFW_MOUSE_BUTTON_8, "Button 8");
    }

    private static void addKeyMapping(int keyCode, String name) {
        KEY_NAMES.put(keyCode, name);
        NAME_TO_KEY.put(name, keyCode);
        // Also add uppercase version for case-insensitive lookup
        NAME_TO_KEY.put(name.toUpperCase(), keyCode);
    }

    public static Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public List<Integer> getCloseKeys() {
        return new ArrayList<>(CloseKeys);
    }

    public void setCloseKeys(List<Integer> keys) {
        this.CloseKeys = new ArrayList<>(keys);
        save();
    }

    public void addCloseKey(int keyCode) {
        if (!CloseKeys.contains(keyCode)) {
            CloseKeys.add(keyCode);
            save();
        }
    }

    public void removeCloseKey(int keyCode) {
        CloseKeys.remove(Integer.valueOf(keyCode));
        save();
    }

    public boolean isCloseKey(int keyCode) {
        return CloseKeys.contains(keyCode);
    }

    public String getKeyName(int keyCode) {
        return KEY_NAMES.getOrDefault(keyCode, "KEY_" + keyCode);
    }

    public Integer getKeyCode(String name) {
        return NAME_TO_KEY.get(name);
    }

    public Set<String> getAllKeyNames() {
        return new HashSet<>(KEY_NAMES.values());
    }

    public static Config load() {
        File configFile = getConfigFile();

        if (!configFile.exists()) {
            Config config = new Config();
            config.save();
            return config;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            ConfigData data = GSON.fromJson(reader, ConfigData.class);

            Config config = new Config();
            if (data != null && data.CloseKeys != null) {
                List<Integer> keys = new ArrayList<>();
                for (String keyName : data.CloseKeys) {
                    Integer keyCode = config.getKeyCode(keyName);
                    if (keyCode != null) {
                        keys.add(keyCode);
                    }
                }
                if (!keys.isEmpty()) {
                    config.CloseKeys = keys;
                }
            }
            return config;
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
            return new Config();
        }
    }

    public void save() {
        File configFile = getConfigFile();

        try {
            configFile.getParentFile().mkdirs();

            ConfigData data = new ConfigData();
            data.CloseKeys = new ArrayList<>();
            for (int keyCode : CloseKeys) {
                data.CloseKeys.add(getKeyName(keyCode));
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    private static File getConfigFile() {
        return new File(FabricLoader.getInstance().getConfigDir().toFile(), CONFIG_FILE_NAME);
    }

    private static class ConfigData {
        public List<String> CloseKeys;
    }
}