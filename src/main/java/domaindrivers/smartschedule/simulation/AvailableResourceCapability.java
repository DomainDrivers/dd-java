package domaindrivers.smartschedule.simulation;


import java.util.UUID;

record AvailableResourceCapability(UUID resourceId, Capability capability, TimeSlot timeSlot) {

    boolean performs(Capability capability) {
        return capability.equals(this.capability);
    }
}
