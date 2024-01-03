package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = {"classpath:schema-allocations.sql"})
class CapabilityAllocatingTest {

    @Autowired
    AllocationFacade allocationFacade;

    @Test
    void canAllocateCapabilityToProject() {
        //given
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Capability skillJava = Capability.skill("JAVA");
        Demand demand = new Demand(skillJava, oneDay);
        //and
        ResourceId allocatableResourceId = ResourceId.newOne();
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
    }


    @Test
    void canReleaseCapabilityFromProject() {
        //given
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        //and
        ResourceId allocatableResourceId = ResourceId.newOne();
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


}