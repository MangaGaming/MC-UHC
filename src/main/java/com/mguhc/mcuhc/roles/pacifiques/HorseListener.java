package com.mguhc.mcuhc.roles.pacifiques;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class HorseListener implements Listener {

    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;
    private final RoleManager roleManager;
    private final PlayerManager playerManager;

    private boolean hasCraftedArmor;

    public HorseListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Horse");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            effectManager.setSpeed(player, 1);
            createHorseArmorRecipe(); // Créer la recette de l'armure de cheval
        }
    }

    private void createHorseArmorRecipe() {
        // Créer l'armure de cheval
        ItemStack horseArmor = getHorseArmor();
        ItemMeta meta = horseArmor.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§9Armure de Horse");
            horseArmor.setItemMeta(meta);
        }

        // Créer la recette
        ShapedRecipe recipe = new ShapedRecipe(horseArmor);
        recipe.shape("odo", "fcf", "odo");

        // Définir les ingrédients
        recipe.setIngredient('o', Material.GOLD_INGOT); // Or
        recipe.setIngredient('d', Material.DIAMOND); // Diamant
        recipe.setIngredient('f', Material.IRON_INGOT); // Fer
        recipe.setIngredient('c', Material.LEATHER); // Cuivre

        // Enregistrer la recette
        JavaPlugin plugin = UhcAPI.getInstance(); // Assurez-vous que UhcAPI est une instance de JavaPlugin
        plugin.getServer().addRecipe(recipe);
    }

    @EventHandler
    private void OnCraft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item.equals(getHorseArmor())) {
            if (isHorse(player) && !hasCraftedArmor) {
                hasCraftedArmor = true;
                Random random = new Random();
                int percentage = random.nextInt(100);
                if (percentage < 50) {
                    effectManager.setResistance(player, 0.5);
                    effectManager.setSpeed(player, 0.5);
                }
                else {
                    effectManager.setResistance(player, 1);
                    effectManager.removeEffect(player, PotionEffectType.SPEED);
                }
            }
            else {
                event.setCancelled(true);
                player.sendMessage("§cSeul le Horse peut crafter cette armure");
            }
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {

    }

    private boolean isHorse(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Horse");
    }

    private ItemStack getHorseArmor() {
        ItemStack horseArmor = new ItemStack(Material.DIAMOND_BARDING);
        ItemMeta meta = horseArmor.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§9Armure de Horse");
            horseArmor.setItemMeta(meta);
        }
        return horseArmor;
    }
}