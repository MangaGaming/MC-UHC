package com.mguhc.mcuhc.roles.pacifiques.cow;

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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CowListener implements Listener {

    private final EffectManager effectManager;
    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;
    private final RoleManager roleManager;
    private final PlayerManager playerManager;

    private Ability milkAbility;
    private final List<Player> playersMilked = new ArrayList<>();

    public CowListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Cow");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            new BukkitRunnable() {
                @Override
                public void run() {
                    checkNegativePotionEffects(player);
                }
            }.runTaskTimer(MCUHC.getInstance(), 0, 3*20);

            milkAbility = new Ability("/mc milk", 10*60*1000);
            abilityManager.registerAbility(uhcPlayer.getRole(), Collections.singletonList(milkAbility));
        }
    }

    private void checkNegativePotionEffects(Player player) {
        if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
            effectManager.removeEffect(player, PotionEffectType.WEAKNESS);
        } else if (player.hasPotionEffect(PotionEffectType.SLOW)) {
            player.removePotionEffect(PotionEffectType.SLOW);
        } else if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
            player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
        } else if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        } else if (player.hasPotionEffect(PotionEffectType.HUNGER)) {
            player.removePotionEffect(PotionEffectType.HUNGER);
        } else if (player.hasPotionEffect(PotionEffectType.CONFUSION)) {
            player.removePotionEffect(PotionEffectType.CONFUSION);
        } else if (player.hasPotionEffect(PotionEffectType.POISON)) {
            player.removePotionEffect(PotionEffectType.POISON);
        } else if (player.hasPotionEffect(PotionEffectType.WITHER)) {
            player.removePotionEffect(PotionEffectType.WITHER);
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if (isCow(player) && args.length == 3 && event.getMessage().contains("/mc milk")) {
            if (cooldownManager.getRemainingCooldown(player, milkAbility) == 0) {
                Player target = Bukkit.getPlayer(args[2]);
                if (target != null) {
                    if (!playersMilked.contains(target)) {
                        playersMilked.add(target);
                        int[] timer = {0};
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                checkNegativePotionEffects(target);
                                if (timer[0] == 5*60) {
                                    cancel();
                                }
                                timer[0] ++;
                            }
                        }.runTaskTimer(MCUHC.getInstance(), 0, 20);
                    }
                    else {
                        player.sendMessage("§cVous avez déja utilisé votre pouvoir sur le joueur visée");
                    }
                }
                else {
                    player.sendMessage("§cLe joueur visée n'existe pas ou n'est pas en ligne");
                }
            }
            else {
                player.sendMessage("§cVous êtes en cooldown pour " + cooldownManager.getRemainingCooldown(player, milkAbility) + "secondes");
            }
        }
    }

    private boolean isCow(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Cow");
    }
}
