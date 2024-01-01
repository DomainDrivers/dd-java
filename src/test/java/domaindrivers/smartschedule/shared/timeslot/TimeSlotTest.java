package domaindrivers.smartschedule.shared.timeslot;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

    @Test
    public void creatingMonthlyTimeSlotAtUTC() {
        //when
        TimeSlot january2023 = TimeSlot.createMonthlyTimeSlotAtUTC(2023, 1);

        //then
        assertEquals(january2023.from(), LocalDate.of(2023, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        assertEquals(january2023.to(), LocalDate.of(2023, 2, 1).atTime(0, 0, 0).atZone(ZoneId.of("UTC")).toInstant());
    }

    @Test
    public void creatingDailyTimeSlotAtUTC() {
        //when
        TimeSlot specificDay = TimeSlot.createDailyTimeSlotAtUTC(2023, 1, 15);

        //then
        assertEquals(specificDay.from(), LocalDate.of(2023, 1, 15).atStartOfDay(ZoneId.of("UTC")).toInstant());
        assertEquals(specificDay.to(), LocalDate.of(2023, 1, 16).atTime(0, 0, 0).atZone(ZoneId.of("UTC")).toInstant());
    }

    @Test
    public void oneSlotWithinAnother() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2023-01-02T00:00:00Z"), Instant.parse("2023-01-02T23:59:59Z"));
        TimeSlot slot2 = new TimeSlot(Instant.parse("2023-01-01T00:00:00Z"), Instant.parse("2023-01-03T00:00:00Z"));
        //expect
        assertTrue(slot1.within(slot2));
        assertFalse(slot2.within(slot1));
    }

    @Test
    public void oneSlotIsNotWithinAnotherIfTheyJustOverlap() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2023-01-01T00:00:00Z"), Instant.parse("2023-01-02T23:59:59Z"));
        TimeSlot slot2 = new TimeSlot(Instant.parse("2023-01-02T00:00:00Z"), Instant.parse("2023-01-03T00:00:00Z"));

        //expect
        assertFalse(slot1.within(slot2));
        assertFalse(slot2.within(slot1));

        //given
        TimeSlot slot3 = new TimeSlot(Instant.parse("2023-01-02T00:00:00Z"), Instant.parse("2023-01-03T23:59:59Z"));
        TimeSlot slot4 = new TimeSlot(Instant.parse("2023-01-01T00:00:00Z"), Instant.parse("2023-01-02T23:59:59Z"));

        //expect
        assertFalse(slot3.within(slot4));
        assertFalse(slot4.within(slot3));
    }

    @Test
    void slotIsNotWithinAnotherWhenTheyAreCompletelyOutside() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2023-01-01T00:00:00Z"), Instant.parse("2023-01-01T23:59:59Z"));
        TimeSlot slot2 = new TimeSlot(Instant.parse("2023-01-02T00:00:00Z"), Instant.parse("2023-01-03T00:00:00Z"));

        //expect
        assertFalse(slot1.within(slot2));
    }

    @Test
    void slotIsWithinItself() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2023-01-01T00:00:00Z"), Instant.parse("2023-01-01T23:59:59Z"));

        //expect
        assertTrue(slot1.within(slot1));
    }

    @Test
    void slotsOverlapping() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2022-01-01T00:00:00Z"), Instant.parse("2022-01-10T00:00:00Z"));
        TimeSlot slot2 = new TimeSlot(Instant.parse("2022-01-05T00:00:00Z"), Instant.parse("2022-01-15T00:00:00Z"));
        TimeSlot slot3 = new TimeSlot(Instant.parse("2022-01-10T00:00:00Z"), Instant.parse("2022-01-20T00:00:00Z"));
        TimeSlot slot4 = new TimeSlot(Instant.parse("2022-01-05T00:00:00Z"), Instant.parse("2022-01-10T00:00:00Z"));
        TimeSlot slot5 = new TimeSlot(Instant.parse("2022-01-01T00:00:00Z"), Instant.parse("2022-01-10T00:00:00Z"));

        //expect
        assertTrue(slot1.overlapsWith(slot2));
        assertTrue(slot1.overlapsWith(slot1));
        assertTrue(slot1.overlapsWith(slot3));
        assertTrue(slot1.overlapsWith(slot4));
        assertTrue(slot1.overlapsWith(slot5));
    }

    @Test
    void slotsNotOverlapping() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2022-01-01T00:00:00Z"), Instant.parse("2022-01-10T00:00:00Z"));
        TimeSlot slot2 = new TimeSlot(Instant.parse("2022-01-10T01:00:00Z"), Instant.parse("2022-01-20T00:00:00Z"));
        TimeSlot slot3 = new TimeSlot(Instant.parse("2022-01-11T00:00:00Z"), Instant.parse("2022-01-20T00:00:00Z"));

        //expect
        assertFalse(slot1.overlapsWith(slot2));
        assertFalse(slot1.overlapsWith(slot3));
    }

    @Test
    public void removingCommonPartsShouldHaveNoEffectWhenThereIsNoOverlap() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2022-01-01T00:00:00Z"), Instant.parse("2022-01-10T00:00:00Z"));
        TimeSlot slot2 = new TimeSlot(Instant.parse("2022-01-15T00:00:00Z"), Instant.parse("2022-01-20T00:00:00Z"));

        //expect
        assertTrue(slot1.leftoverAfterRemovingCommonWith(slot2).containsAll(List.of(slot1, slot2)));
    }

    @Test
    public void removingCommonPartsWhenThereIsFullOverlap() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2022-01-01T00:00:00Z"), Instant.parse("2022-01-10T00:00:00Z"));

        //expect
        assertTrue(slot1.leftoverAfterRemovingCommonWith(slot1).isEmpty());
    }

    @Test
    public void removingCommonPartsWhenThereIsOverlap() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2022-01-01T00:00:00Z"), Instant.parse("2022-01-15T00:00:00Z"));
        TimeSlot slot2 = new TimeSlot(Instant.parse("2022-01-10T00:00:00Z"), Instant.parse("2022-01-20T00:00:00Z"));

        //when
        List<TimeSlot> difference = slot1.leftoverAfterRemovingCommonWith(slot2);

        //then
        assertEquals(2, difference.size());
        assertEquals(Instant.parse("2022-01-01T00:00:00Z"), difference.get(0).from());
        assertEquals(Instant.parse("2022-01-10T00:00:00Z"), difference.get(0).to());
        assertEquals(Instant.parse("2022-01-15T00:00:00Z"), difference.get(1).from());
        assertEquals(Instant.parse("2022-01-20T00:00:00Z"), difference.get(1).to());

        //given
        TimeSlot slot3 = new TimeSlot(Instant.parse("2022-01-05T00:00:00Z"), Instant.parse("2022-01-20T00:00:00Z"));
        TimeSlot slot4 = new TimeSlot(Instant.parse("2022-01-01T00:00:00Z"), Instant.parse("2022-01-10T00:00:00Z"));

        //when
        List<TimeSlot> difference2 = slot3.leftoverAfterRemovingCommonWith(slot4);

        //then
        assertEquals(2, difference2.size());
        assertEquals(Instant.parse("2022-01-01T00:00:00Z"), difference2.get(0).from());
        assertEquals(Instant.parse("2022-01-05T00:00:00Z"), difference2.get(0).to());
        assertEquals(Instant.parse("2022-01-10T00:00:00Z"), difference2.get(1).from());
        assertEquals(Instant.parse("2022-01-20T00:00:00Z"), difference2.get(1).to());
    }

    @Test
    public void removingCommonPartWhenOneSlotInFullyWithinAnother() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2022-01-01T00:00:00Z"), Instant.parse("2022-01-20T00:00:00Z"));
        TimeSlot slot2 = new TimeSlot(Instant.parse("2022-01-10T00:00:00Z"), Instant.parse("2022-01-15T00:00:00Z"));

        //when
        List<TimeSlot> difference = slot1.leftoverAfterRemovingCommonWith(slot2);

        //then
        assertEquals(2, difference.size());
        assertEquals(Instant.parse("2022-01-01T00:00:00Z"), difference.get(0).from());
        assertEquals(Instant.parse("2022-01-10T00:00:00Z"), difference.get(0).to());
        assertEquals(Instant.parse("2022-01-15T00:00:00Z"), difference.get(1).from());
        assertEquals(Instant.parse("2022-01-20T00:00:00Z"), difference.get(1).to());
    }

}