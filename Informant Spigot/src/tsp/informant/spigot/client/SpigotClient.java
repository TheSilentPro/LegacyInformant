package tsp.informant.spigot.client;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONML;
import org.json.JSONObject;

import tsp.informant.spigot.Informant;

public class SpigotClient {

    /**
     * Retrieve a resource from an id
     *
     * @param id The resource id
     * @throws JSONException 
     */
    public void getResource(int id, Consumer<SpigotResource> resource) throws IOException, JSONException {
        resource.accept(buildResource(read("?action=getResource&id=" + id)));
    }

    /**
     * Used for building a {@link SpigotResource}
     *
     * @param json The json response of the resource
     * @return Wrapped resource
     * @throws JSONException 
     */
    private SpigotResource buildResource(JSONObject json) throws JSONException {
        JSONObject premium = json.getJSONObject("premium");
        JSONObject author = json.getJSONObject("author");
        JSONObject stats = json.getJSONObject("stats");
        JSONObject reviews = stats.getJSONObject("reviews");

        List<String> versions = new ArrayList<>();
        JSONArray jsonSupportedMinecraftVersions = json.get("supported_minecraft_versions")!=null ? json.getJSONArray("supported_minecraft_versions") : new JSONArray();
        for (int i = 0; i < jsonSupportedMinecraftVersions.length(); ++i) {
            versions.add(jsonSupportedMinecraftVersions.getString(i));
        }

        String nativeMinecraftVersion = json.get("native_minecraft_version")!=null
                ? json.getString("native_minecraft_version")
                : "";

        return new SpigotResource(
                json.getInt("id"),
                json.getString("title"),
                json.getString("tag"),
                json.getString("description"),
                json.getString("current_version"),
                nativeMinecraftVersion,
                versions.toArray(new String[0]),
                json.getString("icon_link"),

                new SpigotResourceStatistics(
                        author.getInt("id"),
                        author.getString("username"),

                        premium.getInt("price"),
                        premium.getString("currency"),

                        stats.getInt("downloads"),
                        stats.getInt("updates"),
                        stats.getDouble("rating"),
                        reviews.getInt("unique"),
                        reviews.getInt("total")
                )
        );
    }

    /**
     * Reads the json contents of the url
     *
     * @param url The url to read from
     * @return Json response
     * @throws IOException Error
     * @throws JSONException 
     */
    private JSONObject read(String url) throws IOException, JSONException {
        URLConnection connection = new URL("https://api.spigotmc.org/simple/0.2/index.php" + url).openConnection();
        connection.setConnectTimeout(5000);
        connection.setRequestProperty("User-Agent", "Informant/" + Informant.getInstance().getDescription().getVersion());
        return JSONML.toJSONObject(fromStream(connection.getInputStream()));
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
