package tsp.informant.velocity.util;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;

import club.minnced.discord.webhook.send.WebhookEmbed;
import tsp.informant.shared.client.SpigotResource;
import tsp.informant.shared.client.SpigotResourceStatistics;
import tsp.informant.velocity.Informant;
import tsp.informant.velocity.config.Config;

public class Utils {

    private static final Config config = Informant.getInstance().getConfig();

    public static WebhookEmbed buildUpdateMessage(SpigotResource resource, PluginDescription plugin) {
        return config.getMessage();
    }

    private static String translatePlugin(String s, SpigotResource resource, PluginDescription plugin) {
        return translate(s.replace("%pluginCurrentVersion%", plugin.getVersion().get()), resource);
    }

    public static String translate(String s, SpigotResource resource) {
        SpigotResourceStatistics stats = resource.getStatistics();

        return s.replace("%resourceId%", String.valueOf(resource.getId()))
                .replace("%resourceIconUrl%", resource.getIconLink())
                .replace("%resourceCurrentVersion%", resource.getCurrentVersion())
                .replace("%resourceNativeVersion%", resource.getNativeMinecraftVersion())
                .replace("%resourceSupportedVersions%", Arrays.toString(resource.getSupportedMinecraftVersion()))
                .replace("%resourceTitle%", resource.getTitle())
                .replace("%resourceTag%", resource.getTag())
                .replace("%resourceDescription%", resource.getDescription())

                .replace("%resourceDownloads%", String.valueOf(stats.getDownloads()))
                .replace("%resourceUpdates%", String.valueOf(stats.getUpdates()))
                .replace("%resourceRating%", String.valueOf(stats.getRating()))
                .replace("%resourceTotalReviews%", String.valueOf(stats.getTotalReviews()))
                .replace("%resourceUniqueReviews%", String.valueOf(stats.getUniqueReviews()))

                .replace("%resourcePrice%", String.valueOf(stats.getPrice()))
                .replace("%resourceCurrency%", stats.getCurrency())

                // Author
                .replace("%authorId%", String.valueOf(stats.getAuthorId()))
                .replace("%authorUsername%", stats.getAuthorUsername());
    }

    public static int extractId(PluginContainer plugin) {
        try {
            File file = new File(plugin.getClass().getResource("velocity-plugin.json").toURI());
            JsonObject main = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
            return main.get("spigot-id").getAsInt();
        } catch (URISyntaxException | FileNotFoundException ex) {
            Log.debug("Could not extract id from: " + plugin.getDescription().getName());
            Log.debug(ex);
            return -1;
        }
    }

    public static String colorize(String msg) {
        return msg.replace("&", "§");
    }

}