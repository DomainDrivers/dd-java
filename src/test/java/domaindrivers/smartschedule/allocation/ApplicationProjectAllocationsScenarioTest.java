package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.ControllableClockConfiguration;
import domaindrivers.smartschedule.MockedEventPublisherConfiguration;
import domaindrivers.smartschedule.MutableClock;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import domaindrivers.smartschedule.shared.EventsPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Import({TestDbConfiguration.class, MockedEventPublisherConfiguration.class, ControllableClockConfiguration.class})
@Sql(scripts = {"classpath:schema-allocations.sql", "classpath:schema-availability.sql"})
class ApplicationProjectAllocationsScenarioTest extends ProjectAllocationsScenario {

    @Autowired
    AllocationFacade allocationFacade;

    @Autowired
    CapabilityScheduler capabilityScheduler;

    @Autowired
    EventsPublisher eventsPublisher;

    @Autowired
    MutableClock clock;

    @Override
    ProjectAllocationsDsl dsl() {
        return new ApplicationProjectAllocationsDriver(allocationFacade, capabilityScheduler, eventsPublisher, clock);
    }
}
