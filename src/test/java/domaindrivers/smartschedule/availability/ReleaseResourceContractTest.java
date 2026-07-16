package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import io.eventdriven.strictland.Json;
import io.eventdriven.strictland.MessageContract;
import io.eventdriven.strictland.MessageSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

class ReleaseResourceContractTest {

    static final UUID FIXED_EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final ResourceId FIXED_RESOURCE_ID = ResourceId.of("00000000-0000-0000-0000-000000000002");
    static final Owner FIXED_OWNER = Owner.of(UUID.fromString("00000000-0000-0000-0000-000000000003"));
    static final TimeSlot FIXED_TIME_SLOT = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
    static final Instant FIXED_OCCURRED_AT = Instant.parse("2021-01-01T00:00:00Z");

    @Test
    void releaseResourceShapeIsPinned() {
        MessageContract.specification(Json.Jackson.defaults())
                .given(new ReleaseResource(FIXED_EVENT_ID, FIXED_RESOURCE_ID, FIXED_TIME_SLOT, FIXED_OWNER, FIXED_OCCURRED_AT))
                .whenSerializedAs(MessageSnapshot.ofTypeNamed("ReleaseResource"))
                .thenContractIsUnchanged();
    }
}
