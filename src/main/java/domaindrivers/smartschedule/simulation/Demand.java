package domaindrivers.smartschedule.simulation;

import domaindrivers.smartschedule.optimization.WeightDimension;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

public record Demand(Capability capability, TimeSlot slot) implements WeightDimension<AvailableResourceCapability> {

    static Demand demandFor(Capability capability, TimeSlot slot) {
        return new Demand(capability, slot);
    }

    @Override
    public boolean isSatisfiedBy(AvailableResourceCapability availableCapability) {
        return availableCapability.performs(this.capability()) &&
                this.slot().within(availableCapability.timeSlot());
    }
}
