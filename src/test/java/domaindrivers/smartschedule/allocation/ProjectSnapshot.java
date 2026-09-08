package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

record ProjectSnapshot(boolean hasTimeSlot, TimeSlot timeSlot, Demands demands, Demands missingDemands,
                        Allocations allocations) {
}
