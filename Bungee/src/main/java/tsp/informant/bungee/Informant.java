package tsp.informant.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import tsp.informant.bungee.task.CheckTask;
import tsp.informant.bungee.util.Log;
import tsp.informant.shared.client.SpigotClient;

public class Informant extends Plugin {

    private static Informant instance;
    private WebhookClient client;
    private SpigotClient spigotClient;
    private Configuration config;

    @Override
    public void onEnable() {
        instance = this;
        Log.info("Loading Informant - " + getDescription().getVersion());

        //save default config
        if(!new File("plugins/Informant/config.yml").exists()) {
            try (OutputStreamWriter s = new OutputStreamWriter(new FileOutputStream("plugins/Informant/config.yml"))) {
                s.append(SpigotClient.fromStream(getResourceAsStream("config.yml")));
            }catch(Exception ex) {
                Log.error("Failed to load config.");
                Log.error(ex);
            }
        }

        try {
            config = YamlConfiguration.getProvider(YamlConfiguration.class).load(new File("plugins/Informant/config.yml"));
        } catch (Exception ex) {
            Log.error("Failed to load config.");
            Log.error(ex);
        }

        if (getConfig().getString("webhook.url").isEmpty()) {
            Log.error("Invalid webhook url! Check your config.yml");
            return;
        }

        spigotClient = new SpigotClient();
        client = new WebhookClientBuilder(getConfig().getString("webhook.url"))
                .setDaemon(true)
                .build();

        new Metrics(this, 14761);

        getProxy().getScheduler().schedule(this, new CheckTask().load(), 0L, getConfig().getLong("interval") * 20L, TimeUnit.SECONDS);
        Log.info("Done!");
    }

    public SpigotClient getSpigotClient() {
        return spigotClient;
    }

    public WebhookClient getClient() {
        return client;
    }

    public static Informant getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }

        return instance;
    }

    public Configuration getConfig() {
        return config;
    }

}