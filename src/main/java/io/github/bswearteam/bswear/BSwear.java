package io.github.bswearteam.bswear;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BSwear extends JavaPlugin implements Listener {
    public PluginDescriptionFile pdf = this.getDescription();
    public String version = pdf.getVersion();

    public static Permission BypassPerm = new Permission("bswear.bypass");
    public Permission CommandPerm       = new Permission("bswear.command.use");
    public Permission allPerm           = new Permission("bswear.*");
    public Permission AdvertisingBypass = new Permission("bswear.advertising.bypass");
    public FileConfiguration config = new YamlConfiguration();
    public FileConfiguration swears = new YamlConfiguration();
    public FileConfiguration swearers = new YamlConfiguration();
    public FileConfiguration muted = new YamlConfiguration();
    public FileConfiguration log = new YamlConfiguration();
    public String prefix = ChatColor.GOLD + "[BSwear] "+ChatColor.GREEN;
    public File configf,swearf,swearersf,logFile;
    public ArrayList<String> logtext = new ArrayList<String>();

    File mutedf;

    /**
     * code that runs when BSwear is enabled
     * 
     * @author The BSwear Team
     * */
    public void onEnable() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.addPermission(BypassPerm);
        pm.addPermission(CommandPerm);
        pm.addPermission(AdvertisingBypass);

        configf = new File(getDataFolder(), "config.yml");
        swearf = new File(getDataFolder(), "words.yml");
        swearersf = new File(getDataFolder(), "swearers.yml");
        mutedf = new File(getDataFolder(), "mutedPlayers.yml");
        logFile = new File(getDataFolder(), "log.yml");

        resourceSave(configf, "config.yml");
        resourceSave(swearf, "words.yml");
        resourceSave(swearersf, "swearers.yml");
        resourceSave(mutedf, "mutedPlayers.yml");

        if (!logFile.exists()){
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config.load(configf);
            swears.load(swearf);
            swearers.load(swearersf);
            muted.load(mutedf);
            log.load(logFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        saveDefaultConfig();

        // Shows an message saying BSwear is enabled
        if (getConfig().getBoolean("showEnabledMessage")) {
            getLogger().info("[=-=] BSwear team [=-=]");
            getLogger().info("This server runs BSwear v"+version);
            getLogger().info("- ClusterAPI by AdityaTD");
        }

        // Checks if both ban and kick are set to true
        if (getConfig().getBoolean("banSwearer") && getConfig().getBoolean("kickSwearer")) getConfig().set("banSwearer", false);

        // sets the prefix
        if (getConfig().getString("messages.prefix") == null) getConfig().set("messages.prefix", "&6[BSwear]&2");
        prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix")) + " ";

        pm.registerEvents(this, this);
        getCommand("mute").setExecutor(new Mute(this));
        getCommand("bswear").setExecutor(new BSwearCommand(this));
        registerEvents(pm, this, this, new OnJoin(this), new Mute(this), new Advertising(this));
    }
    
    public static void registerEvents(PluginManager p, org.bukkit.plugin.Plugin plugin, Listener... listeners) {
        for (Listener lis : listeners) p.registerEvents(lis, plugin);
    }

    /*Controls config files*/
    public void saveSwearConfig() { saveConf(swears, swearf); }
    public void saveSwearersConfig() { saveConf(swearers, swearersf); }

    /**
     * The swear blocker
     * 
     * @author BSwear Team
     */
    @EventHandler
    public void onChatSwear(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().hasPermission(BypassPerm)) {
            String message = replaceAllNotNormal(event.getMessage().toLowerCase().replaceAll("[%&*()$#!-_@]", ""));
            for (String word : swears.getStringList("warnList")) {
                if (ifHasWord(message, word)) {
                    if (getConfig().getBoolean("cancelMessage") == true) {
                        event.setCancelled(true); // Cancel message.
                    } else {
                        String messagewithoutswear = event.getMessage().replaceAll(word, SwearUtils.repeat("*", word.length()));
                        event.setMessage(messagewithoutswear);
                        event.getPlayer().sendMessage(ChatColor.DARK_GREEN+"[BSwear] "+ChatColor.YELLOW + ChatColor.AQUA + ChatColor.BOLD +"We've detected a swear word MIGHT be in your message so we blocked that word!");
                    }

                    List<String> l = log.getStringList("log");
                    String a = event.getPlayer().getName() + " said " + word.toUpperCase() + "in message: " + event.getMessage();
                    l.add(a);
                    log.set("log", l);
                    saveConf(log, logFile);

                    SwearUtils.checkAll(getConfig().getString("command"), event.getPlayer());
                }
            }
        }
    }

    public boolean ifHasWord(String message, String word) {
        boolean a = false;
        String[] messageAsArray = message.split(" ");
        int messageLength = messageAsArray.length;
        for (int i = 0; i < messageLength;) {
            String partOfMessage = messageAsArray[i];
            StringBuilder strBuilder = new StringBuilder();
            char[] messageAsCharArray = partOfMessage.toLowerCase().toCharArray();
            for(int h=0;h<messageAsCharArray.length;){
                char character=messageAsCharArray[h];
                if(character>='0'&&character<='9'||character>='a'&&character<='z') strBuilder.append(character);
                h++;
            }
            if (strBuilder.toString().equalsIgnoreCase(word.toLowerCase())) a = true;
            i++;
        }
        return a;
    }

    /**
     * Replaces all non-words and non-numbers.
     */
    public String replaceAllNotNormal(String str) {
        return str.replaceAll("[^\\p{L}\\p{Nd}]","").replaceAll("[ * . - = + : ]","").replaceAll("[%&*()$#!-_@]","");
    }

    public void saveConf(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().info("[ERROR] Cant save "+file.getName());
        } 
    }

    private void resourceSave(File file,String fileName){
        if(!file.exists()){file.getParentFile().mkdirs();saveResource(fileName,false);}
    }
}