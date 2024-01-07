package domaindrivers.smartschedule.availability.segment;


public record SegmentInMinutes(int value) {

    public static SegmentInMinutes of(int minutes, int slotDurationInMinutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("SegmentInMinutesDuration must be positive");
        }
        if (minutes < slotDurationInMinutes) {
            throw new IllegalArgumentException("SegmentInMinutesDuration must be at least " + slotDurationInMinutes + " minutes");
        }
        if (minutes % slotDurationInMinutes != 0) {
            throw new IllegalArgumentException("SegmentInMinutesDuration must be a multiple of " + slotDurationInMinutes + " minutes");
        }
        return new SegmentInMinutes(minutes);
    }

    public static SegmentInMinutes of(int minutes) {
        return of(minutes, Segments.DEFAULT_SEGMENT_DURATION_IN_MINUTES);
    }

    public static SegmentInMinutes defaultSegment() {
        return of(Segments.DEFAULT_SEGMENT_DURATION_IN_MINUTES);
    }
}
