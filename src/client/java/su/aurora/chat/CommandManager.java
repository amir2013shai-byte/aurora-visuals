package su.aurora.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import su.aurora.Aurora;
import su.aurora.AuroraClient;
import su.aurora.core.Module;
import su.aurora.core.ProfileManager;
import su.aurora.modules.misc.FriendsModule;
import su.aurora.modules.render.HatsModule;
import su.aurora.modules.render.WaypointsModule;
import su.aurora.setting.ModeSetting;
import su.aurora.setting.StringSetting;
import su.aurora.ui.ClickGuiScreen;
import su.aurora.ui.HudOffsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Чат-клиент: команды с префиксом «.» обрабатываются локально и на сервер не уходят.
 * Перехват — ChatMixin#sendChat.
 */
public final class CommandManager {
    public static final String PREFIX = ".";

    private CommandManager() {
    }

    /** true — сообщение было командой клиента и на сервер его отправлять не нужно. */
    public static boolean handle(String message) {
        if (message == null || !message.startsWith(PREFIX)) {
            return false;
        }
        String raw = message.substring(PREFIX.length()).trim();
        if (raw.isEmpty()) {
            return false;
        }
        String[] parts = raw.split("\\s+");
        String command = parts[0].toLowerCase(Locale.ROOT);
        try {
            switch (command) {
                case "help", "?" -> help();
                case "toggle", "t" -> toggle(parts);
                case "bind", "b" -> bind(parts);
                case "unbind" -> unbind(parts);
                case "friend", "f" -> friend(parts);
                case "wp", "waypoint" -> waypoint(parts);
                case "cfg", "config", "profile" -> config(parts);
                case "theme" -> theme(parts);
                case "hud" -> hud(parts);
                case "cosmetic", "hat" -> cosmetic(parts);
                case "modules", "list" -> modules();
                case "copy" -> copy(parts);
                case "v", "version" -> print(Aurora.NAME + " v" + Aurora.VERSION + " · модулей: "
                        + (AuroraClient.MODULES == null ? 0 : AuroraClient.MODULES.all().size()));
                default -> {
                    error("неизвестная команда: " + command);
                    print("все команды: " + PREFIX + "help");
                }
            }
        } catch (RuntimeException exception) {
            error("ошибка выполнения: " + exception.getMessage());
        }
        return true;
    }

    // ---------- вывод ----------
    private static void print(String text) {
        Minecraft client = Minecraft.getInstance();
        client.gui.getChat().addMessage(Component.literal("§b[Aurora] §f" + text));
    }

    private static void error(String text) {
        Minecraft client = Minecraft.getInstance();
        client.gui.getChat().addMessage(Component.literal("§c[Aurora] §f" + text));
    }

    // ---------- команды ----------
    private static void help() {
        print("команды клиента (локально, на сервер не уходят):");
        print(PREFIX + "toggle <модуль> · " + PREFIX + "bind <модуль> <клавиша> · " + PREFIX + "unbind <модуль>");
        print(PREFIX + "friend add|remove|list [ник] · " + PREFIX + "wp add|remove|list [имя]");
        print(PREFIX + "cfg save|load|list [имя] · " + PREFIX + "theme <тема> · " + PREFIX + "modules");
        print(PREFIX + "hud reset · " + PREFIX + "cosmetic <модель> · " + PREFIX + "copy [строк] · "
                + PREFIX + "version");
    }

    private static Module module(String name) {
        if (AuroraClient.MODULES == null) {
            return null;
        }
        for (Module module : AuroraClient.MODULES.all()) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    private static void toggle(String[] parts) {
        if (parts.length < 2) {
            error("укажи модуль: " + PREFIX + "toggle TargetESP");
            return;
        }
        Module module = module(parts[1]);
        if (module == null) {
            error("модуль не найден: " + parts[1]);
            return;
        }
        module.toggle();
        AuroraClient.CONFIG.markDirty();
        print(module.getName() + " — " + (module.isEnabled() ? "§aвключён" : "§cвыключен"));
    }

    private static void bind(String[] parts) {
        if (parts.length < 3) {
            error("формат: " + PREFIX + "bind <модуль> <клавиша>, например " + PREFIX + "bind Zoom V");
            return;
        }
        Module module = module(parts[1]);
        if (module == null) {
            error("модуль не найден: " + parts[1]);
            return;
        }
        try {
            InputConstants.Key key = InputConstants.getKey("key.keyboard." + parts[2].toLowerCase(Locale.ROOT));
            if (key == InputConstants.UNKNOWN) {
                error("неизвестная клавиша: " + parts[2]);
                return;
            }
            module.setKey(key.getValue());
            AuroraClient.CONFIG.markDirty();
            print(module.getName() + " → " + module.getKeyName());
        } catch (RuntimeException exception) {
            error("не удалось распознать клавишу: " + parts[2]);
        }
    }

    private static void unbind(String[] parts) {
        if (parts.length < 2) {
            error("формат: " + PREFIX + "unbind <модуль>");
            return;
        }
        Module module = module(parts[1]);
        if (module == null) {
            error("модуль не найден: " + parts[1]);
            return;
        }
        module.setKey(-1);
        AuroraClient.CONFIG.markDirty();
        print(module.getName() + " отвязан");
    }

    private static void friend(String[] parts) {
        Module module = module("Friends");
        if (!(module instanceof FriendsModule friends)) {
            error("модуль Friends не найден");
            return;
        }
        StringSetting setting = (StringSetting) friends.getSetting("Друзья");
        if (setting == null) {
            error("настройка «Друзья» недоступна");
            return;
        }
        String action = parts.length > 1 ? parts[1].toLowerCase(Locale.ROOT) : "list";
        if (action.equals("list")) {
            print("друзья: " + (setting.get().isBlank() ? "пусто" : setting.get()));
            return;
        }
        if (parts.length < 3) {
            error("формат: " + PREFIX + "friend add|remove <ник>");
            return;
        }
        List<String> names = new ArrayList<>();
        for (String name : setting.get().split("[,;\\s]+")) {
            if (!name.isBlank()) {
                names.add(name);
            }
        }
        if (action.equals("add")) {
            if (names.stream().noneMatch(name -> name.equalsIgnoreCase(parts[2]))) {
                names.add(parts[2]);
            }
        } else if (action.equals("remove")) {
            names.removeIf(name -> name.equalsIgnoreCase(parts[2]));
        } else {
            error("неизвестное действие: " + action);
            return;
        }
        setting.set(String.join(",", names));
        friends.setEnabled(true);
        AuroraClient.CONFIG.markDirty();
        print("друзья: " + (names.isEmpty() ? "пусто" : String.join(", ", names)));
    }

    private static void waypoint(String[] parts) {
        Module module = module("Waypoints");
        if (!(module instanceof WaypointsModule waypoints)) {
            error("модуль Waypoints не найден");
            return;
        }
        StringSetting setting = (StringSetting) waypoints.getSetting("Точки");
        if (setting == null) {
            error("настройка «Точки» недоступна");
            return;
        }
        String action = parts.length > 1 ? parts[1].toLowerCase(Locale.ROOT) : "list";
        if (action.equals("list")) {
            print("точки: " + (setting.get().isBlank() ? "пусто" : setting.get()));
            return;
        }
        if (action.equals("add")) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                error("нет мира");
                return;
            }
            String name = parts.length > 2 ? parts[2] : "wp" + System.currentTimeMillis() % 1000;
            String entry = String.format(Locale.ROOT, "%s:%.1f:%.1f:%.1f", name, client.player.getX(),
                    client.player.getY(), client.player.getZ());
            String current = setting.get();
            setting.set(current == null || current.isBlank() ? entry : current + ";" + entry);
            waypoints.setEnabled(true);
            AuroraClient.CONFIG.markDirty();
            print("точка добавлена: " + name);
            return;
        }
        if (action.equals("remove")) {
            if (parts.length < 3) {
                error("формат: " + PREFIX + "wp remove <имя>");
                return;
            }
            List<String> keep = new ArrayList<>();
            for (String entry : setting.get().split(";")) {
                if (!entry.isBlank() && !entry.split(":")[0].equalsIgnoreCase(parts[2])) {
                    keep.add(entry);
                }
            }
            setting.set(String.join(";", keep));
            AuroraClient.CONFIG.markDirty();
            print("точка удалена: " + parts[2]);
            return;
        }
        error("формат: " + PREFIX + "wp add|remove|list");
    }

    private static void config(String[] parts) {
        String action = parts.length > 1 ? parts[1].toLowerCase(Locale.ROOT) : "list";
        switch (action) {
            case "save" -> {
                if (parts.length < 3) {
                    error("формат: " + PREFIX + "cfg save <имя>");
                    return;
                }
                print(ProfileManager.save(parts[2]) ? "профиль сохранён: " + parts[2]
                        : "не удалось сохранить профиль");
            }
            case "load" -> {
                if (parts.length < 3) {
                    error("формат: " + PREFIX + "cfg load <имя>");
                    return;
                }
                print(ProfileManager.load(parts[2]) ? "профиль загружен: " + parts[2]
                        : "профиль не найден: " + parts[2]);
            }
            case "list" -> print("профили: " + String.join(", ", ProfileManager.list()));
            default -> error("формат: " + PREFIX + "cfg save|load|list [имя]");
        }
    }

    private static void theme(String[] parts) {
        if (parts.length < 2) {
            StringBuilder names = new StringBuilder();
            for (ClickGuiScreen.Theme theme : ClickGuiScreen.Theme.values()) {
                names.append(names.isEmpty() ? "" : ", ").append(theme.name());
            }
            print("темы: " + names);
            return;
        }
        try {
            ClickGuiScreen.setTheme(ClickGuiScreen.Theme.valueOf(parts[1].toUpperCase(Locale.ROOT)));
            AuroraClient.CONFIG.markDirty();
            print("тема: " + parts[1].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            error("нет такой темы: " + parts[1]);
        }
    }

    private static void hud(String[] parts) {
        if (parts.length > 1 && parts[1].equalsIgnoreCase("reset")) {
            if (AuroraClient.MODULES != null) {
                for (Module module : AuroraClient.MODULES.all()) {
                    HudOffsets.reset(module.getName());
                }
            }
            HudOffsets.save();
            print("смещения HUD сброшены");
            return;
        }
        error("формат: " + PREFIX + "hud reset");
    }

    private static void cosmetic(String[] parts) {
        Module module = module("Hats");
        if (!(module instanceof HatsModule hats)) {
            error("модуль Hats не найден");
            return;
        }
        ModeSetting hat = hats.hatSetting();
        if (parts.length < 2) {
            print("модели: " + String.join(", ", hat.getModes()));
            return;
        }
        for (String mode : hat.getModes()) {
            if (mode.equalsIgnoreCase(parts[1])) {
                hat.set(mode);
                hats.setEnabled(true);
                AuroraClient.CONFIG.markDirty();
                print("надета модель: " + mode);
                return;
            }
        }
        error("нет такой модели: " + parts[1]);
    }

    private static void modules() {
        if (AuroraClient.MODULES == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        int enabled = 0;
        for (Module module : AuroraClient.MODULES.all()) {
            if (module.isEnabled()) {
                enabled++;
                builder.append(builder.isEmpty() ? "" : ", ").append(module.getName());
            }
        }
        print("включено " + enabled + " из " + AuroraClient.MODULES.all().size() + ": " + builder);
    }

    private static void copy(String[] parts) {
        int count = 1;
        if (parts.length > 1) {
            try {
                count = Math.max(1, Integer.parseInt(parts[1]));
            } catch (NumberFormatException ignored) {
                error("формат: " + PREFIX + "copy [число строк]");
                return;
            }
        }
        Minecraft client = Minecraft.getInstance();
        List<String> lines = new ArrayList<>();
        for (String line : client.gui.getChat().getRecentChat()) {
            lines.add(line);
        }
        if (lines.isEmpty()) {
            error("история чата пуста");
            return;
        }
        int from = Math.max(0, lines.size() - count);
        String joined = String.join("\n", lines.subList(from, lines.size()));
        client.keyboardHandler.setClipboard(joined);
        print("скопировано строк: " + (lines.size() - from));
    }

    /** Открыть чат с уже введённым префиксом (для удобства). */
    public static void openChatWithPrefix() {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(new ChatScreen(PREFIX, false));
    }
}
