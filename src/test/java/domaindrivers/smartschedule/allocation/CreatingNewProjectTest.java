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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestDbConfiguration.class, MockedEventPublisherConfiguration.class})
@Sql(scripts = {"classpath:schema-allocations.sql"})
class CreatingNewProjectTest {

    @Autowired
    AllocationFacade allocationFacade;

    @Autowired
    EventsPublisher eventsPublisher;

    static TimeSlot JAN = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
    static TimeSlot FEB = TimeSlot.createDailyTimeSlotAtUTC(2021, 2, 1);

    @Test
    void canCreateNewProject() {
        //given
        Demand demand = new Demand(Capability.skill("JAVA"), JAN);

        //when
        Demands demands = Demands.of(demand);
        ProjectAllocationsId newProject = allocationFacade.createAllocation(JAN, demands);

        //then
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations(Set.of(newProject));
        assertThat(summary.demands().get(newProject)).isEqualTo(demands);
        assertThat(summary.timeSlots().get(newProject)).isEqualTo(JAN);
        Mockito.verify(eventsPublisher).publish(Mockito.argThat(isProjectAllocationsScheduledEvent(newProject, JAN)));

    }

    @Test
    void canRedefineProjectDeadline() {
        //given
        Demand demand = new Demand(Capability.skill("JAVA"), JAN);
        //and
        Demands demands = Demands.of(demand);
        ProjectAllocationsId newProject = allocationFacade.createAllocation(JAN, demands);

        //when
        allocationFacade.editProjectDates(newProject, FEB);

        //then
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations(Set.of(newProject));
        assertThat(summary.timeSlots().get(newProject)).isEqualTo(FEB);
        Mockito.verify(eventsPublisher).publish(Mockito.argThat(isProjectAllocationsScheduledEvent(newProject, FEB)));
    }

    ArgumentMatcher<ProjectAllocationScheduled> isProjectAllocationsScheduledEvent(ProjectAllocationsId projectId, TimeSlot timeSlot) {
        return event ->
                event.uuid() != null &&
                        event.projectId().equals(projectId) &&
                        event.fromTo().equals(timeSlot) &&
                        event.occurredAt() != null;
    }

}