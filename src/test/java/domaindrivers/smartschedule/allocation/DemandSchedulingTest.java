package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.MockedEventPublisherConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.EventsPublisher;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestDbConfiguration.class, MockedEventPublisherConfiguration.class})
@Sql(scripts = {"classpath:schema-allocations.sql"})
class DemandSchedulingTest {

    static final Demand JAVA = new Demand(
            Capability.skill("JAVA"),
            TimeSlot.createDailyTimeSlotAtUTC(2022, 2, 2));
    static final TimeSlot PROJECT_DATES = new TimeSlot(
            Instant.parse("2021-01-01T00:00:00.00Z"),
            Instant.parse("2021-01-06T00:00:00.00Z"));

    @Autowired
    AllocationFacade allocationFacade;

    @Autowired
    EventsPublisher eventsPublisher;

    @Test
    void canScheduleProjectDemands() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();

        //when
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.of(JAVA));

        //then
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        assertThat(summary.projectAllocations()).containsKey(projectId);
        assertThat(summary.projectAllocations().get(projectId).all()).hasSize(0);
        assertThat(summary.demands().get(projectId).all()).containsOnlyOnceElementsOf(Demands.of(JAVA).all());
    }


    @Test
    void projectDemandsScheduledEventIsEmittedAfterDefiningDemands() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();

        //when
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.of(JAVA));

        //then
        Mockito.verify(eventsPublisher).publish(Mockito.argThat(isProjectDemandsScheduledEvent(projectId, Demands.of(JAVA))));
    }

    ArgumentMatcher<ProjectAllocationsDemandsScheduled> isProjectDemandsScheduledEvent(ProjectAllocationsId projectId, Demands demands) {
        return event ->
                event.uuid() != null &&
                        event.projectId().equals(projectId) &&
                        event.missingDemands().equals(demands) &&
                        event.occurredAt() != null;
    }

}