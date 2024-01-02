package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.ResourceName;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Calendar(ResourceName resourceId,
                Map<Owner, List<TimeSlot>> calendar) {

    public static Calendar withAvailableSlots(ResourceName resourceId, TimeSlot... availableSlots) {
        return new Calendar(resourceId, Map.of(Owner.none(), List.of(availableSlots)));
    }

    static Calendar empty(ResourceName resourceId) {
        return new Calendar(resourceId, new HashMap<>());
    }

    public List<TimeSlot> availableSlots() {
        return calendar.getOrDefault(Owner.none(), List.of());
    }

}
