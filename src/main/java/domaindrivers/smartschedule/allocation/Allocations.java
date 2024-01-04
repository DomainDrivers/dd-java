package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

record Allocations(Set<AllocatedCapability> all) {

    static Allocations none() {
        return new Allocations(Set.of());
    }

    Allocations add(AllocatedCapability newOne) {
        Set<AllocatedCapability> all = new HashSet<>(this.all);
        all.add(newOne);
        return new Allocations(all);
    }

    Allocations remove(AllocatableCapabilityId toRemove, TimeSlot slot) {
        return find(toRemove)
                .map(ar -> removeFromSlot(ar, slot))
                .orElse(this);
    }
    private Allocations removeFromSlot(AllocatedCapability allocatedCapability, TimeSlot slot) {
        Set<AllocatedCapability> leftOvers = allocatedCapability
                .timeSlot()
                .leftoverAfterRemovingCommonWith(slot)
                .stream()
                .filter(leftOver -> leftOver.within(allocatedCapability.timeSlot()))
                .map(leftOver -> new AllocatedCapability(allocatedCapability.allocatedCapabilityID(), allocatedCapability.capability(), leftOver))
                .collect(Collectors.toSet());
        Set<AllocatedCapability> newSlots = new HashSet<>(this.all);
        newSlots.remove(allocatedCapability);
        newSlots.addAll(leftOvers);
        return new Allocations(newSlots);
    }

    Optional<AllocatedCapability> find(AllocatableCapabilityId allocatedCapabilityId) {
        return all.stream()
                .filter(ar -> ar.allocatedCapabilityID().equals(allocatedCapabilityId))
                .findFirst();
    }

}
