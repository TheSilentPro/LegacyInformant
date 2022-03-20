package tsp.informant.velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.config.VelocityConfiguration;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import tsp.informant.velocity.client.SpigotClient;
import tsp.informant.velocity.task.CheckTask;
import tsp.informant.velocity.util.Log;

@Plugin(id = "informant", name = "Informant", version = "1.1", authors = {"Silent"})
public class Informant {

    private static Informant instance;
	public static ProxyServer server;
    private WebhookClient client;
    private SpigotClient spigotClient;
    private CommentedFileConfig config;

    @Inject
    public Informant(ProxyServer server, Logger logger) {
    	Informant.server = server;
        instance = this;
        Log.info("Loading Informant - "+server.getPluginManager().getPlugin("informant").get().getDescription().getVersion().get());
        //save default config
        if(!new File("plugins/Informant/config.yml").exists()) {
        	try {
	        	OutputStreamWriter s =new OutputStreamWriter(new FileOutputStream(new File("plugins/Informant/config.yml")));
	        	s.append(SpigotClient.fromStream(Informant.class.getResourceAsStream("config.yml")));
	        	s.flush();
	        	s.close();
        	}catch(Exception err) {}
        }
        try {
        	config = (CommentedFileConfig) CommentedFileConfig.builder(new File("plugins/Informant/config.yml")).defaultData(VelocityConfiguration.class.getClassLoader().getResource("default-velocity.toml")).autosave().preserveInsertionOrder().sync().build();
			config.load();
		} catch (Exception e) {
		}
        if ((getConfig().get("webhook.url")+"").isEmpty()) {
            Log.error("Invalid webhook url! Check your config.yml");
            return;
        }

        spigotClient = new SpigotClient();
        client = new WebhookClientBuilder(getConfig().get("webhook.url")+"")
                .setDaemon(true)
                .build();
        new Thread(() -> {
        	new CheckTask().load();
        }).start();
        
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

	public CommentedFileConfig getConfig() {
		return config;
	}

}
