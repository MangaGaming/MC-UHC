package com.mguhc.mcuhc.roles.pacifiques.parrot;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.mcuhc.MCUHC;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class ParrotListener implements Listener {
    private final EffectManager effectManager = UhcAPI.getInstance().getEffectManager();
    private final PlayerManager playerManager = UhcAPI.getInstance().getPlayerManager();
    private final RoleManager roleManager = UhcAPI.getInstance().getRoleManager();
    private final CooldownManager cooldownManager = UhcAPI.getInstance().getCooldownManager();
    private final AbilityManager abilityManager = UhcAPI.getInstance().getAbilityManager();

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Parrot");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            player.getInventory().addItem(getFlyItem());

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Entity e : player.getNearbyEntities(20, 20, 20)) {
                        if (e instanceof Player) {
                            Player p = (Player) e;
                            UhcPlayer uhcPlayer = playerManager.getPlayer(p);
                            if (uhcPlayer != null && uhcPlayer.getRole() != null) {
                                String roleName = uhcPlayer.getRole().getName();
                                // Vérifiez si le rôle est dans la liste spécifiée
                                if (Arrays.asList("Steve", "Villager", "Creeper", "Zombie", "Guardian",
                                    "Phantom", "Shulker", "Spider", "Witch",
                                    "Elder Guardian", "Ender Dragon").contains(roleName)) {
                                    player.sendMessage("§cVous avez un joueur à imiter dans un rayon de 20 blocks autour de vous");
                                    break; // Annule la tâche
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(MCUHC.getInstance(), 0, 5 * 60 * 20);
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.equals(getFlyItem())) {
            // Vérifiez si le joueur peut voler
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true); // Autoriser le vol
            }

            // Alterner l'état de vol
            player.setFlying(!player.isFlying());
        }
    }

    public static ItemStack getFlyItem() {
        ItemStack flyItem = new ItemStack(Material.FEATHER);
        ItemMeta meta = flyItem.getItemMeta();
        meta.setDisplayName("§7Vol");
        flyItem.setItemMeta(meta);
        return flyItem;
    }

    private boolean isParrot(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Parrot");
    }
}
