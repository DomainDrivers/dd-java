package domaindrivers.smartschedule.allocation.capabilityscheduling;


import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.util.Set;


@Entity(name = "allocatable_capabilities")
class AllocatableCapability {

    @EmbeddedId
    private AllocatableCapabilityId id = AllocatableCapabilityId.newOne();

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private CapabilitySelector possibleCapabilities;

    @Embedded
    private AllocatableResourceId resourceId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "from", column = @Column(name = "from_date")),
            @AttributeOverride(name = "to", column = @Column(name = "to_date"))
    })
    private TimeSlot timeSlot;

    AllocatableCapability(AllocatableResourceId resourceId, CapabilitySelector possibleCapabilities, TimeSlot timeSlot) {
        this.resourceId = resourceId;
        this.possibleCapabilities = possibleCapabilities;
        this.timeSlot = timeSlot;
    }

    public AllocatableCapability() {
    }

    AllocatableCapabilityId id() {
        return id;
    }

    boolean canPerform(Set<Capability> capabilities) {
        return capabilities().canPerform(capabilities);
    }

    AllocatableResourceId resourceId() {
        return resourceId;
    }

    TimeSlot slot() {
        return timeSlot;
    }

    CapabilitySelector capabilities() {
        return possibleCapabilities;
    }
}
