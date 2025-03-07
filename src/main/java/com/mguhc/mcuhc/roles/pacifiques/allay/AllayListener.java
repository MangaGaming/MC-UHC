package com.mguhc.mcuhc.roles.pacifiques.allay;

import com.mguhc.ability.Ability;
import com.mguhc.events.UhcDeathEvent;
import com.mguhc.mcuhc.MCUHC;
import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllayListener implements Listener {

    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;
    private final RoleManager roleManager;
    private final PlayerManager playerManager;

    private Ability soinAbility;
    private final List<Player> safeList = new ArrayList<>();
    private final boolean canUseSecondLife = true;

    public AllayListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Allay");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            player.getInventory().addItem(getSoinItem());

            soinAbility = new Ability("Soin", 10*60*1000);
            abilityManager.registerAbility(roleManager.getUhcRole("Allay"), Collections.singletonList(soinAbility));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.getHealth() <= 19.5) {
                        player.setHealth(player.getHealth() + 0.5);
                    }
                }
            }.runTaskTimer(MCUHC.getInstance(), 0, 3*20);
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getSoinItem()) && isAllay(player)) {
            if (cooldownManager.getRemainingCooldown(player, soinAbility) == 0) {
                cooldownManager.startCooldown(player, soinAbility);
                for (Entity e : player.getNearbyEntities(50, 50 , 50)) {
                    if (e instanceof Player) {
                        Player p = (Player) e;
                        ChatColor color = UhcAPI.getInstance().getColorMap().get(player).get(p);
                        if (color != null && color.equals(ChatColor.GREEN)) {
                            final int[] timer = {0};
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (timer[0] == 20) {
                                        this.cancel();
                                    }
                                    else {
                                        if (player.getHealth() <= 19.5) {
                                            p.setHealth(p.getHealth() + 0.5);
                                        }
                                    }
                                    timer[0]++;
                                }
                            }.runTaskTimer(MCUHC.getInstance(), 0, 20*6);
                        }
                    }
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "Vous devez attendre " + cooldownManager.getRemainingCooldown(player, soinAbility) + " avant de pouvoir utiliser cette ability");
            }
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if (isAllay(player) && args.length == 3 && args[0].equals("/mc") && args[1].equals("safe")) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target != null) {
                if (safeList.contains(target)) {
                    safeList.remove(target);
                    player.sendMessage("§a Vous avez retiré à votre liste de safe : " + target.getName());
                }
                else {
                    safeList.add(target);
                    player.sendMessage("§a Vous avez ajouté à votre liste de safe : " + target.getName());
                }
            }
        }
    }

    @EventHandler
    private void OnRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Player killer = player.getKiller();

        if (canUseSecondLife && killer != null) {
            // Créer le message avec un composant cliquable
            TextComponent message = new TextComponent("§cUn joueur est mort §9[");
            TextComponent clickHere = new TextComponent("§9cliquez ici pour le ressusciter");
            clickHere.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/resurrect " + player.getName())); // Commande à exécuter

            message.addExtra(clickHere);
            message.addExtra("§9]");

            // Envoyer le message spécifiquement à Allay
            UhcPlayer allay = roleManager.getPlayerWithRole("Allay");
            if (allay != null && allay.getPlayer() != null) {
                allay.getPlayer().spigot().sendMessage(message); // Utiliser spigot() pour envoyer le message
            }
        }
    }

    @EventHandler
    private void OnDeath(UhcDeathEvent event) {
        Player player = event.getPlayer();
        if (isAllay(player)) {
            event.getDrops().remove(getSoinItem());
        }
    }

    private boolean isAllay(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Allay");
    }

    public static ItemStack getSoinItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName("§c Soin");
            item.setItemMeta(itemMeta);
        }
        return item;
    }
}
