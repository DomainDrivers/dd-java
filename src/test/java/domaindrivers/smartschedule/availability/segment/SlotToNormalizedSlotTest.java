package domaindrivers.smartschedule.availability.segment;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotToNormalizedSlotTest {

    public static final SlotToNormalizedSlot SLOT_TO_NORMALIZED_SLOT = new SlotToNormalizedSlot();
    public static final int FIFTEEN_MINUTES_SEGMENT_DURATION = 15;

    @Test
    void hasNoEffectWhenSlotAlreadyNormalized() {
        //given
        Instant start = Instant.parse("2023-09-09T00:00:00Z");
        Instant end = Instant.parse("2023-09-09T01:00:00Z");
        TimeSlot timeSlot = new TimeSlot(start, end);
        SegmentInMinutes oneHour = SegmentInMinutes.of(60, FIFTEEN_MINUTES_SEGMENT_DURATION);

        //when
        TimeSlot normalized = new SlotToNormalizedSlot().apply(timeSlot, oneHour);

        //then
        assertEquals(timeSlot, normalized);
    }

    @Test
    void normalizedToTheHour() {
        //given
        Instant start = Instant.parse("2023-09-09T00:10:00Z");
        Instant end = Instant.parse("2023-09-09T00:59:00Z");
        TimeSlot timeSlot = new TimeSlot(start, end);
        SegmentInMinutes oneHour = SegmentInMinutes.of(60, FIFTEEN_MINUTES_SEGMENT_DURATION);

        //when
        TimeSlot normalized = SLOT_TO_NORMALIZED_SLOT.apply(timeSlot, oneHour);

        //then
        assertEquals(Instant.parse("2023-09-09T00:00:00Z"), normalized.from());
        assertEquals(Instant.parse("2023-09-09T01:00:00Z"), normalized.to());
    }

    @Test
    void normalizedWhenShortSlotOverlappingTwoSegments() {
        //given
        Instant start = Instant.parse("2023-09-09T00:29:00Z");
        Instant end = Instant.parse("2023-09-09T00:31:00Z");
        TimeSlot timeSlot = new TimeSlot(start, end);
        SegmentInMinutes oneHour = SegmentInMinutes.of(60, FIFTEEN_MINUTES_SEGMENT_DURATION);

        //when
        TimeSlot normalized = SLOT_TO_NORMALIZED_SLOT.apply(timeSlot, oneHour);

        //then
        assertEquals(Instant.parse("2023-09-09T00:00:00Z"), normalized.from());
        assertEquals(Instant.parse("2023-09-09T01:00:00Z"), normalized.to());
    }

    @Test
    void noNormalizationWhenSlotStartsAtSegmentStart() {
        //given
        Instant start = Instant.parse("2023-09-09T00:15:00Z");
        Instant end = Instant.parse("2023-09-09T00:30:00Z");
        TimeSlot timeSlot = new TimeSlot(start, end);
        Instant start2 = Instant.parse("2023-09-09T00:30:00Z");
        Instant end2 = Instant.parse("2023-09-09T00:45:00Z");
        TimeSlot timeSlot2 = new TimeSlot(start2, end2);
        SegmentInMinutes fifteenMinutes = SegmentInMinutes.of(15, FIFTEEN_MINUTES_SEGMENT_DURATION);

        //when
        TimeSlot normalized = SLOT_TO_NORMALIZED_SLOT.apply(timeSlot, fifteenMinutes);
        TimeSlot normalized2 = SLOT_TO_NORMALIZED_SLOT.apply(timeSlot2, fifteenMinutes);

        //then
        assertEquals(Instant.parse("2023-09-09T00:15:00Z"), normalized.from());
        assertEquals(Instant.parse("2023-09-09T00:30:00Z"), normalized.to());
        assertEquals(Instant.parse("2023-09-09T00:30:00Z"), normalized2.from());
        assertEquals(Instant.parse("2023-09-09T00:45:00Z"), normalized2.to());
    }


}