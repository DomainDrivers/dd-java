package domaindrivers.smartschedule.planning.schedule;

import domaindrivers.smartschedule.planning.parallelization.ResourceName;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.*;
import java.util.stream.Collectors;

//those classes will be part of another module - possibly "availability"
record Calendars(Map<ResourceName, Calendar> calendars) {

    static Calendars of(Calendar... calendars) {
        Map<ResourceName, Calendar> collect =
                Arrays.stream(calendars)
                        .collect(Collectors.toMap(Calendar::resourceId, calendar -> calendar));
        return new Calendars(collect);
    }

    Calendar get(ResourceName resourceId) {
        return calendars.getOrDefault(resourceId, Calendar.empty(resourceId));
    }
}


record Calendar(ResourceName resourceId,
                       Map<Owner, List<TimeSlot>> calendar) {

    static Calendar withAvailableSlots(ResourceName resourceId, TimeSlot... availableSlots) {
        return new Calendar(resourceId, Map.of(Owner.none(), List.of(availableSlots)));
    }

    static Calendar empty(ResourceName resourceId) {
        return new Calendar(resourceId, new HashMap<>());
    }

    List<TimeSlot> availableSlots() {
        return calendar.getOrDefault(Owner.none(), List.of());
    }

}

record Owner(UUID owner) {

    static Owner none() {
        return new Owner(null);
    }

}