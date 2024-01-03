package domaindrivers.smartschedule.availability;

import java.util.UUID;

public record Owner(UUID owner) {

    static Owner none() {
        return new Owner(null);
    }

    static Owner newOne() {
        return new Owner(UUID.randomUUID());
    }

    public boolean byNone() {
        return none().equals(this);
    }

    public UUID id() {
        return owner;
    }
}
