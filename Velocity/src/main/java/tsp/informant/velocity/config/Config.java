package tsp.informant.velocity.config;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginManager;
import tsp.informant.shared.util.Patterns;
import tsp.informant.velocity.Informant;
import tsp.informant.velocity.util.Log;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Config {

    private File file;
    private JsonObject main;

    public Config(File file) {
        this.file = file;
    }

    /**
     * Retrieve the interval of checking.
     *
     * @return interval
     */
    public long getInterval() {
        return main.get("interval").getAsLong();
    }

    /**
     * Retrieve the webhook url
     *
     * @return url
     */
    public String getWebhookUrl() {
        return main.get("webhook-url").getAsString();
    }

    /**
     * Retrieve if installed plugins should be checked.
     *
     * @return Whether installed plugins should be checked
     */
    public boolean checkInstalled() {
        return main.get("checkInstalled").getAsBoolean();
    }

    /**
     * Retrieve the <solid>untranslated</solid> embed message from the config.
     *
     * @return Webhook embed message
     */
    @Nullable
    public WebhookEmbed getMessage() {
        JsonObject message = main.get("message").getAsJsonObject();
        JsonObject author = message.get("author").getAsJsonObject();
        JsonObject title = message.get("title").getAsJsonObject();
        JsonObject footer = message.get("footer").getAsJsonObject();
        JsonArray fields = message.get("fields").getAsJsonArray();

        WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
                .setColor(Color.ORANGE.getRGB())
                .setAuthor(new WebhookEmbed.EmbedAuthor(author.get("name").getAsString(), author.get("iconLink").getAsString(), author.get("url").getAsString()))
                .setTitle(new WebhookEmbed.EmbedTitle(title.get("text").getAsString(), title.get("url").getAsString()))
                .setDescription(main.get("description").getAsString())
                .setFooter(new WebhookEmbed.EmbedFooter(footer.get("text").getAsString(), footer.get("url").getAsString()));

        if (message.get("timestamp").getAsBoolean()) {
            builder.setTimestamp(Instant.now());
        }

        if (!fields.isEmpty()) {
            for (JsonElement entry : fields) {
                JsonObject field = entry.getAsJsonObject();
                builder.addField(new WebhookEmbed.EmbedField(field.get("inline").getAsBoolean(), field.get("name").getAsString(), field.get("value").getAsString()));
            }
        }

        return builder.build();
    }

    /**
     * Retrieve a map of plugins and their respective ids
     *
     * @return Mapped plugins
     */
    public Map<Integer, PluginContainer> getPlugins() {
        PluginManager manager = Informant.getInstance().getServer().getPluginManager();
        Map<Integer, PluginContainer> result = new HashMap<>();
        List<String> rawPlugins = getRawPlugins();
        for (String provided : rawPlugins) {
            String[] args = Patterns.COLON.split(provided);
            Optional<PluginContainer> plugin = manager.getPlugin(args[1]);
            if (plugin.isPresent()) {
                result.put(Integer.parseInt(args[0]), plugin.get());
            } else {
                Log.warning("No plugin found: " + args[1]);
            }
        }

        return result;
    }

    public List<String> getRawPlugins() {
        List<String> result = new ArrayList<>();
        JsonArray array = main.get("plugins").getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            result.add(array.get(i).getAsString());
        }

        return result;
    }

    public boolean isDebug() {
        return main.get("debug").getAsBoolean();
    }

    public void createThenLoad() {
        createIfAbsent();
        load();
    }

    public void load() {
        try {
            main = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
        } catch (FileNotFoundException ex) {
            Log.error("Failed to load config.json!");
            Log.error(ex);
        }
    }

    public void createIfAbsent() {
        if (file != null && !file.exists()) {
            try {
                file = new File(Informant.getInstance().getClass().getResource("config.json").toURI());
                file.createNewFile();
            } catch (URISyntaxException | IOException ex) {
                Log.error("Failed to create config.json!");
                Log.error(ex);
            }
        }
    }

    public File getFile() {
        return file;
    }

}
