package domaindrivers.smartschedule.simulation;


import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.*;

class AvailableCapabilitiesBuilder {
    private final List<AvailableResourceCapability> availabilities = new ArrayList<>();
    private UUID currentResourceId;
    private Capability capability;
    private TimeSlot timeSlot;

    AvailableCapabilitiesBuilder withEmployee(UUID id) {
        if (currentResourceId != null) {
            this.availabilities.add(new AvailableResourceCapability(currentResourceId, capability, timeSlot));
        }
        this.currentResourceId = id;
        return this;
    }

    AvailableCapabilitiesBuilder thatBrings(Capability capability) {
        this.capability = capability;
        return this;
    }

    AvailableCapabilitiesBuilder thatIsAvailableAt(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
        return this;
    }

    SimulatedCapabilities build() {
        if (currentResourceId != null) {
            this.availabilities.add(new AvailableResourceCapability(currentResourceId, capability, timeSlot));
        }
        return new SimulatedCapabilities(availabilities);
    }

}
