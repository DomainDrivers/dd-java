package domaindrivers.smartschedule.availability;

import java.util.UUID;

public record ResourceAvailabilityId(UUID id) {

    static ResourceAvailabilityId none() {
        return new ResourceAvailabilityId(null);
    }

    public static ResourceAvailabilityId newOne() {
        return new ResourceAvailabilityId(UUID.randomUUID());
    }

    public static ResourceAvailabilityId of(String id) {
        if (id == null) {
            return none();
        }
        return new ResourceAvailabilityId(UUID.fromString(id));
    }
}
