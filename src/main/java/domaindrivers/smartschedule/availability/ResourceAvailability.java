package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.Objects;

class ResourceAvailability {

    private final ResourceAvailabilityId id;
    private final ResourceAvailabilityId resourceId;
    private final ResourceAvailabilityId resourceParentId;
    private final TimeSlot segment;
    private Blockade blockade;
    private int version = 0;

    ResourceAvailability(ResourceAvailabilityId id, ResourceAvailabilityId resourceId, ResourceAvailabilityId resourceParentId,
                         TimeSlot segment, Blockade blockade, int version) {
        this.id = id;
        this.resourceId = resourceId;
        this.resourceParentId = resourceParentId;
        this.segment = segment;
        this.blockade = blockade;
        this.version = version;
    }


    ResourceAvailability(ResourceAvailabilityId availabilityId, ResourceAvailabilityId resourceId, TimeSlot segment) {
        this.id = availabilityId;
        this.resourceId = resourceId;
        this.segment = segment;
        this.resourceParentId = ResourceAvailabilityId.none();
        this.blockade = Blockade.none();
    }

    ResourceAvailability(ResourceAvailabilityId availabilityId, ResourceAvailabilityId resourceId, ResourceAvailabilityId resourceParentId, TimeSlot segment) {
        this.id = availabilityId;
        this.resourceId = resourceId;
        this.segment = segment;
        this.resourceParentId = resourceParentId;
        this.blockade = Blockade.none();
    }

    ResourceAvailabilityId id() {
        return id;
    }

    boolean block(Owner requester) {
        if (isAvailableFor(requester)) {
            blockade = Blockade.ownedBy(requester);
            return true;
        } else {
            return false;
        }
    }

    boolean release(Owner requester) {
        if (isAvailableFor(requester)) {
            blockade = Blockade.none();
            return true;
        } else {
            return false;
        }
    }

    boolean disable(Owner requester) {
        this.blockade = Blockade.disabledBy(requester);
        return true;
    }

    boolean enable(Owner requester) {
        if (blockade.canBeTakenBy(requester)) {
            this.blockade = Blockade.none();
            return true;
        }
        return false;
    }

    boolean isDisabled() {
        return blockade.disabled();
    }

    private boolean isAvailableFor(Owner requester) {
        return blockade.canBeTakenBy(requester) && !isDisabled();
    }

    int version() {
        return version;
    }

    Owner blockedBy() {
        return blockade.takenBy();
    }

    boolean isDisabledBy(Owner owner) {
        return blockade.isDisabledBy(owner);
    }

    TimeSlot segment() {
        return segment;
    }

    ResourceAvailabilityId resourceId() {
        return resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceAvailability that = (ResourceAvailability) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public ResourceAvailabilityId resourceParentId() {
        return resourceParentId;
    }
}
