package domaindrivers.smartschedule.resource.employee;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.allocation.AllocationFacade;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilitiesSummary;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityFinder;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import domaindrivers.smartschedule.resource.employee.EmployeeFacade;
import domaindrivers.smartschedule.resource.employee.EmployeeId;
import domaindrivers.smartschedule.resource.employee.Seniority;
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
class ScheduleEmployeeCapabilitiesTest {

    @Autowired
    CapabilityFinder capabilityFinder;

    @Autowired
    EmployeeFacade employeeFacade;

    @Test
    void canSetupCapabilitiesAccordingToPolicy() {
        //given
        EmployeeId employee = employeeFacade.addEmployee(
                "resourceName", "lastName", Seniority.LEAD,
                Capability.skills("JAVA, PYTHON"),
                Capability.permissions("ADMIN"));
        //when
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        List<AllocatableCapabilityId> allocations = employeeFacade.scheduleCapabilities(employee, oneDay);

        //then
        AllocatableCapabilitiesSummary loaded = capabilityFinder.findById(allocations);
        assertEquals(allocations.size(), loaded.all().size());

    }
}