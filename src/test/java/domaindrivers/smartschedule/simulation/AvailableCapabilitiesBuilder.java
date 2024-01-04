package domaindrivers.smartschedule.simulation;


import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.*;

class AvailableCapabilitiesBuilder {
    private final List<AvailableResourceCapability> availabilities = new ArrayList<>();
    private UUID currentResourceId;
    private Set<Capability> capabilities;
    private TimeSlot timeSlot;
    private CapabilitySelector.SelectingPolicy selectingPolicy;

    AvailableCapabilitiesBuilder withEmployee(UUID id) {
        if (currentResourceId != null) {
            this.availabilities.add(new AvailableResourceCapability(currentResourceId, new CapabilitySelector(capabilities, selectingPolicy), timeSlot));
        }
        this.currentResourceId = id;
        return this;
    }

    AvailableCapabilitiesBuilder thatBrings(Capability capability) {
        this.capabilities = Set.of(capability);
        this.selectingPolicy = CapabilitySelector.SelectingPolicy.ONE_OF_ALL;
        return this;
    }

    AvailableCapabilitiesBuilder thatIsAvailableAt(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
        return this;
    }

    SimulatedCapabilities build() {
        if (currentResourceId != null) {
            this.availabilities.add(new AvailableResourceCapability(currentResourceId, new CapabilitySelector(capabilities, selectingPolicy), timeSlot));
        }
        return new SimulatedCapabilities(availabilities);
    }

    public AvailableCapabilitiesBuilder thatBringsSimultaneously(Capability ... skills) {
        this.capabilities = new HashSet<>(Arrays.asList(skills));
        this.selectingPolicy = CapabilitySelector.SelectingPolicy.ALL_SIMULTANEOUSLY;
        return this;
    }
}
