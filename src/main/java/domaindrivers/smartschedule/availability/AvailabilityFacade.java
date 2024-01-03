package domaindrivers.smartschedule.availability;


import domaindrivers.smartschedule.availability.segment.Segments;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.transaction.Transactional;

import java.util.Set;

import static domaindrivers.smartschedule.availability.segment.SegmentInMinutes.defaultSegment;

public class AvailabilityFacade {

    private final ResourceAvailabilityRepository availabilityRepository;
    private final ResourceAvailabilityReadModel availabilityReadModel;

    public AvailabilityFacade(ResourceAvailabilityRepository availabilityRepository, ResourceAvailabilityReadModel availabilityReadModel) {
        this.availabilityRepository = availabilityRepository;
        this.availabilityReadModel = availabilityReadModel;
    }

    public void createResourceSlots(ResourceId resourceId, TimeSlot timeslot) {
        ResourceGroupedAvailability groupedAvailability = ResourceGroupedAvailability.of(resourceId, timeslot);
        availabilityRepository.saveNew(groupedAvailability);
    }

    public void createResourceSlots(ResourceId resourceId, ResourceId parentId, TimeSlot timeslot) {
        ResourceGroupedAvailability groupedAvailability = ResourceGroupedAvailability.of(resourceId, timeslot, parentId);
        availabilityRepository.saveNew(groupedAvailability);
    }

    @Transactional
    public boolean block(ResourceId resourceId, TimeSlot timeSlot, Owner requester) {
        ResourceGroupedAvailability toBlock = findGrouped(resourceId, timeSlot);
        return block(requester, toBlock);
    }

    private boolean block(Owner requester, ResourceGroupedAvailability toBlock) {
        boolean result = toBlock.block(requester);
        if (result) {
            return availabilityRepository.saveCheckingVersion(toBlock);
        }
        return result;
    }

    @Transactional
    public boolean release(ResourceId resourceId, TimeSlot timeSlot, Owner requester) {
        ResourceGroupedAvailability toRelease = findGrouped(resourceId, timeSlot);
        boolean result = toRelease.release(requester);
        if (result) {
            return availabilityRepository.saveCheckingVersion(toRelease);
        }
        return result;
    }

    @Transactional
    public boolean disable(ResourceId resourceId, TimeSlot timeSlot, Owner requester) {
        ResourceGroupedAvailability toDisable = findGrouped(resourceId, timeSlot);
        boolean result = toDisable.disable(requester);
        if (result) {
            result = availabilityRepository.saveCheckingVersion(toDisable);
        }
        return result;
    }

    public ResourceGroupedAvailability findGrouped(ResourceId resourceId, TimeSlot within) {
        TimeSlot normalized = Segments.normalizeToSegmentBoundaries(within, defaultSegment());
        return new ResourceGroupedAvailability(availabilityRepository.loadAllWithinSlot(resourceId, normalized));
    }

    Calendar loadCalendar(ResourceId resourceId, TimeSlot within) {
        TimeSlot normalized = Segments.normalizeToSegmentBoundaries(within, defaultSegment());
        return availabilityReadModel.load(resourceId, normalized);
    }

    public Calendars loadCalendars(Set<ResourceId> resources, TimeSlot within) {
        TimeSlot normalized = Segments.normalizeToSegmentBoundaries(within, defaultSegment());
        return availabilityReadModel.loadAll(resources, normalized);
    }

    ResourceGroupedAvailability find(ResourceId resourceId, TimeSlot within) {
        TimeSlot normalized = Segments.normalizeToSegmentBoundaries(within, defaultSegment());
        return new ResourceGroupedAvailability(availabilityRepository.loadAllWithinSlot(resourceId, normalized));
    }

    ResourceGroupedAvailability findByParentId(ResourceId parentId, TimeSlot within) {
        TimeSlot normalized = Segments.normalizeToSegmentBoundaries(within, defaultSegment());
        return new ResourceGroupedAvailability(availabilityRepository.loadAllByParentIdWithinSlot(parentId, normalized));
    }


}


