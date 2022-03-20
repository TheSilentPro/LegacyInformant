package tsp.informant.spigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import tsp.informant.spigot.client.SpigotClient;
import tsp.informant.spigot.task.CheckTask;
import tsp.informant.spigot.util.Log;

public class Informant extends JavaPlugin {

    private static Informant instance;
    private WebhookClient client;
    private SpigotClient spigotClient;

    @Override
    public void onEnable() {
        instance = this;
        Log.info("Loading Informant - " + getDescription().getVersion());
        saveDefaultConfig();

        if (getConfig().getString("webhook.url").isEmpty()) {
            Log.error("Invalid webhook url! Check your config.yml");
            this.setEnabled(false);
            return;
        }

        spigotClient = new SpigotClient();
        client = new WebhookClientBuilder(getConfig().getString("webhook.url"))
                .setDaemon(true)
                .build();

        new Metrics(this, 14694);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new CheckTask().load(), 0L, getConfig().getLong("interval") * 20L);
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

}
