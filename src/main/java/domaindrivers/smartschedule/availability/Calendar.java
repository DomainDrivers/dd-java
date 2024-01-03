package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Calendar(ResourceId resourceId,
                Map<Owner, List<TimeSlot>> calendar) {

    public static Calendar withAvailableSlots(ResourceId resourceId, TimeSlot... availableSlots) {
        return new Calendar(resourceId, Map.of(Owner.none(), List.of(availableSlots)));
    }

    static Calendar empty(ResourceId resourceId) {
        return new Calendar(resourceId, new HashMap<>());
    }

    public List<TimeSlot> availableSlots() {
        return calendar.getOrDefault(Owner.none(), List.of());
    }

    public List<TimeSlot> takenBy(Owner requester) {
        return calendar.getOrDefault(requester, List.of());
    }

}
