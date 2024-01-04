package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Calendars;
import domaindrivers.smartschedule.availability.Owner;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = {"classpath:schema-allocations.sql", "classpath:schema-availability.sql"})
class CapabilityAllocatingTest {

    @Autowired
    AllocationFacade allocationFacade;

    @Autowired
    AvailabilityFacade availabilityFacade;

    @Test
    void canAllocateCapabilityToProject() {
        //given
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Capability skillJava = Capability.skill("JAVA");
        Demand demand = new Demand(skillJava, oneDay);
        //and
        ResourceId allocatableResourceId = createAllocatableResource(oneDay);
        //and
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        //and
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.of(demand));

        //when
        Optional<UUID> result = allocationFacade.allocateToProject(projectId, allocatableResourceId, skillJava, oneDay);

        //then
        assertTrue(result.isPresent());
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        assertThat(summary.projectAllocations().get(projectId).all()).containsExactly(new AllocatedCapability(allocatableResourceId.getId(), skillJava, oneDay));
        assertThat(summary.demands().get(projectId).all()).containsExactly(demand);
        assertThat(availabilityWasBlocked(allocatableResourceId, oneDay, projectId)).isTrue();
    }

    @Test
    void cantAllocateWhenResourceNotAvailable() {
        //given
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Capability skillJava = Capability.skill("JAVA");
        Demand demand = new Demand(skillJava, oneDay);
        //and
        ResourceId allocatableResourceId = createAllocatableResource(oneDay);
        //and
        availabilityFacade.block(allocatableResourceId, oneDay, Owner.newOne());
        //and
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        //and
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.of(demand));

        //when
        Optional<UUID> result = allocationFacade.allocateToProject(projectId, allocatableResourceId, skillJava, oneDay);

        //then
        assertFalse(result.isPresent());
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        assertThat(summary.projectAllocations().get(projectId).all()).isEmpty();
    }

    @Test
    void canReleaseCapabilityFromProject() {
        //given
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        //and
        ResourceId allocatableResourceId = createAllocatableResource(oneDay);
        //and
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        //and
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.none());
        //and
        Capability chosenCapability = Capability.skill("JAVA");
        Optional<UUID> allocatedId = allocationFacade.allocateToProject(projectId, allocatableResourceId, chosenCapability, oneDay);

        //when
        boolean result = allocationFacade.releaseFromProject(projectId, allocatedId.get(), oneDay);

        //then
        assertTrue(result);
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        assertThat(summary.projectAllocations().get(projectId).all()).isEmpty();
    }

    ResourceId createAllocatableResource(TimeSlot period) {
        ResourceId resourceId = ResourceId.newOne();
        availabilityFacade.createResourceSlots(resourceId, period);
        return resourceId;
    }

    boolean availabilityWasBlocked(ResourceId resource, TimeSlot period, ProjectAllocationsId projectId) {
        Calendars calendars = availabilityFacade.loadCalendars(Set.of(resource), period);
        return calendars
                .calendars()
                .values()
                .stream()
                .allMatch(calendar -> calendar.takenBy(Owner.of(projectId.id())).equals(List.of(period)));

    }



}