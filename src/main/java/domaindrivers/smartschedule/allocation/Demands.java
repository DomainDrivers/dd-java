package domaindrivers.smartschedule.allocation;


import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

record Demands(List<Demand> all) {

    static Demands none() {
        return new Demands(List.of());
    }

    static Demands of(Demand ... demands) {
        return new Demands(List.of(demands));
    }

    static Demands allInSameTimeSlot(TimeSlot slot, Capability ... capabilities) {
        return new Demands(Stream
                .of(capabilities)
                .map(c -> new Demand(c, slot))
                .toList());
    }

    Demands missingDemands(Allocations allocations) {
        return new Demands(all
                .stream()
                .filter(d -> !satisfiedBy(d, allocations))
                .toList());
    }

    boolean satisfiedBy(Demand d, Allocations allocations) {
        return allocations
                .all()
                .stream()
                .anyMatch(ar -> ar.capability().equals(d.capability()) && d.slot().within(ar.timeSlot()));

    }

    Demands withNew(Demands newDemands) {
        List<Demand> all = new ArrayList<>(this.all);
        all.addAll(newDemands.all);
        return new Demands(all);
    }
}
