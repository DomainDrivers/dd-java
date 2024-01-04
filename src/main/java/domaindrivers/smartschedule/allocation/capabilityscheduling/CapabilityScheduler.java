package domaindrivers.smartschedule.allocation.capabilityscheduling;

import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Set;

public class CapabilityScheduler {

    private final AvailabilityFacade availabilityFacade;
    private final AllocatableCapabilityRepository allocatableResourceRepository;

    public CapabilityScheduler(AvailabilityFacade availabilityFacade, AllocatableCapabilityRepository allocatableResourceRepository) {
        this.availabilityFacade = availabilityFacade;
        this.allocatableResourceRepository = allocatableResourceRepository;
    }

    @Transactional
    public List<AllocatableCapabilityId> scheduleResourceCapabilitiesForPeriod(AllocatableResourceId resourceId, List<CapabilitySelector> capabilities, TimeSlot timeSlot) {
        List<AllocatableCapabilityId> allocatableResourceIds = createAllocatableResources(resourceId, capabilities, timeSlot);
        allocatableResourceIds.forEach(resource ->
                availabilityFacade.createResourceSlots(resource.toAvailabilityResourceId(), timeSlot));
        return allocatableResourceIds;
    }

    @Transactional
    public List<AllocatableCapabilityId> scheduleMultipleResourcesForPeriod(Set<AllocatableResourceId> resources, Capability capability, TimeSlot timeSlot) {
        List<AllocatableCapability> allocatableCapability =
                resources
                        .stream()
                        .map(resource -> new AllocatableCapability(resource, CapabilitySelector.canJustPerform(capability), timeSlot)).toList();
        allocatableResourceRepository.saveAll(allocatableCapability);
        allocatableCapability.forEach(
                resource -> availabilityFacade.createResourceSlots(resource.id().toAvailabilityResourceId(), timeSlot));
        return allocatableCapability
                .stream()
                .map(AllocatableCapability::id)
                .toList();
    }

    private List<AllocatableCapabilityId> createAllocatableResources(AllocatableResourceId resourceId, List<CapabilitySelector> capabilities, TimeSlot timeSlot) {
        List<AllocatableCapability> allocatableResources = capabilities
                .stream()
                .map(capability -> new AllocatableCapability(resourceId, capability, timeSlot))
                .toList();
        allocatableResourceRepository.saveAll(allocatableResources);
        return allocatableResources
                .stream()
                .map(AllocatableCapability::id)
                .toList();
    }

    public AllocatableCapabilityId findResourceCapabilities(AllocatableResourceId resourceId, Capability capability, TimeSlot period) {
        return allocatableResourceRepository
                .findByResourceIdAndCapabilityAndTimeSlot(resourceId.id(), capability.name(), capability.type(), period.from(), period.to())
                .map(AllocatableCapability::id)
                .orElse(null);
    }

    AllocatableCapabilityId findResourceCapabilities(AllocatableResourceId allocatableResourceId, Set<Capability> capabilities, TimeSlot timeSlot) {
        return allocatableResourceRepository
                .findByResourceIdAndTimeSlot(allocatableResourceId.id(), timeSlot.from(), timeSlot.to())
                .stream()
                .filter(ac -> ac.canPerform(capabilities))
                .map(AllocatableCapability::id)
                .findFirst()
                .orElse(null);
    }
}
