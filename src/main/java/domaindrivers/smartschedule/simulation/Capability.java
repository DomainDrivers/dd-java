package domaindrivers.smartschedule.simulation;


import java.io.Serializable;

record Capability(String name, String type) implements Serializable {

    static Capability skill(String name) {
        return new Capability(name, "SKILL");
    }

    static Capability permission(String name) {
        return new Capability(name, "PERMISSION");
    }

    static Capability asset(String asset) {
        return new Capability(asset, "ASSET");
    }

}