package domaindrivers.smartschedule.shared.capability;


import java.io.Serializable;
import java.util.List;
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

    public static List<Capability> skills(String ... skills) {
        return Stream.of(skills).map(Capability::skill).toList();
    }
}