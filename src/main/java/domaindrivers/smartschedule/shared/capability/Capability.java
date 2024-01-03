package domaindrivers.smartschedule.shared.capability;


import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Capability(String name, String type) implements Serializable {

    public static Capability skill(String name) {
        return new Capability(name, "SKILL");
    }

    public static Capability permission(String name) {
        return new Capability(name, "PERMISSION");
    }

    public static Capability asset(String asset) {
        return new Capability(asset, "ASSET");
    }

    public static Set<Capability> skills(String ... skills) {
        return Stream.of(skills).map(Capability::skill).collect(Collectors.toSet());
    }

    public static Set<Capability> assets(String ... assets) {
        return Stream.of(assets).map(Capability::asset).collect(Collectors.toSet());
    }

    public static Set<Capability>  permissions(String ... permissions) {
        return Stream.of(permissions).map(Capability::permission).collect(Collectors.toSet());
    }

    public boolean isOfType(String type) {
        return this.type.equals(type);
    }
}