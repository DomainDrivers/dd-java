package domaindrivers.smartschedule.allocation.capabilityscheduling;


import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;


@Entity(name = "allocatable_capabilities")
class AllocatableCapability {

    @EmbeddedId
    private AllocatableCapabilityId id = AllocatableCapabilityId.newOne();

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Capability capability;

    @Embedded
    private AllocatableResourceId resourceId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "from", column = @Column(name = "from_date")),
            @AttributeOverride(name = "to", column = @Column(name = "to_date"))
    })
    private TimeSlot timeSlot;

    AllocatableCapability(AllocatableResourceId resourceId, Capability capability, TimeSlot timeSlot) {
        this.resourceId = resourceId;
        this.capability = capability;
        this.timeSlot = timeSlot;
    }

    public AllocatableCapability() {
    }

    AllocatableCapabilityId id() {
        return id;
    }


    public boolean canPerform(Capability capability) {
        return capability.equals(capability);
    }

    AllocatableResourceId resourceId() {
        return resourceId;
    }

    TimeSlot slot() {
        return timeSlot;
    }

    Capability capability() {
        return capability;
    }
}
