package domaindrivers.smartschedule.availability;

import java.util.*;
import java.util.stream.Collectors;

public record Calendars(Map<ResourceId, Calendar> calendars) {

    public static Calendars of(Calendar... calendars) {
        Map<ResourceId, Calendar> collect =
                Arrays.stream(calendars)
                        .collect(Collectors.toMap(Calendar::resourceId, calendar -> calendar));
        return new Calendars(collect);
    }

    public Calendar get(ResourceId resourceId) {
        return calendars.getOrDefault(resourceId, Calendar.empty(resourceId));
    }
}


