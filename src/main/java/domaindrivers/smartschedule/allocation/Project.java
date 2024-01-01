package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.math.BigDecimal;
import java.util.Optional;

class Project {

    private BigDecimal earnings;
    private Demands demands;
    private Allocations allocations;

    Project(Demands demands, BigDecimal earnings) {
        this.demands = demands;
        this.earnings = earnings;
        this.allocations = Allocations.none();
    }

    Demands missingDemands() {
        return demands.missingDemands(allocations);
    }

    BigDecimal earnings() {
        return earnings;
    }

    Optional<AllocatedCapability> remove(AllocatedCapability capability, TimeSlot forSlot) {
        Optional<AllocatedCapability> toRemove = allocations.find(capability, forSlot);
        if (toRemove.isEmpty()) {
            return Optional.empty();
        }
        this.allocations = allocations.remove(capability, forSlot);
        return toRemove;
    }

    Allocations add(AllocatedCapability allocatedCapability) {
        this.allocations = allocations.add(allocatedCapability);
        return allocations;
    }
}
