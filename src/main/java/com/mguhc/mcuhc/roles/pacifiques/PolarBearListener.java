package com.mguhc.mcuhc.roles.pacifiques;

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
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolarBearListener implements Listener {
    private final EffectManager effectManager = UhcAPI.getInstance().getEffectManager();
    private final PlayerManager playerManager = UhcAPI.getInstance().getPlayerManager();
    private final RoleManager roleManager = UhcAPI.getInstance().getRoleManager();
    private final CooldownManager cooldownManager = UhcAPI.getInstance().getCooldownManager();
    private final AbilityManager abilityManager = UhcAPI.getInstance().getAbilityManager();
    private final Map<Player, Location> playerGlaced = new HashMap<>();

    private Ability glassAbility;

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Polar Bear");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            player.setMaxHealth(26);
            player.getInventory().addItem(getGlassItem());

            glassAbility = new Ability("Zone de Glace", 3*60*1000);
            abilityManager.registerAbility(uhcPlayer.getRole(), Collections.singletonList(glassAbility));
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (cooldownManager.getRemainingCooldown(player, glassAbility) == 0) {
            if (item != null && item.isSimilar(getGlassItem())) {
                Player target = getTargetPlayer(player, 10);
                if (target != null) {
                    cooldownManager.startCooldown(player, glassAbility);
                    createGlassZone(target);
                    playerGlaced.put(target, target.getLocation());
                    player.sendMessage("§aZone de Glace créer");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            playerGlaced.remove(target);
                        }
                    }.runTaskLater(MCUHC.getInstance(), 3*60*20);

                }
            }
        }
        else {
            player.sendMessage("§cVous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, glassAbility) / 1000 + "s");
        }
    }

    private void createGlassZone(Player player) {
        // Créer une zone de glace autour du joueur
        int radius = 15; // Rayon de 30 blocs
        Location playerLocation = player.getLocation();
        World nmsWorld = ((CraftPlayer) player).getHandle().world;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Calculer la distance au centre
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= radius) {
                    // Obtenir la position du bloc
                    int blockX = playerLocation.getBlockX() + x;
                    int blockZ = playerLocation.getBlockZ() + z;
                    int blockY = player.getWorld().getHighestBlockYAt(blockX, blockZ);

                    // Créer un paquet pour changer le type de bloc
                    BlockPosition blockPosition = new BlockPosition(blockX, blockY, blockZ);
                    PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(nmsWorld, blockPosition);
                    packet.block = Block.getById(Material.PACKED_ICE.getId()).getBlockData(); // Utilisez l'ID du bloc de glace

                    // Envoyer le paquet au joueur
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
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

    @EventHandler
    private void OnMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (playerGlaced.containsKey(player) && isInCircle(player, playerGlaced.get(player))) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
        }
    }

    private boolean isInCircle(Player player, Location location) {
        // Récupérer la position du joueur
        Location playerLocation = player.getLocation();

        // Calculer la distance horizontale entre le joueur et la location
        double distance = Math.sqrt(Math.pow(playerLocation.getX() - location.getX(), 2) +
                Math.pow(playerLocation.getZ() - location.getZ(), 2));

        // Vérifier si la distance est inférieure ou égale au rayon (30)
        return distance <= 30;
    }

    @EventHandler
    private void OnBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        // Vérifiez si l'item est le bloc de glace que vous souhaitez interdire
        if (item != null && item.isSimilar(getGlassItem())) {
            event.setCancelled(true); // Annuler le placement du bloc
        }
    }

    public static ItemStack getGlassItem() {
        ItemStack item = new ItemStack(Material.ICE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("§9Bloc de Glace");
        item.setItemMeta(itemMeta);
        return item;
    }
}
