package domaindrivers.smartschedule.allocation.capabilityscheduling;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AllocatableResourceId implements Serializable {

    private UUID resourceId;

    public AllocatableResourceId(UUID uuid) {
        this.resourceId = uuid;
    }

    public AllocatableResourceId() {

    }

    public static AllocatableResourceId newOne() {
        return new AllocatableResourceId(UUID.randomUUID());
    }

    public UUID id() {
        return resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllocatableResourceId that = (AllocatableResourceId) o;
        return Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId);
    }

}
