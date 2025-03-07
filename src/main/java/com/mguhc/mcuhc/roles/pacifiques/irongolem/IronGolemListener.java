package com.mguhc.mcuhc.roles.pacifiques.irongolem;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.mcuhc.MCUHC;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class IronGolemListener implements Listener {
    private final EffectManager effectManager = UhcAPI.getInstance().getEffectManager();
    private final PlayerManager playerManager = UhcAPI.getInstance().getPlayerManager();
    private final RoleManager roleManager = UhcAPI.getInstance().getRoleManager();
    private final CooldownManager cooldownManager = UhcAPI.getInstance().getCooldownManager();
    private final AbilityManager abilityManager = UhcAPI.getInstance().getAbilityManager();

    private Ability coupDeMainAbility;
    private Ability ironBlockAbility;
    private int slotUnPickable = -1;

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Iron Golem");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            UhcPlayer villager = roleManager.getPlayerWithRole("Villager");
            if (villager != null) {
                player.sendMessage("§aVillager : " + villager.getPlayer().getName());
            }
            effectManager.setResistance(player, 1);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventory inventory = player.getInventory();
                    if (slotUnPickable > -1 && slotUnPickable < 9) {
                        ItemStack slotItem = inventory.getItem(slotUnPickable);
                        if (slotItem != null && slotItem.getType() != Material.AIR) {
                            player.getInventory().remove(slotItem);
                            addItemToInventory(player, slotItem);
                        }
                    }
                }
            }.runTaskTimer(MCUHC.getInstance(), 0, 5);

            coupDeMainAbility = new Ability("Coup de Main", 60*1000);
            ironBlockAbility = new Ability("Bloc de Fer", 30*1000);
            abilityManager.registerAbility(uhcPlayer.getRole(), Arrays.asList(coupDeMainAbility, ironBlockAbility));
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        // Vérifiez si le joueur est un Golem et si la commande est correcte
        if (isGolem(player) && args.length == 3 && args[0].equalsIgnoreCase("/mc") && args[1].equalsIgnoreCase("slot")) {
            try {
                int slot = Integer.parseInt(args[2]);
                // Vérifiez si le slot est entre 1 et 9 inclus
                if (slot >= 1 && slot <= 9) {
                    player.sendMessage("§aVous avez défini votre slot qui ne peut pas ramasser des objets au slot " + slot);
                    slotUnPickable = slot - 1; // Mettre à jour le slotUnPickable (0-8)
                } else {
                    player.sendMessage("§cSlot illégal. Veuillez entrer un slot entre 1 et 9.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cVeuillez entrer un nombre valide pour le slot.");
            }
        }
    }

    public void addItemToInventory(Player player, ItemStack item) {
        // Chercher un slot disponible en dehors de la hotbar
        for (int i = 9; i < player.getInventory().getSize(); i++) {
            if (player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR) {
                player.getInventory().setItem(i, item); // Ajouter l'item dans le slot trouvé
                return; // Sortir de la méthode après avoir ajouté l'item
            }
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        Action action = event.getAction();
        if (isGolem(player) && cooldownManager.getRemainingCooldown(player, coupDeMainAbility) == 0) {
            if (item == null && action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                Player target = getTargetPlayer(player, 5);
                if (target != null) {
                    cooldownManager.startCooldown(player, coupDeMainAbility);
                    target.damage(2, player);
                    Vector velocity = target.getVelocity().add(new Vector(0, 8, 0));
                    target.setVelocity(velocity);
                }
            }
        }

        if (isGolem(player)) {
            if (cooldownManager.getRemainingCooldown(player, ironBlockAbility) == 0) {
                if (block != null && block.getType().equals(Material.IRON_BLOCK) && action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                    double health = player.getHealth();
                    if (health + 4 <= 20) {
                        cooldownManager.startCooldown(player, ironBlockAbility);
                        player.setHealth(health + 4);
                        player.sendMessage("§aVous avez gagner 2c");
                    }
                    else {
                        player.sendMessage("§cVous avez trop de vie");
                    }
                }
            }
            else {
                player.sendMessage("§cVous êtes en cooldown pour "+ (long) cooldownManager.getRemainingCooldown(player, ironBlockAbility) / 1000 + "s");
            }
        }
    }

    private Player getTargetPlayer(Player player, double maxDistance) {
        // Get the player's eye location and direction
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        // Get nearby entities within the specified distance
        List<Entity> nearbyEntities = player.getNearbyEntities(maxDistance, maxDistance, maxDistance);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player && entity != player) {
                // Check if the entity is in the line of sight
                Vector toEntity = entity.getLocation().toVector().subtract(eyeLocation.toVector()).normalize();
                double dotProduct = direction.dot(toEntity);

                // Check if the entity is within the player's line of sight
                if (dotProduct > 0.9) {
                    return (Player) entity;
                }
            }
        }
        return null; // No target player found
    }

    private boolean isGolem(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Iron Golem");
    }
}
