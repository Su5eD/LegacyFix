package dev.su5ed.legacyfix.skins;

import argo.format.CompactJsonFormatter;
import argo.format.JsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;
import org.bouncycastle.util.encoders.Base64;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class PlayerTextures {
    public static final PlayerTextures INSTANCE = new PlayerTextures();
    
    private static final String PROFILE_API = "https://api.mojang.com/profiles/minecraft";
    private static final String SESSION_HOST = "https://sessionserver.mojang.com/session/minecraft/";
    private static final JsonFormatter JSON_FORMATTER = new CompactJsonFormatter();

    private final Map<String, Map<TextureType, String>> textures = new HashMap<>();

    public String getTextureURL(String username, TextureType type) {
        Map<TextureType, String> map = textures.get(username);
        if (map == null) {
            map = getTextures(username);
            textures.put(username, map);
        }
        
        String url = map.get(type);
        if (url != null) {
            return url;
        }

        return "https://skins.minecraft.net/MinecraftSkins/" + username + ".png";
    }
    
    public static Map<TextureType, String> getTextures(String username) {
        PlayerProfile profile = getProfile(username);
        if (profile != null) {
            fillProfileProperties(profile);
            return getProfileTextures(profile);
        }
        return Collections.emptyMap();
    }
    
    public static Map<TextureType, String> getProfileTextures(PlayerProfile profile) {
        Map<TextureType, String> map = new HashMap<>();
        String property = profile.getProperties().get("textures");
        if (property != null) {
            try {
                String data = new String(Base64.decode(property), StandardCharsets.UTF_8);
                JsonRootNode json = new JdomParser().parse(data);
                Map<JsonStringNode, JsonNode> textures = json.getObjectNode("textures");
                for (Map.Entry<JsonStringNode, JsonNode> entry : textures.entrySet()) {
                    String type = entry.getKey().getText();
                    String url = entry.getValue().getStringValue("url");
                    
                    map.put(TextureType.valueOf(type), url);
                }
            } catch (InvalidSyntaxException ignored) {}
        }
        return map;
    }
    
    public static void fillProfileProperties(PlayerProfile profile) {
        Map<String, String> profileProps = profile.getProperties();
        String url = SESSION_HOST + "profile/" + profile.getId();
        JsonRootNode response = httpGet(url);
        if (response != null) {
            List<JsonNode> properties = response.getArrayNode("properties");
            for (JsonNode prop : properties) {
                profileProps.put(prop.getStringValue("name"), prop.getStringValue("value"));
            }
        }
    }
    
    public static PlayerProfile getProfile(String username) {
        JsonRootNode input = JsonNodeFactories.array(JsonNodeFactories.string(username));
        JsonRootNode response = httpPost(PROFILE_API, JSON_FORMATTER.format(input));
        if (response != null) {
            for (JsonNode node : response.getElements()) {
                String name = node.getStringValue("name");
                if (name.equals(username)) {
                    String id = node.getStringValue("id");
                    return PlayerProfile.create(id, name);
                }
            }
        }
        return null;
    }
    
    private static JsonRootNode httpPost(String url, String data) {
        try {
            URL targetUrl = new URL(url);
            HttpURLConnection con = (HttpURLConnection) targetUrl.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            
            try(OutputStream os = con.getOutputStream()) {
                byte[] input = data.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);			
            }
            
            try(Reader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                return new JdomParser().parse(reader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JsonRootNode httpGet(String url) {
        try {
            URL targetUrl = new URL(url);
            HttpURLConnection con = (HttpURLConnection) targetUrl.openConnection();
            con.setRequestMethod("GET");

            try(Reader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                return new JdomParser().parse(reader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private PlayerTextures() {}
}
