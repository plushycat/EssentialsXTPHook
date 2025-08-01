package com.plushycat.essentialsxtphook;

import com.earth2me.essentials.Essentials;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class essentialstphook extends JavaPlugin implements Listener {

    private Essentials essentials;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            getLogger().severe("EssentialsX not found! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("EssentialsXTP has been enabled and hooked into EssentialsX.");
    }

    @Override
    public void onDisable() {
        getLogger().info("EssentialsXTP has been disabled.");
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        FileConfiguration config = getConfig();

        if (command.startsWith("/tpa ") || command.startsWith("/tpahere ")) {
            String[] args = command.split(" ");
            if (args.length < 2) return;

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null || !target.isOnline()) return;

            Bukkit.getScheduler().runTaskLater(this, () -> {
                TextComponent msg = new TextComponent(ChatColor.AQUA + player.getName() + " has requested to teleport to you.");
                target.spigot().sendMessage(msg);

                playSound(target, config.getString("sounds.request"));

                TextComponent line = new TextComponent("\n");
                TextComponent accept = new TextComponent(ChatColor.GREEN + "[✔]");
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + player.getName()));
                accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Accept teleport").create()));

                TextComponent deny = new TextComponent(ChatColor.RED + "[✘]");
                deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + player.getName()));
                deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Deny teleport").create()));

                line.addExtra(accept);
                line.addExtra(" ");
                line.addExtra(deny);

                target.spigot().sendMessage(line);

                TextComponent cancelMsg = new TextComponent(ChatColor.YELLOW + "You sent a teleport request to " + target.getName() + ". ");
                TextComponent cancel = new TextComponent(ChatColor.RED + "[✘]");
                cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpacancel"));
                cancel.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Cancel request").create()));
                cancelMsg.addExtra(cancel);

                player.spigot().sendMessage(cancelMsg);
            }, 1L);
        }

        // Accept: play sound for both receiver and sender
        else if (command.startsWith("/tpaccept ")) {
            String[] args = command.split(" ");
            if (args.length < 2) {
                playSound(player, config.getString("sounds.accept"));
                return;
            }
            Player sender = Bukkit.getPlayerExact(args[1]);
            if (sender != null && sender.isOnline()) {
                playSound(sender, config.getString("sounds.accept"));
            }
            playSound(player, config.getString("sounds.accept"));
        }

        // Deny: play sound for both receiver and sender
        else if (command.startsWith("/tpdeny ")) {
            String[] args = command.split(" ");
            if (args.length < 2) {
                playSound(player, config.getString("sounds.deny"));
                return;
            }
            Player sender = Bukkit.getPlayerExact(args[1]);
            if (sender != null && sender.isOnline()) {
                playSound(sender, config.getString("sounds.deny"));
            }
            playSound(player, config.getString("sounds.deny"));
        }

        // Cancel: play sound for sender only (revert to previous implementation)
        else if (command.startsWith("/tpcancel")) {
            playSound(player, config.getString("sounds.cancel"));
        }
    }

    private void playSound(Player player, String soundName) {
        if (soundName == null || soundName.isEmpty()) return;
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid sound name: " + soundName);
        }
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("essxtpreload")) {
            reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "[EssentialsXTP] Config reloaded!");
            return true;
        }
        return false;
    }
}