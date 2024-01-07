package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.MockedEventPublisherConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.EventsPublisher;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static domaindrivers.smartschedule.availability.segment.Segments.DEFAULT_SEGMENT_DURATION_IN_MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TestDbConfiguration.class, MockedEventPublisherConfiguration.class})
@Sql(scripts = "classpath:schema-availability.sql")
class AvailabilityFacadeTest {

    @Autowired
    AvailabilityFacade availabilityFacade;

    @Autowired
    EventsPublisher eventsPublisher;

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
        assertThat(availabilityFacade.findByParentId(parentId, oneDay).isEntirelyWithParentId(parentId)).isTrue();
        assertThat(availabilityFacade.findByParentId(differentParentId, oneDay).isEntirelyWithParentId(differentParentId)).isTrue();

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
    void cantBlockWhenNoSlotsCreated() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner owner = Owner.newOne();

        //when
        boolean result = availabilityFacade.block(resourceId, oneDay, owner);

        //then
        assertFalse(result);
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
        assertThat(resourceAvailabilities.isDisabledEntirelyBy(owner)).isTrue();
    }

    @Test
    void cantDisableWhenNoSlotsCreated() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner owner = Owner.newOne();

        //when
        boolean result = availabilityFacade.disable(resourceId, oneDay, owner);

        //then
        assertFalse(result);
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
    void cantReleaseWhenNoSlotsCreated() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner owner = Owner.newOne();

        //when
        boolean result = availabilityFacade.release(resourceId, oneDay, owner);

        //then
        assertFalse(result);
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
        Duration durationOfSevenSlots = Duration.ofMinutes(7 * DEFAULT_SEGMENT_DURATION_IN_MINUTES);
        TimeSlot sevenSlots = TimeSlot.createTimeSlotAtUTCOfDuration(2021, 1, 1, durationOfSevenSlots);
        TimeSlot minimumSlot = new TimeSlot(sevenSlots.from(), sevenSlots.from().plus(DEFAULT_SEGMENT_DURATION_IN_MINUTES, ChronoUnit.MINUTES));
        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, sevenSlots);
        //and
        availabilityFacade.block(resourceId, sevenSlots, owner);
        //and
        availabilityFacade.release(resourceId, minimumSlot, owner);

        //when
        Owner newRequester = Owner.newOne();
        boolean result = availabilityFacade.block(resourceId, minimumSlot, newRequester);

        //then
        assertTrue(result);
        Calendar entireCalendar = availabilityFacade.loadCalendar(resourceId, sevenSlots);
        assertThat(entireCalendar.availableSlots()).isEmpty();
        assertThat(entireCalendar.takenBy(owner)).containsExactlyElementsOf(sevenSlots.leftoverAfterRemovingCommonWith(minimumSlot));
        assertThat(entireCalendar.takenBy(newRequester)).containsExactly(minimumSlot);
    }

    @Test
    void resourceTakenOverEventIsEmittedAfterTakingOverTheResource() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner initialOwner = Owner.newOne();
        Owner newOwner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, oneDay);
        availabilityFacade.block(resourceId, oneDay, initialOwner);

        //when
        boolean result = availabilityFacade.disable(resourceId, oneDay, newOwner);

        //then
        assertTrue(result);
        Mockito.verify(eventsPublisher)
                .publish(Mockito.argThat(takenOver(resourceId, initialOwner, oneDay)));
    }

    ArgumentMatcher<ResourceTakenOver> takenOver(ResourceId resourceId, Owner initialOwner, TimeSlot oneDay) {
        return event ->
                event.resourceId().equals(resourceId) &&
                        event.slot().equals(oneDay) &&
                        event.previousOwners().equals(Set.of(initialOwner)) &&
                        event.occurredAt() != null &&
                        event.eventId() != null;
    }

}