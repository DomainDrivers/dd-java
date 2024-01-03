package domaindrivers.smartschedule.availability.segment;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

class SlotToSegments implements BiFunction<TimeSlot, SegmentInMinutes, List<TimeSlot>> {

    @Override
    public List<TimeSlot> apply(TimeSlot timeSlot, SegmentInMinutes duration) {
        TimeSlot minimalSegment = new TimeSlot(timeSlot.from(), timeSlot.from().plus(duration.value(), ChronoUnit.MINUTES));
        if (timeSlot.within(minimalSegment)) {
            return List.of(minimalSegment);
        }
        int segmentInMinutesDuration = duration.value();
        long numberOfSegments = calculateNumberOfSegments(timeSlot, segmentInMinutesDuration);
        return Stream
                .iterate(timeSlot.from(), currentStart -> currentStart.plus(segmentInMinutesDuration, ChronoUnit.MINUTES))
                .limit(numberOfSegments)
                .map(currentStart -> new TimeSlot(currentStart, calculateEnd(segmentInMinutesDuration, currentStart, timeSlot.to())))
                .toList();
    }

    private long calculateNumberOfSegments(TimeSlot timeSlot, int segmentInMinutesDuration) {
        return (long) Math.ceil((double) Duration.between(timeSlot.from(), timeSlot.to()).toMinutes() / segmentInMinutesDuration);
    }

    private Instant calculateEnd(int segmentInMinutesDuration, Instant currentStart, Instant initialEnd) {
        Instant segmentEnd = currentStart.plus(segmentInMinutesDuration, ChronoUnit.MINUTES);
        if (initialEnd.isBefore(segmentEnd)) {
            return initialEnd;
        }
        return segmentEnd;
    }


}

