package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityFinder;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.shared.EventsPublisher;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.time.Clock;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


class CreatingNewProjectTest {

    EventsPublisher  eventsPublisher = mock(EventsPublisher.class);

    AllocationFacade allocationFacade =
            new AllocationFacade(new InMemoryProjectAllocationsRepository(),
                    mock(AvailabilityFacade.class),
                    mock(CapabilityFinder.class),
                    eventsPublisher,
                    Clock.systemDefaultZone());


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