package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TestDbConfiguration.class, AvailabilityConfiguration.class})
@Sql(scripts = "classpath:schema-availability.sql")
class AvailabilityFacadeTest {

    @Autowired
    AvailabilityFacade availabilityFacade;

    @Test
    void canCreateAvailabilitySlots() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);

        //when
        availabilityFacade.createResourceSlots(resourceId, oneDay);

        //then
        TimeSlot entireMonth = TimeSlot.createMonthlyTimeSlotAtUTC(2021, 1);
        Calendar monthlyCalendar = availabilityFacade.loadCalendar(resourceId, entireMonth);
        assertThat(monthlyCalendar).isEqualTo(Calendar.withAvailableSlots(resourceId, oneDay));
    }

    @Test
    void canCreateNewAvailabilitySlotsWithParentId() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        ResourceId resourceId2 = ResourceId.newOne();
        ResourceId parentId = ResourceId.newOne();
        ResourceId differentParentId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);

        //when
        availabilityFacade.createResourceSlots(resourceId, parentId, oneDay);
        availabilityFacade.createResourceSlots(resourceId2, differentParentId, oneDay);

        //then
        assertEquals(96, availabilityFacade.findByParentId(parentId, oneDay).size());
        assertEquals(96, availabilityFacade.findByParentId(differentParentId, oneDay).size());
    }

    @Test
    void canBlockAvailabilities() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, oneDay);

        //when
        boolean result = availabilityFacade.block(resourceId, oneDay, owner);

        //then
        assertTrue(result);
        TimeSlot entireMonth = TimeSlot.createMonthlyTimeSlotAtUTC(2021, 1);
        Calendar monthlyCalendar = availabilityFacade.loadCalendar(resourceId, entireMonth);
        assertThat(monthlyCalendar.availableSlots()).isEmpty();
        assertThat(monthlyCalendar.takenBy(owner)).containsExactly(oneDay);
    }

    @Test
    void canDisableAvailabilities() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, oneDay);

        //when
        boolean result = availabilityFacade.disable(resourceId, oneDay, owner);

        //then
        assertTrue(result);
        ResourceGroupedAvailability resourceAvailabilities = availabilityFacade.find(resourceId, oneDay);
        assertEquals(96, resourceAvailabilities.size());
        assertThat(resourceAvailabilities.isDisabledEntirelyBy(owner)).isTrue();
    }

    @Test
    void cantBlockEvenWhenJustSmallSegmentOfRequestedSlotIsBlocked() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, oneDay);
        //and
        availabilityFacade.block(resourceId, oneDay, owner);
        TimeSlot fifteenMinutes = new TimeSlot(oneDay.from(), oneDay.from().plus(15, ChronoUnit.MINUTES));

        //when
        boolean result = availabilityFacade.block(resourceId, fifteenMinutes, Owner.newOne());

        //then
        assertFalse(result);
        ResourceGroupedAvailability resourceAvailability = availabilityFacade.find(resourceId, oneDay);
        assertThat(resourceAvailability.blockedEntirelyBy(owner)).isTrue();

    }


    @Test
    void canReleaseAvailability() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        TimeSlot fifteenMinutes = new TimeSlot(oneDay.from(), oneDay.from().plus(15, ChronoUnit.MINUTES));
        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, fifteenMinutes);
        //and
        availabilityFacade.block(resourceId, fifteenMinutes, owner);

        //when
        boolean result = availabilityFacade.release(resourceId, oneDay, owner);

        //then
        assertTrue(result);
        ResourceGroupedAvailability resourceAvailability = availabilityFacade.find(resourceId, oneDay);
        assertThat(resourceAvailability.isEntirelyAvailable()).isTrue();
    }

    @Test
    void cantReleaseEvenWhenJustPartOfSlotIsOwnedByTheRequester() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot jan_1 = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        TimeSlot jan_2 = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 2);
        TimeSlot jan_1_2 = new TimeSlot(jan_1.from(), jan_2.to());
        Owner jan1owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, jan_1_2);
        //and
        availabilityFacade.block(resourceId, jan_1, jan1owner);
        //and
        Owner jan2owner = Owner.newOne();
        availabilityFacade.block(resourceId, jan_2, jan2owner);

        //when
        boolean result = availabilityFacade.release(resourceId, jan_1_2, jan1owner);

        //then
        assertFalse(result);
        ResourceGroupedAvailability resourceAvailability = availabilityFacade.find(resourceId, jan_1);
        assertThat(resourceAvailability.blockedEntirelyBy(jan1owner)).isTrue();
    }


    @Test
    void oneSegmentCanBeTakenBySomeoneElseAfterRealising() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        TimeSlot fifteenMinutes = new TimeSlot(oneDay.from(), oneDay.from().plus(15, ChronoUnit.MINUTES));
        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, oneDay);
        //and
        availabilityFacade.block(resourceId, oneDay, owner);
        //and
        availabilityFacade.release(resourceId, fifteenMinutes, owner);

        //when
        Owner newRequester = Owner.newOne();
        boolean result = availabilityFacade.block(resourceId, fifteenMinutes, newRequester);

        //then
        assertTrue(result);
        Calendar dailyCalendar = availabilityFacade.loadCalendar(resourceId, oneDay);
        assertThat(dailyCalendar.availableSlots()).isEmpty();
        assertThat(dailyCalendar.takenBy(owner)).containsExactlyElementsOf(oneDay.leftoverAfterRemovingCommonWith(fifteenMinutes));
        assertThat(dailyCalendar.takenBy(newRequester)).containsExactly(fifteenMinutes);
    }

}