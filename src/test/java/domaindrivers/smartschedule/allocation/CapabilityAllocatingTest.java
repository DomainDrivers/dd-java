package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableResourceId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Calendars;
import domaindrivers.smartschedule.availability.Owner;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Set;

import static domaindrivers.smartschedule.shared.CapabilitySelector.canPerformOneOf;
import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static domaindrivers.smartschedule.shared.capability.Capability.skills;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = {"classpath:schema-allocations.sql", "classpath:schema-availability.sql"})
class CapabilityAllocatingTest {

    static final AllocatableResourceId ALLOCATABLE_RESOURCE_ID = AllocatableResourceId.newOne();
    static final AllocatableResourceId ALLOCATABLE_RESOURCE_ID_2 = AllocatableResourceId.newOne();
    static final AllocatableResourceId ALLOCATABLE_RESOURCE_ID_3 = AllocatableResourceId.newOne();

    @Autowired
    AllocationFacade allocationFacade;

    @Autowired
    AvailabilityFacade availabilityFacade;

    @Autowired
    CapabilityScheduler capabilityScheduler;

    @Test
    void canAllocateAnyCapabilityOfRequiredType() {
        //given
        CapabilitySelector javaAndPython = canPerformOneOf(skills("JAVA11", "PYTHON"));
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        //and
        AllocatableCapabilityId allocatableCapabilityId1 = scheduleCapabilities(ALLOCATABLE_RESOURCE_ID, javaAndPython, oneDay);
        AllocatableCapabilityId allocatableCapabilityId2 = scheduleCapabilities(ALLOCATABLE_RESOURCE_ID_2, javaAndPython, oneDay);
        AllocatableCapabilityId allocatableCapabilityId3 = scheduleCapabilities(ALLOCATABLE_RESOURCE_ID_3, javaAndPython, oneDay);
        //and
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.none());

        //when
        boolean result = allocationFacade.allocateCapabilityToProjectForPeriod(projectId, skill("JAVA11"), oneDay);

        //then
        assertThat(result).isTrue();
        Set<AllocatableCapabilityId> allocatedCapabilities = loadProjectAllocations(projectId);
        assertThat(allocatedCapabilities).containsAnyOf(allocatableCapabilityId1, allocatableCapabilityId2, allocatableCapabilityId3);
        assertThat(availabilityWasBlocked(allocatedCapabilities, oneDay, projectId)).isTrue();
    }

    @Test
    void cantAllocateAnyCapabilityOfRequiredTypeWhenNoCapabilities() {
        //given
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        //and
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        //and
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.none());

        //when
        boolean result = allocationFacade.allocateCapabilityToProjectForPeriod(projectId, skill("DEBUGGING"), oneDay);

        //then
        assertThat(result).isFalse();
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        assertThat(summary.projectAllocations().get(projectId).all()).isEmpty();
    }

    @Test
    void cantAllocateAnyCapabilityOfRequiredTypeWhenAllCapabilitiesTaken() {
        //given
        CapabilitySelector capability =
                canPerformOneOf(skills("DEBUGGING"));
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);

        AllocatableCapabilityId allocatableCapabilityId1 = scheduleCapabilities(ALLOCATABLE_RESOURCE_ID, capability, oneDay);
        AllocatableCapabilityId allocatableCapabilityId2 = scheduleCapabilities(ALLOCATABLE_RESOURCE_ID_2, capability, oneDay);
        //and
        ProjectAllocationsId project1 = allocationFacade.createAllocation(oneDay, Demands.of(new Demand(skill("DEBUGGING"), oneDay)));
        ProjectAllocationsId project2 = allocationFacade.createAllocation(oneDay, Demands.of(new Demand(skill("DEBUGGING"), oneDay)));
        //and
        allocationFacade.allocateToProject(project1, allocatableCapabilityId1, skill("DEBUGGING"), oneDay);
        allocationFacade.allocateToProject(project2, allocatableCapabilityId2, skill("DEBUGGING"), oneDay);

        //and
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        allocationFacade.scheduleProjectAllocationDemands(projectId, Demands.none());

        //when
        boolean result = allocationFacade.allocateCapabilityToProjectForPeriod(projectId, skill("DEBUGGING"), oneDay);

        //then
        assertThat(result).isFalse();
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        assertThat(summary.projectAllocations().get(projectId).all()).isEmpty();
    }

    Set<AllocatableCapabilityId> loadProjectAllocations(ProjectAllocationsId projectId1) {
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        Set<AllocatableCapabilityId> allocatedCapabilities =
                summary
                        .projectAllocations()
                        .get(projectId1)
                        .all()
                        .stream()
                        .map(AllocatedCapability::allocatedCapabilityID)
                        .collect(toSet());
        return allocatedCapabilities;
    }

    AllocatableCapabilityId scheduleCapabilities(AllocatableResourceId allocatableResourceId, CapabilitySelector capabilities, TimeSlot oneDay) {
        List<AllocatableCapabilityId> allocatableCapabilityIds = capabilityScheduler.scheduleResourceCapabilitiesForPeriod(allocatableResourceId, List.of(capabilities), oneDay);
        assert allocatableCapabilityIds.size() == 1;
        return allocatableCapabilityIds.get(0);
    }

    boolean availabilityWasBlocked(Set<AllocatableCapabilityId> capabilities, TimeSlot oneDay, ProjectAllocationsId projectId) {
        Calendars calendars = availabilityFacade.loadCalendars(capabilities.stream()
                .map(AllocatableCapabilityId::toAvailabilityResourceId)
                .collect(toSet()), oneDay);
        return calendars
                .calendars()
                .values()
                .stream()
                .allMatch(calendar -> calendar.takenBy(Owner.of(projectId.id())).equals(List.of(oneDay)));
    }

}