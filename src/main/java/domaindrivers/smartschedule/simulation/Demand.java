package domaindrivers.smartschedule.simulation;

record Demand(Capability capability, TimeSlot slot) {

    static Demand demandFor(Capability capability, TimeSlot slot) {
        return new Demand(capability, slot);
    }

    boolean isSatisfiedBy(AvailableResourceCapability availableCapability) {
        return availableCapability.performs(this.capability()) &&
                this.slot().within(availableCapability.timeSlot());
    }
}
