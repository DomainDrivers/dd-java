package domaindrivers.smartschedule.optimization;


import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.UUID;

record CapabilityCapacityDimension(UUID uuid, String id, String capacityName, String capacityType) implements CapacityDimension {

    public CapabilityCapacityDimension(String id, String capacityName, String capacityType) {
        this(UUID.randomUUID(), id, capacityName, capacityType);
    }
}

record CapabilityWeightDimension(String name, String type) implements WeightDimension<CapabilityCapacityDimension> {

    @Override
    public boolean isSatisfiedBy(CapabilityCapacityDimension capacityDimension) {
        return capacityDimension.capacityName().equals(name) &&
                capacityDimension.capacityType().equals(type);
    }
}

record CapabilityTimedCapacityDimension(UUID uuid, String id, String capacityName, String capacityType, TimeSlot timeSlot) implements CapacityDimension {

    public CapabilityTimedCapacityDimension(String id, String capacityName, String capacityType, TimeSlot timeSlot) {
        this(UUID.randomUUID(), id, capacityName, capacityType, timeSlot);
    }
}

record CapabilityTimedWeightDimension(String name, String type, TimeSlot timeSlot) implements WeightDimension<CapabilityTimedCapacityDimension> {

    @Override
    public boolean isSatisfiedBy(CapabilityTimedCapacityDimension capacityTimedDimension) {
        return capacityTimedDimension.capacityName().equals(name) &&
                capacityTimedDimension.capacityType().equals(type) &&
                this.timeSlot.within(capacityTimedDimension.timeSlot());
    }
}

