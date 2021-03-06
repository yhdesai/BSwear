package io.github.bswearteam.bswear;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Mute implements Listener, CommandExecutor {
    private BSwear m;
    public Mute(BSwear b){m = b;}

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onChatMute(AsyncPlayerChatEvent event) {
        if (m.muted.getBoolean("muted." + event.getPlayer().getName().toLowerCase())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(m.prefix+"You are muted");
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender.hasPermission("bswear.command.mute") || sender.hasPermission("bswear.command.bmute") || sender.isOp()) {
            if (args.length == 2) {
                boolean add = args[0].equalsIgnoreCase("add");
                boolean remove = args[0].equalsIgnoreCase("remove");

                if (!(add || remove)) {
                    sender.sendMessage(ChatColor.RED + "Usage: /bmute <add/remove> <player>");
                    return true;
                }

                Player mutePlayer = Bukkit.getPlayer(args[1]);
                if (mutePlayer == null) {
                    sender.sendMessage(ChatColor.YELLOW + args[1] + ChatColor.RED  + " is not online");
                } else {
                    m.muted.set("muted." + mutePlayer.getName().toLowerCase(), (add ? true : null));

                    m.saveConf(m.muted, m.mutedf);
                    sender.sendMessage(ChatColor.RED + mutePlayer.getName() + ChatColor.RED + " is now " + (add ? "muted!" : "unmuted"));
                }
            } else sender.sendMessage(ChatColor.RED + "Usage: /bmute <add/remove> <player>");

            return true;
        } else sender.sendMessage("No permission");

        return false;
    }
}
