package com.mguhc.mcuhc.roles.pacifiques.camel;

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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;

public class CamelListener implements Listener {

    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;
    private final RoleManager roleManager;
    private final PlayerManager playerManager;

    private Ability montureAbility;

    public CamelListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Camel");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            player.getInventory().addItem(getMontureItem());

            new BukkitRunnable() {
                @Override
                public void run() {
                    long time = player.getWorld().getTime(); // Obtenir le temps actuel dans le monde
                    if (time >= 0 && time < 12000) { // Vérifier si c'est le jour
                        effectManager.removeEffect(player, PotionEffectType.DAMAGE_RESISTANCE);
                    } else {
                        effectManager.setResistance(player, 1);
                    }

                    player.setFoodLevel(19);
                }
            }.runTaskTimer(MCUHC.getInstance(), 0, 20*3);

            montureAbility = new Ability("Monture", 5*60*1000);
            abilityManager.registerAbility(uhcPlayer.getRole(), Collections.singletonList(montureAbility));
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getMontureItem())) {
            if (cooldownManager.getRemainingCooldown(player, montureAbility) == 0) {
                Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
                horse.setMaxHealth(40);
                horse.setHealth(horse.getMaxHealth());
                horse.setOwner(player);
                horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            }
            else {
                player.sendMessage(ChatColor.RED + "Vous devez attendre " + cooldownManager.getRemainingCooldown(player, montureAbility) + " avant de pouvoir utiliser cette ability");
            }
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (damager instanceof Player && isCamel((Player) damager) && damager.getVehicle() != null) {
            event.setCancelled(true); // Annuler les dégâts si Camel est sur son cheval
        }

        if (entity instanceof Player && isCamel((Player) entity) && entity.getVehicle() != null) {
            event.setCancelled(true); // Annuler les dégâts si Camel est sur son cheval
        }
    }

    @EventHandler
    private void OnDeath(UhcDeathEvent event) {
        Player player = event.getPlayer();
        if (isCamel(player)) {
            event.getDrops().remove(getMontureItem());
        }
    }

    private boolean isCamel(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Camel");
    }

    public static ItemStack getMontureItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName("§3Monture");
            item.setItemMeta(itemMeta);
        }
        return item;
    }
}