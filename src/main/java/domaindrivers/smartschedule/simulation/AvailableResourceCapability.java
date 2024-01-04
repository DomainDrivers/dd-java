package domaindrivers.smartschedule.simulation;


import domaindrivers.smartschedule.optimization.CapacityDimension;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.Set;
import java.util.UUID;

record AvailableResourceCapability(UUID resourceId, CapabilitySelector capabilitySelector, TimeSlot timeSlot) implements CapacityDimension {

    AvailableResourceCapability(UUID resourceId, Capability capability, TimeSlot timeSlot) {
       this(resourceId, CapabilitySelector.canJustPerform(capability), timeSlot);
    }
    boolean performs(Capability capability) {
        return capabilitySelector.canPerform(Set.of(capability));
    }
}
