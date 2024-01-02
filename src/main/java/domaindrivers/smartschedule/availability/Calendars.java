package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.ResourceName;

import java.util.*;
import java.util.stream.Collectors;

public record Calendars(Map<ResourceName, Calendar> calendars) {

    public static Calendars of(Calendar... calendars) {
        Map<ResourceName, Calendar> collect =
                Arrays.stream(calendars)
                        .collect(Collectors.toMap(Calendar::resourceId, calendar -> calendar));
        return new Calendars(collect);
    }

    public Calendar get(ResourceName resourceId) {
        return calendars.getOrDefault(resourceId, Calendar.empty(resourceId));
    }
}


