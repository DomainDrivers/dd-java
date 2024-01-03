package domaindrivers.smartschedule.availability;


import domaindrivers.smartschedule.availability.segment.Segments;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.transaction.Transactional;


import static domaindrivers.smartschedule.availability.segment.SegmentInMinutes.defaultSegment;

public class AvailabilityFacade {

    private final ResourceAvailabilityRepository availabilityRepository;

    public AvailabilityFacade(ResourceAvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    public void createResourceSlots(ResourceAvailabilityId resourceId, TimeSlot timeslot) {
        ResourceGroupedAvailability groupedAvailability = ResourceGroupedAvailability.of(resourceId, timeslot);
        availabilityRepository.saveNew(groupedAvailability);
    }

    public void createResourceSlots(ResourceAvailabilityId resourceId, ResourceAvailabilityId parentId, TimeSlot timeslot) {
        ResourceGroupedAvailability groupedAvailability = ResourceGroupedAvailability.of(resourceId, timeslot, parentId);
        availabilityRepository.saveNew(groupedAvailability);
    }

    @Transactional
    public boolean block(ResourceAvailabilityId resourceId, TimeSlot timeSlot, Owner requester) {
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
    public boolean release(ResourceAvailabilityId resourceId, TimeSlot timeSlot, Owner requester) {
        ResourceGroupedAvailability toRelease = findGrouped(resourceId, timeSlot);
        boolean result = toRelease.release(requester);
        if (result) {
            return availabilityRepository.saveCheckingVersion(toRelease);
        }
        return result;
    }

    @Transactional
    public boolean disable(ResourceAvailabilityId resourceId, TimeSlot timeSlot, Owner requester) {
        ResourceGroupedAvailability toDisable = findGrouped(resourceId, timeSlot);
        boolean result = toDisable.disable(requester);
        if (result) {
            result = availabilityRepository.saveCheckingVersion(toDisable);
        }
        return result;
    }

    private ResourceGroupedAvailability findGrouped(ResourceAvailabilityId resourceId, TimeSlot within) {
        TimeSlot normalized = Segments.normalizeToSegmentBoundaries(within, defaultSegment());
        return new ResourceGroupedAvailability(availabilityRepository.loadAllWithinSlot(resourceId, normalized));
    }

    ResourceGroupedAvailability find(ResourceAvailabilityId resourceId, TimeSlot within) {
        TimeSlot normalized = Segments.normalizeToSegmentBoundaries(within, defaultSegment());
        return new ResourceGroupedAvailability(availabilityRepository.loadAllWithinSlot(resourceId, normalized));
    }

    ResourceGroupedAvailability findByParentId(ResourceAvailabilityId parentId, TimeSlot within) {
        TimeSlot normalized = Segments.normalizeToSegmentBoundaries(within, defaultSegment());
        return new ResourceGroupedAvailability(availabilityRepository.loadAllByParentIdWithinSlot(parentId, normalized));
    }


}


