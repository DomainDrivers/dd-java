package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ReleaseResourceHandlerTest {

    AvailabilityFacade availabilityFacade = mock(AvailabilityFacade.class);
    ReleaseResourceHandler handler = new ReleaseResourceHandler(availabilityFacade, new ProcessedMessages());

    @Test
    void handlesReleaseResourceByReleasingTheResource() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner owner = Owner.newOne();
        ReleaseResource command = new ReleaseResource(resourceId, oneDay, owner, Instant.now());

        //when
        handler.handle(command);

        //then
        verify(availabilityFacade).release(resourceId, oneDay, owner);
    }

    @Test
    void redeliveringTheSameCommandReleasesOnlyOnce() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Owner owner = Owner.newOne();
        ReleaseResource command = new ReleaseResource(resourceId, oneDay, owner, Instant.now());

        //when
        handler.handle(command);
        handler.handle(command);

        //then
        verify(availabilityFacade).release(resourceId, oneDay, owner);
        verifyNoMoreInteractions(availabilityFacade);
    }
}
