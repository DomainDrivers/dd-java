package domaindrivers.smartschedule.planning.schedule;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

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
    public void twoSlotsHaveCommonPartWhenSlotsOverlap() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2022-01-01T00:00:00Z"), Instant.parse("2022-01-15T00:00:00Z"));
        TimeSlot slot2 = new TimeSlot(Instant.parse("2022-01-10T00:00:00Z"), Instant.parse("2022-01-20T00:00:00Z"));

        //when
        TimeSlot common = slot1.commonPartWith(slot2);

        //then
        assertFalse(common.isEmpty());
        assertEquals(Instant.parse("2022-01-10T00:00:00Z"), common.from());
        assertEquals(Instant.parse("2022-01-15T00:00:00Z"), common.to());
    }

    @Test
    public void twoSlotsHaveCommonPartWhenFullOverlap() {
        //given
        TimeSlot slot1 = new TimeSlot(Instant.parse("2022-01-10T00:00:00Z"), Instant.parse("2022-01-20T00:00:00Z"));
        TimeSlot slot2 = new TimeSlot(Instant.parse("2022-01-10T00:00:00Z"), Instant.parse("2022-01-20T00:00:00Z"));

        //when
        TimeSlot common = slot1.commonPartWith(slot2);

        //then
        assertFalse(common.isEmpty());
        assertEquals(slot1, common);
    }

    @Test
    public void stretchTimeSlot() {
        // Arrange
        Instant initialFrom = Instant.parse("2022-01-01T10:00:00Z");
        Instant initialTo = Instant.parse("2022-01-01T12:00:00Z");
        TimeSlot timeSlot = new TimeSlot(initialFrom, initialTo);

        // Act
        TimeSlot stretchedSlot = timeSlot.stretch(Duration.ofHours(1));

        // Assert
        assertEquals(Instant.parse("2022-01-01T09:00:00Z"), stretchedSlot.from());
        assertEquals(Instant.parse("2022-01-01T13:00:00Z"), stretchedSlot.to());
    }

}