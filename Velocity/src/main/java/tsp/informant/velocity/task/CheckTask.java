package tsp.informant.velocity.task;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;

import tsp.informant.velocity.Informant;
import tsp.informant.velocity.config.Config;
import tsp.informant.velocity.util.Log;
import tsp.informant.velocity.util.Utils;

public class CheckTask implements Runnable {

    private final Config config = Informant.getInstance().getConfig();
    private final Map<Integer, PluginDescription> pluginIds = new HashMap<>();

    @Override
    public void run() {
        if (pluginIds.isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, PluginDescription> entry : pluginIds.entrySet()) {
            try {
                Informant.getInstance().getSpigotClient().getResource(entry.getKey(), resource -> {
                    if (entry.getValue() != null) {
                        // Check if version matches
                        if (!resource.getCurrentVersion().equalsIgnoreCase(entry.getValue().getVersion().get())) {
                            Informant.getInstance().getClient().send(Utils.buildUpdateMessage(resource, entry.getValue())).whenComplete((response, throwable) -> {
                                Log.debug("Sent message: " + response.getId());
                            });
                        }
                    }
                });
            } catch (IOException | JSONException ex) {
                Log.debug("Failed to fetch information for resource! ID: " + entry.getKey() + " | Plugin: " + entry.getValue().getName().get());
                Log.debug(ex);
            }
        }
    }

    public CheckTask load() {
        // Provided config list
        config.getPlugins().forEach((id, plugin) -> pluginIds.put(id, plugin.getDescription()));

        // Self checking
        if (config.checkInstalled()) {
            for (PluginContainer plugin : Informant.getInstance().getServer().getPluginManager().getPlugins()) {
                //First check for bungee.yml
                URL in = plugin.getInstance().get().getClass().getResource("bungee.yml");
                if(in == null) {
                    // check for plugin.yml
                    in = plugin.getInstance().get().getClass().getResource("plugin.yml");
                    if (in == null) {
                        // check for velocity-plugin.json
                        int id = Utils.extractId(plugin);
                        if (id != -1) {
                            try {
                                pluginIds.put(id, plugin.getDescription());
                            } catch (NumberFormatException ignored) {
                                Log.debug("Id is not a number for plugin: " + plugin.getDescription().getName());
                            }
                        } else {
                            Log.debug("Undefined spigot-id in plugin: " + plugin.getDescription().getName());
                        }
                    }
                }
            }
        }

        return this;
    }

}