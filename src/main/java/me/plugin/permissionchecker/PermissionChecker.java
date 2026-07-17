package me.plugin.permissionchecker;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class PermissionChecker extends JavaPlugin implements CommandExecutor, Listener {

    @Override
    public void onEnable() {
        // Регистрация команды и ивента
        if (getCommand("checkpermission") != null) {
            getCommand("checkpermission").setExecutor(this);
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("PermissionChecker успешно включен!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cИспользование: /checkpermission <команда>");
            return true;
        }

        // Очищаем от слеша, если игрок ввел его (например, /plugins -> plugins)
        String targetCmdName = args[0].replace("/", "").toLowerCase();

        // Ищем команду в глобальной карте сервера (работает для ванильных команд и плагинов)
        Command targetCommand = Bukkit.getServer().getCommandMap().getCommand(targetCmdName);

        if (targetCommand == null) {
            sender.sendMessage("§cКоманда /" + targetCmdName + " не найдена или не зарегистрирована.");
            return true;
        }

        String permission = targetCommand.getPermission();

        if (permission == null || permission.isEmpty()) {
            sender.sendMessage("§eДля команды §6/" + targetCmdName + " §eне задан конкретный permission (доступна всем или обрабатывается внутри плагина).");
        } else {
            sender.sendMessage("§aДля использования §6/" + targetCmdName + " §aтребуется право: §b" + permission);
        }

        return true;
    }

    // Перехват ввода команд для защиты /plugins
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase().trim();

        // Проверяем вариации команды /plugins
        if (message.startsWith("/plugins") || message.startsWith("/pl") || 
            message.startsWith("/bukkit:plugins") || message.startsWith("/bukkit:pl")) {
            
            // Если у игрока нет права на обход, отменяем ивент
            if (!player.hasPermission("permissionchecker.plugins.bypass")) {
                event.setCancelled(true);
                player.sendMessage("§cУ вас нет прав для просмотра списка плагинов!");
            }
        }
    }
}
