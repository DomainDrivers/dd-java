package domaindrivers.smartschedule.allocation.capabilityscheduling;

import domaindrivers.smartschedule.availability.ResourceId;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AllocatableCapabilityId implements Serializable {

    public static AllocatableCapabilityId newOne() {
        return new AllocatableCapabilityId(UUID.randomUUID());
    }

    private UUID id;

    public AllocatableCapabilityId(UUID uuid) {
        this.id = uuid;
    }

    public AllocatableCapabilityId() {
    }

    public static AllocatableCapabilityId none() {
        return new AllocatableCapabilityId();
    }

    public UUID getId() {
        return id;
    }

    public ResourceId toAvailabilityResourceId() {
        return ResourceId.of(id.toString());
    }

    public static AllocatableCapabilityId from(ResourceId resourceId) {
        return new AllocatableCapabilityId(resourceId.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllocatableCapabilityId that = (AllocatableCapabilityId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
