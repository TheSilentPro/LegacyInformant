package tsp.informant.shared.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpigotClient {

    /**
     * Retrieve a resource from an id
     *
     * @param id The resource id
     */
    public void getResource(int id, Consumer<SpigotResource> resource) throws IOException {
        resource.accept(buildResource(read("?action=getResource&id=" + id).getAsJsonObject()));
    }

    /**
     * Used for building a {@link SpigotResource}
     *
     * @param json The json response of the resource
     * @return Wrapped resource
     */
    private SpigotResource buildResource(JsonObject json) {
        JsonObject premium = json.get("premium").getAsJsonObject();
        JsonObject author = json.get("author").getAsJsonObject();
        JsonObject stats = json.get("stats").getAsJsonObject();
        JsonObject reviews = stats.get("reviews").getAsJsonObject();

        List<String> versions = new ArrayList<>();
        JsonArray jsonSupportedMinecraftVersions = !json.get("supported_minecraft_versions").isJsonNull() ? json.get("supported_minecraft_versions").getAsJsonArray() : new JsonArray();
        for (JsonElement entry : jsonSupportedMinecraftVersions) {
            versions.add(entry.getAsString());
        }

        String nativeMinecraftVersion = !json.get("native_minecraft_version").isJsonNull()
                ? json.get("native_minecraft_version").getAsString()
                : "";

        return new SpigotResource(
                json.get("id").getAsInt(),
                json.get("title").getAsString(),
                json.get("tag").getAsString(),
                json.get("description").getAsString(),
                json.get("current_version").getAsString(),
                nativeMinecraftVersion,
                versions.toArray(new String[0]),
                json.get("icon_link").getAsString(),

                new SpigotResourceStatistics(
                        author.get("id").getAsInt(),
                        author.get("username").getAsString(),

                        premium.get("price").getAsDouble(),
                        premium.get("currency").getAsString(),

                        stats.get("downloads").getAsInt(),
                        stats.get("updates").getAsInt(),
                        stats.get("rating").getAsDouble(),
                        reviews.get("unique").getAsInt(),
                        reviews.get("total").getAsInt()
                )
        );
    }

    /**
     * Reads the json contents of the url
     *
     * @param url The url to read from
     * @return Json response
     * @throws IOException Error
     */
    private JsonElement read(String url) throws IOException {
        URLConnection connection = new URL("https://api.spigotmc.org/simple/0.2/index.php" + url).openConnection();
        connection.setConnectTimeout(5000);
        connection.setRequestProperty("User-Agent", "Informant");

        return JsonParser.parseReader(new BufferedReader(new InputStreamReader(connection.getInputStream())));
    }

    public static String fromStream(InputStream stream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8), 8192);
            StringBuilder sb = new StringBuilder(512);
            String content;
            while ((content = br.readLine()) != null)
                sb.append(content).append(System.lineSeparator());
            br.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

}
