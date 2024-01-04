package domaindrivers.smartschedule.availability;

import java.util.UUID;

public record Owner(UUID owner) {

    static Owner none() {
        return new Owner(null);
    }

    public static Owner newOne() {
        return new Owner(UUID.randomUUID());
    }

    public static Owner of(UUID id) {
        return new Owner(id);
    }

    public boolean byNone() {
        return none().equals(this);
    }

    public UUID id() {
        return owner;
    }
}
