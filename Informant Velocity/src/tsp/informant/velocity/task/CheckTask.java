package tsp.informant.velocity.task;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONException;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.proxy.config.VelocityConfiguration;

import tsp.informant.velocity.Informant;
import tsp.informant.velocity.util.Log;
import tsp.informant.velocity.util.Patterns;
import tsp.informant.velocity.util.Utils;

public class CheckTask implements Runnable {

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
        CommentedFileConfig config = Informant.getInstance().getConfig();
        // Provided config list
        List<String> rawPlugins = config.get("plugins");
        if (!rawPlugins.isEmpty()) {
            for (String entry : rawPlugins) {
                if (entry.isEmpty()) {
                    continue;
                }

                String[] args = Patterns.SPLIT.split(entry);
                int id = Integer.parseInt(args[0]);
                Optional<PluginContainer> plugin = Informant.server.getPluginManager().getPlugin(args[1]);
                if(plugin.isPresent())
                pluginIds.put(id, plugin.get().getDescription());
            }
        }

        // Self checking
        if ((boolean)config.get("checkInstalled")) {
            for (PluginContainer plugin : Informant.server.getPluginManager().getPlugins()) {
                URL in = plugin.getInstance().get().getClass().getResource("bungee.yml"); //First check for bungee.yml
                if(in == null)in = plugin.getInstance().get().getClass().getResource("plugin.yml"); //Missing? Check for plugin.yml
                if (in == null) { //Missing? lol, wtf - how can this be plugin
                    Log.debug("Failed to get plugin.yml from plugin: " + plugin.getDescription().getName());
                    continue;
                }
                CommentedFileConfig data;
				try {
					data = CommentedFileConfig.builder(new File(in.toURI())).defaultData(VelocityConfiguration.class.getClassLoader().getResource("default-velocity.toml")).autosave().preserveInsertionOrder().sync().build();
				} catch (URISyntaxException e) {
					e.printStackTrace();
					return this;
				}
                if (data.contains("spigot-id")) {
                    try {
                        int id = Integer.parseInt((String)data.get("spigot-id"));
                        pluginIds.put(id, plugin.getDescription());
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
