package domaindrivers.smartschedule.availability.segment;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.List;

public class Segments {

    public static final int DEFAULT_SEGMENT_DURATION_IN_MINUTES = 15;

    public static List<TimeSlot> split(TimeSlot timeSlot, SegmentInMinutes unit) {
        TimeSlot normalizedSlot = normalizeToSegmentBoundaries(timeSlot, unit);
        return new SlotToSegments().apply(normalizedSlot, unit);
    }

    public static TimeSlot normalizeToSegmentBoundaries(TimeSlot timeSlot, SegmentInMinutes unit) {
        return new SlotToNormalizedSlot().apply(timeSlot, unit);
    }
}
