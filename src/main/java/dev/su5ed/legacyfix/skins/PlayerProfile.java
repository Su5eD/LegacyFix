package dev.su5ed.legacyfix.skins;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerProfile {
    private final UUID id;
    private final String name;
    private final Map<String, String> properties = new HashMap<>();
    
    public static PlayerProfile create(String id, String name) {
        return new PlayerProfile(uuidFromString(id), name);
    }
    
    private PlayerProfile(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return uuidToString(id);
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public static String uuidToString(final UUID value) {
        return value.toString().replace("-", "");
    }
    
    private static UUID uuidFromString(String input) {
        return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }
}
