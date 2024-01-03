package domaindrivers.smartschedule.availability;

import java.util.UUID;

public record Owner(UUID owner) {

    static Owner none() {
        return new Owner(null);
    }

}
