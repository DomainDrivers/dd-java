package domaindrivers.smartschedule.availability.segment;


public record SegmentInMinutes(int value) {

    public static SegmentInMinutes of(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("SegmentInMinutesDuration must be positive");
        }
        if (minutes % Segments.DEFAULT_SEGMENT_DURATION_IN_MINUTES != 0) {
            throw new IllegalArgumentException("SegmentInMinutesDuration must be a multiple of 15");
        }
        return new SegmentInMinutes(minutes);
    }

    public static SegmentInMinutes defaultSegment() {
        return of(Segments.DEFAULT_SEGMENT_DURATION_IN_MINUTES);
    }
}
