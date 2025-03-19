package com.mguhc.mcuhc.roles.pacifiques;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.events.UhcDeathEvent;
import com.mguhc.mcuhc.MCUHC;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CatListener implements Listener {

    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;
    private final RoleManager roleManager;
    private final PlayerManager playerManager;

    private Ability mcRole;
    private boolean hasFindPhantom = false;
    private boolean hasFindCreeper = false;
    private boolean hasKilledPhantom = false;
    private boolean hasKilledCreeper = false;

    public CatListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Cat");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            effectManager.setNoFall(player, true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
            new BukkitRunnable() {
                @Override
                public void run() {
                    long time = player.getWorld().getTime();
                    if (time >= 0 && time < 12000) { // Vérifier si c'est le jour
                        if (hasKilledCreeper) {
                            effectManager.setSpeed(player, 1);
                        }
                    } else { // Si c'est la nuit
                        if (hasKilledPhantom) {
                            effectManager.setSpeed(player, 1);
                        }
                    }
                }
            }.runTaskTimer(MCUHC.getInstance(), 0, 20*3);

            new BukkitRunnable() {
                @Override
                public void run() {
                    // Récupérer tous les joueurs dans une liste
                    List<Player> players = new ArrayList<>(playerManager.getPlayers().keySet());

                    for (Player p : players) {
                        Map<Player, ChatColor> colorMap = UhcAPI.getInstance().getColorMap().get(player);

                        if (colorMap != null && colorMap.containsKey(p) && colorMap.get(p).equals(ChatColor.RED)) {
                            Player closestPlayer = null;
                            double closestDistance = Double.MAX_VALUE; // Initialiser à une valeur très élevée
                            if (!p.equals(player)) { // Ne pas se comparer à soi-même
                                double distance = player.getLocation().distance(p.getLocation());
                                if (distance < closestDistance) {
                                    closestDistance = distance;
                                    closestPlayer = p;
                                }
                            }

                            // Si un joueur le plus proche a été trouvé, envoyer la distance
                            if (closestPlayer != null) {
                                player.sendMessage("§cLe joueur hostile le plus proche est à une distance de " + Math.round(closestDistance) + " blocs.");
                            }
                        }
                    }
                }
            }.runTaskTimer(MCUHC.getInstance(), 0, 60 * 20); // Exécuter toutes les 60 secondes

            mcRole = new Ability("/mc <role>", 10*60*1000);
            abilityManager.registerAbility(uhcPlayer.getRole(), Collections.singletonList(mcRole));
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if (isCat(player) && args.length == 3 && event.getMessage().contains("/mc creeper")) {
            if (cooldownManager.getRemainingCooldown(player, mcRole) == 0) {
                Player target = Bukkit.getPlayer(args[2]);
                if (target != null) {
                    cooldownManager.startCooldown(player, mcRole);
                    UhcPlayer uhcPlayer = playerManager.getPlayer(target);
                    if (uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Creeper")) {
                        hasFindCreeper = true;
                        player.sendMessage("§aVous avez trouvé le creeper vous n'avez plus qu'a le tuer pour avoir vos effets");
                    }
                    else {
                        player.sendMessage("§cLe joueur cité n'est pas creeper");
                    }
                }
            }
            else {
                player.sendMessage("§cVous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, mcRole) / 1000);
            }
        }
        if (isCat(player) && args.length == 3 && event.getMessage().contains("/mc phantom")) {
            if (cooldownManager.getRemainingCooldown(player, mcRole) == 0) {
                Player target = Bukkit.getPlayer(args[2]);
                if (target != null) {
                    cooldownManager.startCooldown(player, mcRole);
                    UhcPlayer uhcPlayer = playerManager.getPlayer(target);
                    if (uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Phantom")) {
                        hasFindPhantom = true;
                        player.sendMessage("§aVous avez trouvé le phantom vous n'avez plus qu'a le tuer pour avoir vos effets");
                    }
                    else {
                        player.sendMessage("§cLe joueur cité n'est pas phantom");
                    }
                }
            }
            else {
                player.sendMessage("§cVous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, mcRole) / 1000);
            }
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            double damage = event.getDamage();
            if (hasFindCreeper) {
                UhcPlayer uhcPlayer = playerManager.getPlayer(damager);
                if (uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Creeper")) {
                    damage = damage - (double) 20 /100 * damage;
                }
            }
            if (hasFindPhantom) {
                UhcPlayer uhcPlayer = playerManager.getPlayer(damager);
                if (uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Phantom")) {
                    damage = damage - (double) 20 /100 * damage;
                }
            }
            event.setDamage(damage);
        }
    }

    @EventHandler
    private void OnDeath(UhcDeathEvent event) {
        Player player = event.getKiller();
        Player victim = event.getPlayer();
        if (!isCat(player)) {
            return;
        }
        UhcPlayer uhcPlayer = playerManager.getPlayer(victim);
        if (uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Creeper")) {
            hasKilledCreeper = true;
            player.sendMessage("§aVous avez tué le Creeper vous récupérez Speed 1 de jour");
        }
        else if (uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Phantom")) {
            hasKilledPhantom = true;
            player.sendMessage("§aVous avez tué le Phantom vous récupérez Speed 1 de nuit");
        }
    }

    private boolean isCat(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Cat");
    }
}
