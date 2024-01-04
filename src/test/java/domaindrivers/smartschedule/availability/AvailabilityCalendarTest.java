package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.MockedEventPublisherConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.ClockConfiguration;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.temporal.ChronoUnit;
import java.util.Set;

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
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        TimeSlot fifteenMinutes = new TimeSlot(oneDay.from().plus(15, ChronoUnit.MINUTES), oneDay.from().plus(30, ChronoUnit.MINUTES));
        Owner owner = Owner.newOne();
        //and
        availabilityFacade.createResourceSlots(resourceId, oneDay);

        //when
        availabilityFacade.block(resourceId, fifteenMinutes, owner);

        //then
        Calendar calendar = availabilityFacade.loadCalendar(resourceId, oneDay);
        assertThat(calendar.takenBy(owner)).containsExactly(fifteenMinutes);
        assertThat(calendar.availableSlots()).containsExactlyInAnyOrderElementsOf(oneDay.leftoverAfterRemovingCommonWith(fifteenMinutes));
    }

    @Test
    void loadsCalendarForMultipleResources() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        ResourceId resourceId2 = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        TimeSlot fifteenMinutes = new TimeSlot(oneDay.from().plus(15, ChronoUnit.MINUTES), oneDay.from().plus(30, ChronoUnit.MINUTES));

        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, oneDay);
        availabilityFacade.createResourceSlots(resourceId2, oneDay);

        //when
        availabilityFacade.block(resourceId, fifteenMinutes, owner);
        availabilityFacade.block(resourceId2, fifteenMinutes, owner);

        //then
        Calendars calendars = availabilityFacade.loadCalendars(Set.of(resourceId, resourceId2), oneDay);
        assertThat(calendars.get(resourceId).takenBy(owner)).containsExactly(fifteenMinutes);
        assertThat(calendars.get(resourceId2).takenBy(owner)).containsExactly(fifteenMinutes);
        assertThat(calendars.get(resourceId).availableSlots()).containsExactlyInAnyOrderElementsOf(oneDay.leftoverAfterRemovingCommonWith(fifteenMinutes));
        assertThat(calendars.get(resourceId2).availableSlots()).containsExactlyInAnyOrderElementsOf(oneDay.leftoverAfterRemovingCommonWith(fifteenMinutes));
    }


}