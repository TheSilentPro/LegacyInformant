package tsp.informant.velocity;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import tsp.informant.shared.client.SpigotClient;
import tsp.informant.velocity.config.Config;
import tsp.informant.velocity.task.CheckTask;
import tsp.informant.velocity.util.Log;

@Plugin(id = "informant", name = "Informant", version = "1.1", authors = {"Silent", "TheDevTec"})
public class Informant {

    private static Informant instance;
    private final ProxyServer server;
    private final Config config;
    private WebhookClient client;
    private SpigotClient spigotClient;

    @Inject
    public Informant(ProxyServer server, Logger logger) {
        instance = this;
        Log.info("Loading Informant - " + server.getPluginManager().getPlugin("informant").get().getDescription().getVersion().get());
        this.server = server;
        this.config = new Config(new File("plugins/Informant-Velocity/config.json"));
        this.config.createThenLoad();

        if (config.getWebhookUrl().isEmpty()) {
            Log.error("Invalid webhook url! Check your config.yml");
            return;
        }

        spigotClient = new SpigotClient();
        client = new WebhookClientBuilder(getConfig().getWebhookUrl())
                .setDaemon(true)
                .build();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new CheckTask().load(), 0L, config.getInterval(), TimeUnit.SECONDS);
        Log.info("Done!");
    }

    public SpigotClient getSpigotClient() {
        return spigotClient;
    }

    public WebhookClient getClient() {
        return client;
    }

    public Config getConfig() {
        return config;
    }

    public ProxyServer getServer() {
        return server;
    }

    public static Informant getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance is null.");
        }

        return instance;
    }


}