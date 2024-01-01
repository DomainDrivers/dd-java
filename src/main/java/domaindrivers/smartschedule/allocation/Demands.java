package domaindrivers.smartschedule.allocation;


import java.util.List;

record Demands(List<Demand> all) {

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
}
