package domaindrivers.smartschedule.allocation.capabilityscheduling;

import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Calendars;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class CapabilityFinder {

    private final AvailabilityFacade availabilityFacade;
    private final AllocatableCapabilityRepository allocatableResourceRepository;

    CapabilityFinder(AvailabilityFacade availabilityFacade, AllocatableCapabilityRepository allocatableResourceRepository) {
        this.availabilityFacade = availabilityFacade;
        this.allocatableResourceRepository = allocatableResourceRepository;
    }

    AllocatableCapabilitiesSummary findAvailableCapabilities(Capability capability, TimeSlot timeSlot) {
        List<AllocatableCapability> findAllocatableCapability = allocatableResourceRepository.findByCapabilityWithin(capability.name(), capability.type(), timeSlot.from(), timeSlot.to());
        List<AllocatableCapability> found = filterAvailabilityInTimeSlot(findAllocatableCapability, timeSlot);
        return createSummary(found);
    }

    public AllocatableCapabilitiesSummary findCapabilities(Capability capability, TimeSlot timeSlot) {
        List<AllocatableCapability> found = allocatableResourceRepository.findByCapabilityWithin(capability.name(), capability.type(), timeSlot.from(), timeSlot.to());
        return createSummary(found);
    }

    public AllocatableCapabilitiesSummary findById(List<AllocatableCapabilityId> allocatableCapabilityIds) {
        List<AllocatableCapability> allByIdIn = allocatableResourceRepository.findAllById(allocatableCapabilityIds);
        return createSummary(allByIdIn);
    }

    private List<AllocatableCapability> filterAvailabilityInTimeSlot(List<AllocatableCapability> findAllocatableCapability, TimeSlot timeSlot) {
        Set<domaindrivers.smartschedule.availability.ResourceId> resourceIds =
                findAllocatableCapability
                        .stream()
                        .map(ac -> ac.id().toAvailabilityResourceId())
                        .collect(Collectors.toSet());
        Calendars calendars = availabilityFacade.loadCalendars(resourceIds, timeSlot);
        return findAllocatableCapability
                .stream()
                .filter(ac -> calendars.get(ac.id().toAvailabilityResourceId()).availableSlots().contains(timeSlot))
                .collect(toList());
    }

    private AllocatableCapabilitiesSummary createSummary(List<AllocatableCapability> from) {
        return new AllocatableCapabilitiesSummary(
                from.stream()
                        .map(allocatableCapability -> new AllocatableCapabilitySummary(allocatableCapability.id(), allocatableCapability.resourceId(), allocatableCapability.capabilities(), allocatableCapability.slot()))
                        .collect(toList()));
    }

    public boolean isPresent(AllocatableCapabilityId allocatableCapabilityId) {
        return allocatableResourceRepository.existsById(allocatableCapabilityId);
    }
}
