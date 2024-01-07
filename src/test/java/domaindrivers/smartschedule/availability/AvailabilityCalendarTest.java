package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.MockedEventPublisherConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.ClockConfiguration;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static domaindrivers.smartschedule.availability.segment.Segments.DEFAULT_SEGMENT_DURATION_IN_MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TestDbConfiguration.class, MockedEventPublisherConfiguration.class})
@Sql(scripts = "classpath:schema-availability.sql")
public class AvailabilityCalendarTest {

    @Autowired
    AvailabilityFacade availabilityFacade;

    @Test
    void loadsCalendarForEntireMonth() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        Duration durationOfSevenSlots = Duration.ofMinutes(7 * DEFAULT_SEGMENT_DURATION_IN_MINUTES);
        TimeSlot sevenSlots = TimeSlot.createTimeSlotAtUTCOfDuration(2021, 1, 1, durationOfSevenSlots);
        TimeSlot minimumSlot = new TimeSlot(sevenSlots.from(), sevenSlots.from().plus(DEFAULT_SEGMENT_DURATION_IN_MINUTES, ChronoUnit.MINUTES));
        Owner owner = Owner.newOne();
        //and
        availabilityFacade.createResourceSlots(resourceId, sevenSlots);

        //when
        availabilityFacade.block(resourceId, minimumSlot, owner);

        //then
        Calendar calendar = availabilityFacade.loadCalendar(resourceId, sevenSlots);
        assertThat(calendar.takenBy(owner)).containsExactly(minimumSlot);
        assertThat(calendar.availableSlots()).containsExactlyInAnyOrderElementsOf(sevenSlots.leftoverAfterRemovingCommonWith(minimumSlot));
    }

    @Test
    void loadsCalendarForMultipleResources() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        ResourceId resourceId2 = ResourceId.newOne();
        Duration durationOfSevenSlots = Duration.ofMinutes(7 * DEFAULT_SEGMENT_DURATION_IN_MINUTES);
        TimeSlot sevenSlots = TimeSlot.createTimeSlotAtUTCOfDuration(2021, 1, 1, durationOfSevenSlots);
        TimeSlot minimumSlot = new TimeSlot(sevenSlots.from(), sevenSlots.from().plus(DEFAULT_SEGMENT_DURATION_IN_MINUTES, ChronoUnit.MINUTES));

        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, sevenSlots);
        availabilityFacade.createResourceSlots(resourceId2, sevenSlots);

        //when
        availabilityFacade.block(resourceId, minimumSlot, owner);
        availabilityFacade.block(resourceId2, minimumSlot, owner);

        //then
        Calendars calendars = availabilityFacade.loadCalendars(Set.of(resourceId, resourceId2), sevenSlots);
        assertThat(calendars.get(resourceId).takenBy(owner)).containsExactly(minimumSlot);
        assertThat(calendars.get(resourceId2).takenBy(owner)).containsExactly(minimumSlot);
        assertThat(calendars.get(resourceId).availableSlots()).containsExactlyInAnyOrderElementsOf(sevenSlots.leftoverAfterRemovingCommonWith(minimumSlot));
        assertThat(calendars.get(resourceId2).availableSlots()).containsExactlyInAnyOrderElementsOf(sevenSlots.leftoverAfterRemovingCommonWith(minimumSlot));
    }


}