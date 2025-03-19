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
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;

public class FrogListener implements Listener {

    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;
    private final RoleManager roleManager;
    private final PlayerManager playerManager;

    private boolean hasKilledMagma = false;
    private boolean isPassifActive = false;
    private boolean isJumping;

    private Ability jumpAbility;

    public FrogListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Frog");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();

            ItemStack bow = new ItemStack(Material.BOW);
            ItemMeta bowMeta = bow.getItemMeta();
            bowMeta.setDisplayName("Arc de Frog");
            bowMeta.addEnchant(Enchantment.ARROW_DAMAGE, 3, true);
            bow.setItemMeta(bowMeta);
            player.getInventory().addItem(bow);

            effectManager.setNoFall(player, true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    long time = player.getWorld().getTime(); // Obtenir le temps actuel dans le monde
                    if (time >= 0 && time < 12000) { // Vérifier si c'est le jour
                        if (hasKilledMagma) {
                            effectManager.setStrength(player, 1);
                        }
                    } else {
                        effectManager.removeEffect(player, PotionEffectType.INCREASE_DAMAGE);
                    }

                    if (isPassifActive) {
                        player.setAllowFlight(true);
                        if (cooldownManager.getRemainingCooldown(player, jumpAbility) == 0) {
                            UhcAPI.sendActionBar(player, "§6Double Saut : §aDisponible");
                        }
                        else {
                            UhcAPI.sendActionBar(player, "§6Double Saut : §9" + (long) cooldownManager.getRemainingCooldown(player, jumpAbility) / 1000 + "s");
                        }
                    }
                    else {
                        UhcAPI.sendActionBar(player, "§6Double Saut : §cDésactivé");
                    }
                }
            }.runTaskTimer(MCUHC.getInstance(), 0, 20);

            jumpAbility = new Ability("Double Saut", 30*1000);
            abilityManager.registerAbility(uhcPlayer.getRole(), Collections.singletonList(jumpAbility));
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if (isFrog(player) && args.length == 2 && event.getMessage().contains("/mc jump")) {
            isPassifActive = !isPassifActive;
            if (isPassifActive) {
                player.sendMessage("§aVous avez activé votre double saut");
            }
            else {
                player.sendMessage("§cVous avez désactivé votre double saut");
            }
        }
    }

    @EventHandler
    private void OnToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (isFrog(player) && isPassifActive && cooldownManager.getRemainingCooldown(player, jumpAbility) == 0) {
            // Lancer le joueur dans la direction qu'il vise
            launchPlayer(player, 1.5); // Ajustez la puissance selon vos besoins
            // Démarrer le cooldown pour le double saut
            cooldownManager.startCooldown(player, jumpAbility);
        }
    }

    private void launchPlayer(Player player, double power) {
        // Obtenir la direction dans laquelle le joueur regarde
        Vector direction = player.getLocation().getDirection().normalize(); // Normaliser la direction
        player.setVelocity(direction.multiply(power)); // Multiplier la direction par la puissance
    }

    @EventHandler
    private void OnDeath(UhcDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = event.getKiller();

        boolean isMagma = false;
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        if(uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Frog")) {
            isMagma = true;
        }

        if (killer != null && isFrog(player) && isMagma) {
            hasKilledMagma = true;
            killer.sendMessage("§aVous avez tué le Magma Cube vous gagnez donc Force 1 de jour");
        }
    }

    private boolean isFrog(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Frog");
    }
}
