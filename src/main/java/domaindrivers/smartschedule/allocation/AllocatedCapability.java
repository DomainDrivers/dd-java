package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.shared.capability.Capability;

import java.util.Objects;
import java.util.UUID;

record AllocatedCapability(UUID allocatedCapabilityID, UUID resourceId, Capability capability, TimeSlot timeSlot) {

    AllocatedCapability(UUID resourceId, Capability capability, TimeSlot forSlot) {
        this(UUID.randomUUID(), resourceId, capability, forSlot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllocatedCapability that = (AllocatedCapability) o;
        return Objects.equals(resourceId, that.resourceId) && Objects.equals(capability, that.capability) && Objects.equals(timeSlot, that.timeSlot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, capability, timeSlot);
    }
}
