package domaindrivers.smartschedule.allocation;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ResourceId implements Serializable {

    public static ResourceId newOne() {
        return new ResourceId(UUID.randomUUID());
    }

    private UUID resourceId;

    ResourceId(UUID uuid) {
        this.resourceId = uuid;
    }

    public ResourceId() {
    }

    public UUID id() {
        return resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceId projectId1 = (ResourceId) o;
        return Objects.equals(resourceId, projectId1.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId);
    }

}

