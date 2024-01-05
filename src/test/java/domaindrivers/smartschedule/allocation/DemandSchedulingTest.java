package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityFinder;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.shared.EventsPublisher;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


class DemandSchedulingTest {

    static final Demand JAVA = new Demand(
            Capability.skill("JAVA"),
            TimeSlot.createDailyTimeSlotAtUTC(2022, 2, 2));
    static final TimeSlot PROJECT_DATES = new TimeSlot(
            Instant.parse("2021-01-01T00:00:00.00Z"),
            Instant.parse("2021-01-06T00:00:00.00Z"));

    AllocationFacade allocationFacade =
            new AllocationFacade(new InMemoryProjectAllocationsRepository(),
                    mock(AvailabilityFacade.class),
                    mock(CapabilityFinder.class),
                    mock(EventsPublisher.class),
                    Clock.systemDefaultZone());

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


}