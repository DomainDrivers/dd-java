package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceAvailabilityTest {

    ResourceAvailabilityId resourceAvailability = ResourceAvailabilityId.newOne();
    Owner OWNER_ONE = Owner.newOne();
    Owner OWNER_TWO = Owner.newOne();

    @Test
    void canBeBlockedWhenIsAvailable() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();

        //when
        boolean result = resourceAvailability.block(OWNER_ONE);

        //then
        assertTrue(result);
    }

    @Test
    void cantBeBlockedWhenAlreadyBlockedBySomeoneElse() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();
        //and
        resourceAvailability.block(OWNER_ONE);

        //when
        boolean result = resourceAvailability.block(OWNER_TWO);

        //then
        assertFalse(result);
    }

    @Test
    void canBeReleasedOnlyByInitialOwner() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();
        //and
        resourceAvailability.block(OWNER_ONE);

        //when
        boolean result = resourceAvailability.release(OWNER_ONE);

        //then
        assertTrue(result);
    }

    @Test
    void cantBeReleaseBySomeoneElse() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();
        //and
        resourceAvailability.block(OWNER_ONE);

        //when
        boolean result = resourceAvailability.release(OWNER_TWO);

        //then
        assertFalse(result);
    }

    @Test
    void canBeBlockedBySomeoneElseAfterReleasing() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();
        //and
        resourceAvailability.block(OWNER_ONE);
        //and
        resourceAvailability.release(OWNER_ONE);

        //when
        boolean result = resourceAvailability.release(OWNER_TWO);

        //then
        assertTrue(result);
    }

    @Test
    void canDisableWhenAvailable() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();

        //and
        boolean result = resourceAvailability.disable(OWNER_ONE);

        //then
        assertTrue(result);
        assertTrue(resourceAvailability.isDisabled());
        assertTrue(resourceAvailability.isDisabledBy(OWNER_ONE));
    }

    @Test
    void canDisableWhenBlocked() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();

        //and
        boolean resultBlocking = resourceAvailability.block(OWNER_ONE);

        //when
        boolean resultDisabling = resourceAvailability.disable(OWNER_TWO);

        //then
        assertTrue(resultBlocking);
        assertTrue(resultDisabling);
        assertTrue(resourceAvailability.isDisabled());
        assertTrue(resourceAvailability.isDisabledBy(OWNER_TWO));
    }

    @Test
    void cantBeBlockedWhileDisabled() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();

        //and
        boolean resultDisabling = resourceAvailability.disable(OWNER_ONE);

        //when
        boolean resultBlocking = resourceAvailability.block(OWNER_TWO);
        boolean resultBlockingBySameOwner = resourceAvailability.block(OWNER_ONE);

        //then
        assertTrue(resultDisabling);
        assertFalse(resultBlocking);
        assertFalse(resultBlockingBySameOwner);
        assertTrue(resourceAvailability.isDisabled());
        assertTrue(resourceAvailability.isDisabledBy(OWNER_ONE));
    }

    @Test
    void canBeEnabledByInitialRequester() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();

        //and
        resourceAvailability.disable(OWNER_ONE);

        //and
        boolean result = resourceAvailability.enable(OWNER_ONE);

        //then
        assertTrue(result);
        assertFalse(resourceAvailability.isDisabled());
        assertFalse(resourceAvailability.isDisabledBy(OWNER_ONE));
    }

    @Test
    void cantBeEnabledByAnotherRequester() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();

        //and
        resourceAvailability.disable(OWNER_ONE);

        //and
        boolean result = resourceAvailability.enable(OWNER_TWO);

        //then
        assertFalse(result);
        assertTrue(resourceAvailability.isDisabled());
        assertTrue(resourceAvailability.isDisabledBy(OWNER_ONE));
    }

    @Test
    void canBeBlockedAgainAfterEnabling() {
        //given
        ResourceAvailability resourceAvailability = resourceAvailability();

        //and
        resourceAvailability.disable(OWNER_ONE);

        //and
        resourceAvailability.enable(OWNER_ONE);

        //when
        boolean result = resourceAvailability.block(OWNER_TWO);

        //then
        assertTrue(result);
    }

    ResourceAvailability resourceAvailability() {
        return new ResourceAvailability(resourceAvailability, ResourceAvailabilityId.newOne(), TimeSlot.createDailyTimeSlotAtUTC(2000, 1, 1));
    }

}