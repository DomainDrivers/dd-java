package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.shared.ResourceName;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.Set;

record ChosenResources(Set<ResourceName> resources, TimeSlot timeSlot) {
    static ChosenResources none() {
        return new ChosenResources(Set.of(), null);
    }
}
