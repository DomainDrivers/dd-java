package domaindrivers.smartschedule.simulation;


import domaindrivers.smartschedule.optimization.CapacityDimension;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.UUID;

record AvailableResourceCapability(UUID resourceId, Capability capability, TimeSlot timeSlot) implements CapacityDimension {

    boolean performs(Capability capability) {
        return capability.equals(this.capability);
    }
}
