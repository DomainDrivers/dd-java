package domaindrivers.smartschedule.allocation;

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

    Allocations remove(AllocatedCapability toRemove, TimeSlot slot) {
        return find(toRemove, slot)
                .map(ar -> removeFromSlot(ar, slot))
                .orElse(this);
    }

    private Allocations removeFromSlot(AllocatedCapability allocatedResource, TimeSlot slot) {
        Set<AllocatedCapability> leftOvers = allocatedResource
                .timeSlot()
                .leftoverAfterRemovingCommonWith(slot)
                .stream()
                .filter(leftOver -> leftOver.within(allocatedResource.timeSlot()))
                .map(leftOver -> new AllocatedCapability(allocatedResource.resourceId(), allocatedResource.capability(), leftOver))
                .collect(Collectors.toSet());
        Set<AllocatedCapability> newSlots = new HashSet<>(this.all);
        newSlots.remove(allocatedResource);
        newSlots.addAll(leftOvers);
        return new Allocations(newSlots);
    }

    Optional<AllocatedCapability> find(AllocatedCapability capability, TimeSlot timeSlot) {
        return all.stream()
                .filter(ar -> ar.equals(capability))
                .findFirst();
    }

}
