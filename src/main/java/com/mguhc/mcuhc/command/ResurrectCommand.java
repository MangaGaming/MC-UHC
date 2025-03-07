package com.mguhc.mcuhc.command;

import com.mguhc.UhcAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResurrectCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String message, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage("§cVeuillez spécifier un joueur.");
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]); // Utiliser args[0] pour le nom du joueur
        if (target != null) {
            target.setHealth(target.getMaxHealth());
            UhcAPI.getInstance().getUhcGame().teleportToRandomLocation(target);
            commandSender.sendMessage("§aVous avez ressuscité " + target.getName() + " !");
            return true;
        } else {
            commandSender.sendMessage("§cLe joueur n'est pas en mode spectateur ou n'existe pas.");
            return false;
        }
    }
}