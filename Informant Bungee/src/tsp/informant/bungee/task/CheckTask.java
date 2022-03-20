package tsp.informant.bungee.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import tsp.informant.bungee.Informant;
import tsp.informant.bungee.util.Log;
import tsp.informant.bungee.util.Patterns;
import tsp.informant.bungee.util.Utils;

public class CheckTask implements Runnable {

    private final Map<Integer, Plugin> pluginIds = new HashMap<>();

    @Override
    public void run() {
        if (pluginIds.isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, Plugin> entry : pluginIds.entrySet()) {
            try {
				Informant.getInstance().getSpigotClient().getResource(entry.getKey(), resource -> {
				    if (entry.getValue() != null) {
				        // Check if version matches
				        if (!resource.getCurrentVersion().equalsIgnoreCase(entry.getValue().getDescription().getVersion())) {
				            Informant.getInstance().getClient().send(Utils.buildUpdateMessage(resource, entry.getValue())).whenComplete((response, throwable) -> {
				                Log.debug("Sent message: " + response.getId());
				            });
				        }
				    }
				});
            } catch (IOException | JSONException ex) {
                Log.debug("Failed to fetch information for resource! ID: " + entry.getKey() + " | Plugin: " + entry.getValue().getDescription().getName());
                Log.debug(ex);
            }
        }
    }

    public CheckTask load() {
        Configuration config = Informant.getInstance().getConfig();
        // Provided config list
        List<String> rawPlugins = config.getStringList("plugins");
        if (!rawPlugins.isEmpty()) {
            for (String entry : rawPlugins) {
                if (entry.isEmpty()) {
                    continue;
                }

                String[] args = Patterns.SPLIT.split(entry);
                int id = Integer.parseInt(args[0]);
                Plugin plugin = ProxyServer.getInstance().getPluginManager().getPlugin(args[1]);

                pluginIds.put(id, plugin);
            }
        }

        // Self checking
        if (config.getBoolean("checkInstalled")) {
            for (Plugin plugin : ProxyServer.getInstance().getPluginManager().getPlugins()) {
                InputStream in = plugin.getResourceAsStream("bungee.yml"); //First check for bungee.yml
                if(in == null)in = plugin.getResourceAsStream("plugin.yml"); //Missing? Check for plugin.yml
                if (in == null) { //Missing? lol, wtf - how can this be plugin
                    Log.debug("Failed to get plugin.yml from plugin: " + plugin.getDescription().getName());
                    continue;
                }
                Configuration data = YamlConfiguration.getProvider(YamlConfiguration.class).load(new InputStreamReader(in));
                if (data.contains("spigot-id")) {
                    try {
                        int id = Integer.parseInt(data.getString("spigot-id"));
                        pluginIds.put(id, plugin);
                    } catch (NumberFormatException ignored) {
                        Log.debug("Id is not a number for plugin: " + plugin.getDescription().getName());
                    }
                } else {
                    Log.debug("Undefined spigot-id in plugin: " + plugin.getDescription().getName());
                }
            }
        }

        return this;
    }

}
