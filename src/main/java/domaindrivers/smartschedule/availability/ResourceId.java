package domaindrivers.smartschedule.availability;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ResourceId implements Serializable {

    public static ResourceId newOne() {
        return new ResourceId(UUID.randomUUID());
    }

    private UUID id;

    public ResourceId(UUID uuid) {
        this.id = uuid;
    }

    public ResourceId() {
    }

    public static ResourceId none() {
        return new ResourceId(null);
    }

    public static ResourceId of(String id) {
        if (id == null) {
            return none();
        }
        return new ResourceId(UUID.fromString(id));
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceId resourceId = (ResourceId) o;
        return Objects.equals(resourceId.id, this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

