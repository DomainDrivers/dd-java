package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Entity(name = "project_allocations")
class ProjectAllocations {

    @EmbeddedId
    private ProjectAllocationsId projectId;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Allocations allocations;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Demands demands;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "from", column = @Column(name = "from_date")), @AttributeOverride(name = "to", column = @Column(name = "to_date"))})
    private TimeSlot timeSlot;

    ProjectAllocations(ProjectAllocationsId projectId, Allocations allocations, Demands scheduledDemands, TimeSlot timeSlot) {
        this.projectId = projectId;
        this.allocations = allocations;
        this.demands = scheduledDemands;
        this.timeSlot = timeSlot;
    }

    static ProjectAllocations empty(ProjectAllocationsId projectId) {
        return new ProjectAllocations(projectId, Allocations.none(), Demands.none(), TimeSlot.empty());
    }

    static ProjectAllocations withDemands(ProjectAllocationsId projectId, Demands demands) {
        return new ProjectAllocations(projectId, Allocations.none(), demands);
    }

    ProjectAllocations() {
    }

    ProjectAllocations(ProjectAllocationsId projectId, Allocations allocations, Demands demands) {
        this.projectId = projectId;
        this.allocations = allocations;
        this.demands = demands;
    }

    Optional<CapabilitiesAllocated> allocate(ResourceId resourceId, Capability capability, TimeSlot requestedSlot, Instant when) {
        if (nothingAllocated() || !withinProjectTimeSlot(requestedSlot)) {
            return Optional.empty();
        }
        return Optional.of(new CapabilitiesAllocated(null, null, null, null, null));
    }

    private boolean nothingAllocated() {
        return false;
    }

    private boolean withinProjectTimeSlot(TimeSlot requestedSlot) {
        return false;
    }

    Optional<CapabilityReleased> release(UUID allocatedCapabilityId, TimeSlot timeSlot, Instant when) {
        if (nothingReleased()) {
            return Optional.empty();
        }
        return Optional.of(new CapabilityReleased(null, null, null));
    }

    private boolean nothingReleased() {
        return false;
    }

    Demands missingDemands() {
        return demands.missingDemands(allocations);
    }

    Allocations allocations() {
        return allocations;
    }

    boolean hasTimeSlot() {
        return timeSlot != null && !timeSlot.equals(TimeSlot.empty());
    }

}

