package com.mguhc.mcuhc.roles.pacifiques;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.events.UhcDeathEvent;
import com.mguhc.mcuhc.MCUHC;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class FoxListener implements Listener {

    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;
    private final RoleManager roleManager;
    private final PlayerManager playerManager;

    private int gapCounter = 0;
    private int hitCounter = 0;

    public FoxListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Fox");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();

            new BukkitRunnable() {
                @Override
                public void run() {
                    long time = player.getWorld().getTime();
                    if (time >= 0 && time < 12000) { // Vérifier si c'est le jour
                        effectManager.removeEffect(player, PotionEffectType.SPEED);
                    } else { // Si c'est la nuit
                        effectManager.setSpeed(player, 1);
                    }

                    MCUHC.sendActionBar(player, "§6Gap : " + gapCounter);
                }
            }.runTaskTimer(MCUHC.getInstance(), 0, 20*3);
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if (isFox(player) && args.length == 3 && event.getMessage().contains("/mc gap")) {
            int v = Integer.parseInt(args[2]);
            if (v != 0) {
                gapCounter ++;
            }
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (isFox(victim)) {
                hitCounter++; // Incrémenter le compteur de coups reçus

                // Vérifier si le compteur atteint 5
                if (hitCounter >= 5) {
                    if (gapCounter > 0) {
                        gapCounter--; // Consommer une gap
                        victim.sendMessage("§aVous avez consommé une gap. Restant : " + gapCounter);
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2*60*20, 0, true, false));
                    } else {
                        victim.sendMessage("§cVous n'avez plus de gaps à consommer !");
                    }
                    hitCounter = 0; // Réinitialiser le compteur
                }
            }
        }
    }

    @EventHandler
    private void OnDeath(UhcDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = event.getKiller();
        if (killer != null && isFox(killer) && playerManager.getKill(player) == 0) {
            player.sendMessage("§aC'est votre premier kill. Vous récupérez donc l'item de son role s'il en avait un");
            switch (roleManager.getRole(playerManager.getPlayer(player)).getName()) {
                case "Allay":
                    player.getInventory().addItem(AllayListener.getSoinItem());
                case "Camel":
                    player.getInventory().addItem(CamelListener.getMontureItem());
                case "Parrot":
                    player.getInventory().addItem(ParrotListener.getFlyItem());
                case "Polar Bear":
                    player.getInventory().addItem(PolarBearListener.getGlassItem());
                default:
                    break;
            }
        }
    }

    private boolean isFox(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Fox");
    }
}
