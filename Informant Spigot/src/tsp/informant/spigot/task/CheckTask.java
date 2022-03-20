package tsp.informant.spigot.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.JSONException;

import tsp.informant.spigot.Informant;
import tsp.informant.spigot.util.Log;
import tsp.informant.spigot.util.Patterns;
import tsp.informant.spigot.util.Utils;

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
                Log.debug("Failed to fetch information for resource! ID: " + entry.getKey() + " | Plugin: " + entry.getValue().getName());
                Log.debug(ex);
            }
        }
    }

    public CheckTask load() {
        FileConfiguration config = Informant.getInstance().getConfig();
        // Provided config list
        List<String> rawPlugins = config.getStringList("plugins");
        if (!rawPlugins.isEmpty()) {
            for (String entry : rawPlugins) {
                if (entry.isEmpty()) {
                    continue;
                }

                String[] args = Patterns.SPLIT.split(entry);
                int id = Integer.parseInt(args[0]);
                Plugin plugin = Bukkit.getPluginManager().getPlugin(args[1]);

                pluginIds.put(id, plugin);
            }
        }

        // Self checking
        if (config.getBoolean("checkInstalled")) {
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                InputStream in = plugin.getResource("plugin.yml");
                if (in == null) {
                    Log.debug("Failed to get plugin.yml from plugin: " + plugin.getName());
                    continue;
                }

                YamlConfiguration data = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
                if (data.contains("spigot-id")) {
                    try {
                        int id = Integer.parseInt(data.getString("spigot-id"));
                        pluginIds.put(id, plugin);
                    } catch (NumberFormatException ignored) {
                        Log.debug("Id is not a number for plugin: " + plugin.getName());
                    }
                } else {
                    Log.debug("Undefined spigot-id in plugin: " + plugin.getName());
                }
            }
        }

        return this;
    }

}
