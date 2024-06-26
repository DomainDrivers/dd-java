package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.MockedEventPublisherConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableResourceId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Calendars;
import domaindrivers.smartschedule.availability.Owner;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.shared.CapabilitySelector;
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
@Import({TestDbConfiguration.class, MockedEventPublisherConfiguration.class})
@Sql(scripts = {"classpath:schema-allocations.sql", "classpath:schema-availability.sql"})
class ResourceAllocatingTest {

    static final AllocatableResourceId RESOURCE_ID = AllocatableResourceId.newOne();

    @Autowired
    AllocationFacade allocationFacade;

    @Autowired
    AvailabilityFacade availabilityFacade;

    @Autowired
    CapabilityScheduler capabilityScheduler;

    @Test
    void canAllocateCapabilityToProject() {
        //given
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Capability skillJava = Capability.skill("JAVA");
        Demand demand = new Demand(skillJava, oneDay);
        //and
        AllocatableCapabilityId allocatableCapabilityId = createAllocatableResource(oneDay, skillJava, RESOURCE_ID);
        //and
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        //and
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.of(demand));

        //when
        Optional<UUID> result = allocationFacade.allocateToProject(projectId, allocatableCapabilityId, oneDay);

        //then
        assertTrue(result.isPresent());
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        assertThat(summary.projectAllocations().get(projectId).all()).containsExactly(new AllocatedCapability(allocatableCapabilityId, CapabilitySelector.canJustPerform(skillJava), oneDay));
        assertThat(summary.demands().get(projectId).all()).containsExactly(demand);
        assertThat(availabilityWasBlocked(allocatableCapabilityId.toAvailabilityResourceId(), oneDay, projectId)).isTrue();
    }

    @Test
    void cantAllocateWhenResourceNotAvailable() {
        //given
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Capability skillJava = Capability.skill("JAVA");
        Demand demand = new Demand(skillJava, oneDay);
        //and
        AllocatableCapabilityId allocatableCapabilityId = createAllocatableResource(oneDay, skillJava, RESOURCE_ID);
        //and
        availabilityFacade.block(allocatableCapabilityId.toAvailabilityResourceId(), oneDay, Owner.newOne());
        //and
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        //and
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.of(demand));

        //when
        Optional<UUID> result = allocationFacade.allocateToProject(projectId, allocatableCapabilityId, oneDay);

        //then
        assertFalse(result.isPresent());
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        assertThat(summary.projectAllocations().get(projectId).all()).isEmpty();
    }

    @Test
    void cantAllocateWhenCapabilityHasNotBeenScheduled() {
        //given
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Capability skillJava = Capability.skill("JAVA");
        Demand demand = new Demand(skillJava, oneDay);
        //and
        AllocatableCapabilityId notScheduledCapability = AllocatableCapabilityId.newOne();
        //and
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        //and
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.of(demand));

        //when
        Optional<UUID> result = allocationFacade.allocateToProject(projectId, notScheduledCapability, oneDay);

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
        AllocatableCapabilityId allocatableCapabilityId = createAllocatableResource(oneDay, Capability.skill("JAVA"), RESOURCE_ID);
        //and
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        //and
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.none());
        //and
        allocationFacade.allocateToProject(projectId, allocatableCapabilityId, oneDay);

        //when
        boolean result = allocationFacade.releaseFromProject(projectId, allocatableCapabilityId, oneDay);

        //then
        assertTrue(result);
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        assertThat(summary.projectAllocations().get(projectId).all()).isEmpty();
        assertThat(availabilityIsReleased(oneDay, allocatableCapabilityId, projectId)).isTrue();

    }

    AllocatableCapabilityId scheduleCapabilities(AllocatableResourceId allocatableResourceId, CapabilitySelector capabilities, TimeSlot oneDay) {
        List<AllocatableCapabilityId> allocatableCapabilityIds = capabilityScheduler.scheduleResourceCapabilitiesForPeriod(allocatableResourceId, List.of(capabilities), oneDay);
        assert allocatableCapabilityIds.size() == 1;
        return allocatableCapabilityIds.get(0);
    }

    AllocatableCapabilityId createAllocatableResource(TimeSlot period, Capability capability, AllocatableResourceId resourceId) {
        CapabilitySelector capabilitySelector = CapabilitySelector.canJustPerform(capability);
        return scheduleCapabilities(resourceId, capabilitySelector, period);
    }

    boolean availabilityWasBlocked(ResourceId resource, TimeSlot period, ProjectAllocationsId projectId) {
        Calendars calendars = availabilityFacade.loadCalendars(Set.of(resource), period);
        return calendars
                .calendars()
                .values()
                .stream()
                .allMatch(calendar -> calendar.takenBy(Owner.of(projectId.id())).equals(List.of(period)));

    }

    boolean availabilityIsReleased(TimeSlot oneDay, AllocatableCapabilityId allocatableCapabilityId, ProjectAllocationsId projectId) {
        return !availabilityWasBlocked(allocatableCapabilityId.toAvailabilityResourceId(), oneDay, projectId);
    }


}