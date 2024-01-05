package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateHourlyDemandsSummaryServiceTest {

    static Instant NOW = Instant.now();
    static TimeSlot JAN = TimeSlot.createMonthlyTimeSlotAtUTC(2021, 1);
    static Demands CSHARP = Demands.of(new Demand(skill("CSHARP"), JAN));
    static Demands JAVA = Demands.of(new Demand(skill("JAVA"), JAN));

    CreateHourlyDemandsSummaryService service = new CreateHourlyDemandsSummaryService();

    @Test
    void createsMissingDemandsSummaryForAllGivenProjects() {
        //given
        ProjectAllocationsId csharpProjectId = ProjectAllocationsId.newOne();
        ProjectAllocationsId javaProjectId = ProjectAllocationsId.newOne();
        ProjectAllocations csharpProject = new ProjectAllocations(csharpProjectId, Allocations.none(), CSHARP, JAN);
        ProjectAllocations javaProject = new ProjectAllocations(javaProjectId, Allocations.none(), JAVA, JAN);

        //when
        NotSatisfiedDemands result = service.create(List.of(csharpProject, javaProject), NOW);

        //then
        assertEquals(NOW, result.occurredAt());
        Map<ProjectAllocationsId, Demands> expectedMissingDemands =
                Map.of(javaProjectId, JAVA, csharpProjectId, CSHARP);
        assertThat(result.missingDemands()).containsExactlyInAnyOrderEntriesOf(expectedMissingDemands);
    }

    @Test
    void takesIntoAccountOnlyProjectsWithTimeSlot() {
        //given
        ProjectAllocationsId withTimeSlotId = ProjectAllocationsId.newOne();
        ProjectAllocationsId withoutTimeSlotId = ProjectAllocationsId.newOne();
        ProjectAllocations withTimeSlot = new ProjectAllocations(withTimeSlotId, Allocations.none(), CSHARP, JAN);
        ProjectAllocations withoutTimeSlot = new ProjectAllocations(withoutTimeSlotId, Allocations.none(), JAVA);

        //when
        NotSatisfiedDemands result = service.create(List.of(withTimeSlot, withoutTimeSlot), NOW);

        //then
        assertEquals(NOW, result.occurredAt());
        Map<ProjectAllocationsId, Demands> expectedMissingDemands =
                Map.of(withTimeSlotId, CSHARP);
        assertThat(result.missingDemands()).containsExactlyInAnyOrderEntriesOf(expectedMissingDemands);
    }


}