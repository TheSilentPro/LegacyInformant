package tsp.informant.velocity.util;

import java.awt.Color;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.velocitypowered.api.plugin.PluginDescription;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import tsp.informant.velocity.Informant;
import tsp.informant.velocity.client.SpigotResource;
import tsp.informant.velocity.client.SpigotResourceStatistics;

public class Utils {

    private static final CommentedFileConfig config = Informant.getInstance().getConfig();

    public static WebhookEmbed buildUpdateMessage(SpigotResource resource, PluginDescription plugin) {
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
                .setAuthor(new WebhookEmbed.EmbedAuthor(translate(config.get("message.author.name"), resource), translate(config.get("message.author.iconLink"), resource), translate(config.get("message.author.url"), resource)))
                .setColor(Color.ORANGE.getRGB())
                .setTitle(new WebhookEmbed.EmbedTitle(translate(config.get("message.title.name"), resource), translate(config.get("message.title.url"), resource)))
                .setDescription(translate(config.get("message.description"), resource))
                .setTimestamp(Instant.now());

        List<String> fields = config.get("message.fields");
        if (!fields.isEmpty()) {
            for (String rawField : fields) {
                String[] args = Patterns.COLON.split(translatePlugin(rawField, resource, plugin));
                builder.addField(new WebhookEmbed.EmbedField(Boolean.parseBoolean(args[0]), args[1], args[2]));
            }
        }

        return builder.build();
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


    public static String colorize(String msg) {
        return msg.replace("&", "§");
    }

}
