package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class AvailabilityFacadeTest {

    AvailabilityFacade availabilityFacade = new AvailabilityFacade();

    @Test
    void canCreateAvailabilitySlots() {
        //given
        ResourceAvailabilityId resourceId = ResourceAvailabilityId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);

        //when
        availabilityFacade.createResourceSlots(resourceId, oneDay);

        //then
        //todo check that availability(ies) was/were created

    }

    @Test
    void canBlockAvailabilities() {
        //given
        ResourceAvailabilityId resourceId = ResourceAvailabilityId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, oneDay);

        //when
        boolean result = availabilityFacade.block(resourceId, oneDay, owner);

        //then
        assertTrue(result);
        //todo check that can't be taken
    }

    @Test
    void canDisableAvailabilities() {
        //given
        ResourceAvailabilityId resourceId = ResourceAvailabilityId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner owner = Owner.newOne();
        availabilityFacade.createResourceSlots(resourceId, oneDay);

        //when
        boolean result = availabilityFacade.disable(resourceId, oneDay, owner);

        //then
        assertTrue(result);
        //todo check that are disabled
    }

    @Test
    void cantBlockEvenWhenJustSmallSegmentOfRequestedSlotIsBlocked() {
        //given
        ResourceAvailabilityId resourceId = ResourceAvailabilityId.newOne();
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
        //todo check that nothing was changed
    }


    @Test
    void canReleaseAvailability() {
        //given
        ResourceAvailabilityId resourceId = ResourceAvailabilityId.newOne();
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
        //todo check can be taken again
    }

    @Test
    void cantReleaseEvenWhenJustPartOfSlotIsOwnedByTheRequester() {
        //given
        ResourceAvailabilityId resourceId = ResourceAvailabilityId.newOne();
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
        //todo check still owned by jan1
    }


}