package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableResourceId;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.shared.capability.Capability;

import java.util.Objects;
import java.util.UUID;

record AllocatedCapability(AllocatableCapabilityId allocatedCapabilityID, CapabilitySelector capability, TimeSlot timeSlot) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllocatedCapability that = (AllocatedCapability) o;
        return Objects.equals(allocatedCapabilityID, that.allocatedCapabilityID) && Objects.equals(capability, that.capability) && Objects.equals(timeSlot, that.timeSlot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allocatedCapabilityID, capability, timeSlot);
    }
}
