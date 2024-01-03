package domaindrivers.smartschedule.availability.segment;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;

class SlotToNormalizedSlot implements BiFunction<TimeSlot, SegmentInMinutes, TimeSlot> {

    @Override
    public TimeSlot apply(TimeSlot timeSlot, SegmentInMinutes segmentInMinutes) {

        int segmentInMinutesDuration = segmentInMinutes.value();
        Instant segmentStart = normalizeStart(timeSlot.from(), segmentInMinutesDuration);
        Instant segmentEnd = normalizeEnd(timeSlot.to(), segmentInMinutesDuration);
        TimeSlot normalized = new TimeSlot(segmentStart, segmentEnd);
        TimeSlot minimalSegment = new TimeSlot(segmentStart, segmentStart.plus(segmentInMinutes.value(), ChronoUnit.MINUTES));
        if (normalized.within(minimalSegment)) {
            return minimalSegment;
        }
        return normalized;
    }

    private Instant normalizeEnd(Instant initialEnd, int segmentInMinutesDuration) {
        Instant closestSegmentEnd = initialEnd.truncatedTo(ChronoUnit.HOURS);
        while (initialEnd.isAfter(closestSegmentEnd)) {
            closestSegmentEnd = closestSegmentEnd.plus(segmentInMinutesDuration, ChronoUnit.MINUTES);
        }
        return closestSegmentEnd;
    }

    private Instant normalizeStart(Instant initialStart, int segmentInMinutesDuration) {
        Instant closestSegmentStart = initialStart.truncatedTo(ChronoUnit.HOURS);
        if (closestSegmentStart.plus(segmentInMinutesDuration, ChronoUnit.MINUTES).isAfter(initialStart)) {
            return closestSegmentStart;
        }
        while (closestSegmentStart.isBefore(initialStart)) {
            closestSegmentStart = closestSegmentStart.plus(segmentInMinutesDuration, ChronoUnit.MINUTES);
        }
        return closestSegmentStart;
    }
}
