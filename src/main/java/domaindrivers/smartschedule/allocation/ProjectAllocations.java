package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.util.Optional;

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

    static Decision<CapabilitiesAllocated> decide(ProjectAllocations state, Allocate command) {
        AllocatedCapability allocatedCapability =
                new AllocatedCapability(command.allocatableCapabilityId(), command.capability(), command.requestedSlot());
        Allocations newAllocations = state.allocations.add(allocatedCapability);
        if (noNewAllocation(state.allocations, newAllocations) || !withinTimeSlot(state.timeSlot, command.requestedSlot())) {
            return new Decision<>(state, Optional.empty());
        }
        ProjectAllocations nextState = new ProjectAllocations(state.projectId, newAllocations, state.demands, state.timeSlot);
        CapabilitiesAllocated event = new CapabilitiesAllocated(
                allocatedCapability.allocatedCapabilityID().getId(), state.projectId, nextState.missingDemands(), command.when());
        return new Decision<>(nextState, Optional.of(event));
    }

    private static boolean noNewAllocation(Allocations before, Allocations after) {
        return after.equals(before);
    }

    private static boolean withinTimeSlot(TimeSlot projectTimeSlot, TimeSlot requestedSlot) {
        if (projectTimeSlot == null || projectTimeSlot.equals(TimeSlot.empty())) {
            return true;
        }
        return requestedSlot.within(projectTimeSlot);
    }

    static Decision<CapabilityReleased> decide(ProjectAllocations state, Release command) {
        Allocations newAllocations = state.allocations.remove(command.allocatableCapabilityId(), command.slot());
        if (newAllocations.equals(state.allocations)) {
            return new Decision<>(state, Optional.empty());
        }
        ProjectAllocations nextState = new ProjectAllocations(state.projectId, newAllocations, state.demands, state.timeSlot);
        CapabilityReleased event = new CapabilityReleased(state.projectId, nextState.missingDemands(), command.when());
        return new Decision<>(nextState, Optional.of(event));
    }

    static Decision<ProjectAllocationScheduled> decide(ProjectAllocations state, DefineSlot command) {
        ProjectAllocations nextState = new ProjectAllocations(state.projectId, state.allocations, state.demands, command.timeSlot());
        ProjectAllocationScheduled event = new ProjectAllocationScheduled(state.projectId, command.timeSlot(), command.when());
        return new Decision<>(nextState, Optional.of(event));
    }

    static Decision<ProjectAllocationsDemandsScheduled> decide(ProjectAllocations state, AddDemands command) {
        Demands nextDemands = state.demands.withNew(command.newDemands());
        ProjectAllocations nextState = new ProjectAllocations(state.projectId, state.allocations, nextDemands, state.timeSlot);
        ProjectAllocationsDemandsScheduled event =
                new ProjectAllocationsDemandsScheduled(state.projectId, nextState.missingDemands(), command.when());
        return new Decision<>(nextState, Optional.of(event));
    }

    Demands missingDemands() {
        return demands.missingDemands(allocations);
    }

    Demands demands() {
        return demands;
    }

    Allocations allocations() {
        return allocations;
    }

    boolean hasTimeSlot() {
        return timeSlot != null && !timeSlot.equals(TimeSlot.empty());
    }

    ProjectAllocationsId id() {
        return projectId;
    }

    TimeSlot timeSlot() {
        return timeSlot;
    }
}

