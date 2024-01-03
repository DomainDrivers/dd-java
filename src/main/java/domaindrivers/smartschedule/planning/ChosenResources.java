package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.Set;

record ChosenResources(Set<ResourceId> resources, TimeSlot timeSlot) {
    static ChosenResources none() {
        return new ChosenResources(Set.of(), TimeSlot.empty());
    }
}
