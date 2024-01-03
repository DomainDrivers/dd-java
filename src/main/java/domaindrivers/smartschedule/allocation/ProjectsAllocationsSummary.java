package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ProjectsAllocationsSummary(
        Map<ProjectAllocationsId, TimeSlot> timeSlots,
        Map<ProjectAllocationsId, Allocations> projectAllocations,
        Map<ProjectAllocationsId, Demands> demands) {

    static ProjectsAllocationsSummary of(List<ProjectAllocations> allProjectAllocations) {
        Map<ProjectAllocationsId, TimeSlot> timeSlots =
                allProjectAllocations
                        .stream()
                        .filter(ProjectAllocations::hasTimeSlot)
                        .collect(Collectors.toMap(
                ProjectAllocations::id,
                ProjectAllocations::timeSlot));
        Map<ProjectAllocationsId, Allocations> allocations =
                allProjectAllocations
                        .stream()
                        .collect(Collectors.toMap(
                                ProjectAllocations::id,
                                ProjectAllocations::allocations
                        ));
        Map<ProjectAllocationsId, Demands> demands =
                allProjectAllocations
                        .stream()
                        .collect(Collectors.toMap(
                                ProjectAllocations::id,
                                ProjectAllocations::demands));
        return new ProjectsAllocationsSummary(timeSlots, allocations, demands);
    }
}
