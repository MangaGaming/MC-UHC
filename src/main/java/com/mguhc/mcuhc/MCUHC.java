package com.mguhc.mcuhc;

import com.mguhc.UhcAPI;
import com.mguhc.mcuhc.command.ResurrectCommand;
import com.mguhc.mcuhc.roles.pacifiques.SnowGolemListener;
import com.mguhc.mcuhc.roles.pacifiques.PolarBearListener;
import com.mguhc.mcuhc.roles.pacifiques.FoxListener;
import com.mguhc.mcuhc.roles.pacifiques.CowListener;
import com.mguhc.mcuhc.roles.pacifiques.FrogListener;
import com.mguhc.mcuhc.roles.pacifiques.HorseListener;
import com.mguhc.mcuhc.roles.pacifiques.IronGolemListener;
import com.mguhc.mcuhc.roles.pacifiques.ParrotListener;
import com.mguhc.roles.Camp;
import com.mguhc.roles.UhcRole;
import com.mguhc.mcuhc.roles.pacifiques.AllayListener;
import com.mguhc.mcuhc.roles.pacifiques.CamelListener;
import com.mguhc.mcuhc.roles.pacifiques.CatListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MCUHC extends JavaPlugin {

    private static MCUHC instance;
    private List<UhcRole> roles;
    private Map<String, UhcRole> roleMap;

    public void onEnable() {
        instance = this;
        roles = new ArrayList<>();
        roleMap = new HashMap<>();
        UhcAPI.getInstance().setUhcName("Mc Uhc");
        initializeRole();
        initializeCamp();
        initializeListeners();

        getCommand("resurrect").setExecutor(new ResurrectCommand());
    }

    private void initializeListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        // RoleListener
        pluginManager.registerEvents(new AllayListener(), this);
        pluginManager.registerEvents(new CamelListener(), this);
        pluginManager.registerEvents(new CatListener() , this);
        pluginManager.registerEvents(new CowListener(), this);
        pluginManager.registerEvents(new FoxListener(), this);
        pluginManager.registerEvents(new FrogListener(), this);
        pluginManager.registerEvents(new HorseListener() , this);
        pluginManager.registerEvents(new IronGolemListener() , this);
        pluginManager.registerEvents(new ParrotListener() , this);
        pluginManager.registerEvents(new PolarBearListener() , this);
        pluginManager.registerEvents(new SnowGolemListener() , this);
    }

    private void initializeRole() {
        // Supprimer tous les rôles précédents
        roles.clear();
        roleMap.clear();

        // Créez et stockez les rôles dans la liste et la carte pour le camp Pacifiques
        addRole("Allay", "Vous êtes Allay");
        addRole("Camel", "Vous êtes Camel");
        addRole("Cat", "Vous êtes Cat");
        addRole("Cow", "Vous êtes Cow");
        addRole("Fox", "Vous êtes Fox");
        addRole("Frog", "Vous êtes Frog");
        addRole("Horse", "Vous êtes Horse");
        addRole("Iron Golem", "Vous êtes Iron Golem");
        addRole("Parrot", "Vous êtes Parrot");
        addRole("Polar Bear", "Vous êtes Polar Bear");
        addRole("Snow Golem", "Vous êtes Snow Golem");
        addRole("Steve", "Vous êtes Steve");
        addRole("Squid", "Vous êtes Squid");
        addRole("Villager", "Vous êtes Villager");
        addRole("Wandering Trader", "Vous êtes Wandering Trader");
        addRole("Wolf", "Vous êtes Wolf");

        // Rôles pour le camp Hostiles
        addRole("Creeper", "Vous êtes Creeper");
        addRole("Enderman", "Vous êtes Enderman");
        addRole("Guardian", "Vous êtes Guardian");
        addRole("Magma Cube", "Vous êtes Magma Cube");
        addRole("Phantom", "Vous êtes Phantom");
        addRole("Pigmen", "Vous êtes Pigmen");
        addRole("Spider", "Vous êtes Spider");
        addRole("Shulker", "Vous êtes Shulker");
        addRole("Skeleton", "Vous êtes Skeleton");
        addRole("Witch", "Vous êtes Witch");
        addRole("Zombie", "Vous êtes Zombie");

        // Rôles pour le camp Solo
        addRole("Elder Guardian", "Vous êtes Elder Guardian");
        addRole("Ender Dragon", "Vous êtes Ender Dragon");
        addRole("Warden", "Vous êtes Warden");

        // Ajoutez tous les rôles au RoleManager
        for (UhcRole role : roles) {
            UhcAPI.getInstance().getRoleManager().addRole(role);
        }
    }

    private void addRole(String name, String description) {
        UhcRole role = new UhcRole(name, description);
        roles.add(role);
        roleMap.put(name, role); // Store the role in the map
    }

    private void initializeCamp() {
        // Créer le camp Pacifiques
        Camp peacefulCamp = new Camp("Pacifiques", "Location of Peaceful Camp");
        peacefulCamp.addRole(roleMap.get("Allay"));
        peacefulCamp.addRole(roleMap.get("Camel"));
        peacefulCamp.addRole(roleMap.get("Cat"));
        peacefulCamp.addRole(roleMap.get("Cow"));
        peacefulCamp.addRole(roleMap.get("Fox"));
        peacefulCamp.addRole(roleMap.get("Frog"));
        peacefulCamp.addRole(roleMap.get("Horse"));
        peacefulCamp.addRole(roleMap.get("Iron Golem"));
        peacefulCamp.addRole(roleMap.get("Parrot"));
        peacefulCamp.addRole(roleMap.get("Polar Bear"));
        peacefulCamp.addRole(roleMap.get("Snow Golem"));
        peacefulCamp.addRole(roleMap.get("Steve"));
        peacefulCamp.addRole(roleMap.get("Squid"));
        peacefulCamp.addRole(roleMap.get("Villager"));
        peacefulCamp.addRole(roleMap.get("Wandering Trader"));
        peacefulCamp.addRole(roleMap.get("Wolf"));
        UhcAPI.getInstance().getRoleManager().addCamp(peacefulCamp);

        // Créer le camp Hostiles
        Camp hostileCamp = new Camp("Hostiles", "Location of Hostile Camp");
        hostileCamp.addRole(roleMap.get("Creeper"));
        hostileCamp.addRole(roleMap.get("Enderman"));
        hostileCamp.addRole(roleMap.get("Guardian"));
        hostileCamp.addRole(roleMap.get("Magma Cube"));
        hostileCamp.addRole(roleMap.get("Phantom"));
        hostileCamp.addRole(roleMap.get("Pigmen"));
        hostileCamp.addRole(roleMap.get("Spider"));
        hostileCamp.addRole(roleMap.get("Shulker"));
        hostileCamp.addRole(roleMap.get("Skeleton"));
        hostileCamp.addRole(roleMap.get("Witch"));
        hostileCamp.addRole(roleMap.get("Zombie"));
        UhcAPI.getInstance().getRoleManager().addCamp(hostileCamp);

        // Créer le camp Solo
        Camp soloCamp = new Camp("Solo", "Location of Solo Camp");
        soloCamp.addRole(roleMap.get("Elder Guardian"));
        soloCamp.addRole(roleMap.get("Ender Dragon"));
        soloCamp.addRole(roleMap.get("Warden"));
        UhcAPI.getInstance().getRoleManager().addCamp(soloCamp);
    }

    public static MCUHC getInstance() {
        return instance;
    }
}