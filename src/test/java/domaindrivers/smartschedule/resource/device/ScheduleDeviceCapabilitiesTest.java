package domaindrivers.smartschedule.resource.device;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.allocation.AllocationFacade;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilitiesSummary;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityFinder;
import domaindrivers.smartschedule.resource.device.DeviceFacade;
import domaindrivers.smartschedule.resource.device.DeviceId;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = {"classpath:schema-resources.sql", "classpath:schema-availability.sql", "classpath:schema-capability-scheduling.sql"})
class ScheduleDeviceCapabilitiesTest {

    @Autowired
    DeviceFacade deviceFacade;

    @Autowired
    CapabilityFinder capabilityFinder;

    @Test
    void canSetupCapabilitiesAccordingToPolicy() {
        //given
        DeviceId device = deviceFacade.createDevice(
                "super-bulldozer-3000",
                Capability.assets("EXCAVATOR", "BULLDOZER"));
        //when
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        List<AllocatableCapabilityId> allocations = deviceFacade.scheduleCapabilities(device, oneDay);

        //then
        AllocatableCapabilitiesSummary loaded = capabilityFinder.findById(allocations);
        assertEquals(allocations.size(), loaded.all().size());
    }
}