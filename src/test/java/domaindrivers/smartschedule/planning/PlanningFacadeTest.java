package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.EventsPublisher;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static domaindrivers.smartschedule.planning.Demand.demandFor;
import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;

class PlanningFacadeTest {

    EventsPublisher eventsPublisher = Mockito.mock(EventsPublisher.class);
    PlanningFacade projectFacade = PlanningTestConfiguration.planningFacadeWithInMemoryDb(eventsPublisher);

    @Test
    void canCreateProjectAndLoadProjectCard() {
        //given
        ProjectId projectId =
                projectFacade.addNewProject("project", new Stage("Stage1"));

        //when
        ProjectCard loaded = projectFacade.load(projectId);

        //then
        assertEquals(projectId, loaded.projectId());
        assertEquals("project", loaded.name());
        assertEquals("Stage1", loaded.parallelizedStages().print());
    }

    @Test
    void canLoadMultipleProjects() {
        //given
        ProjectId projectId = projectFacade.addNewProject("project", new Stage("Stage1"));
        ProjectId projectId2 = projectFacade.addNewProject("project2", new Stage("Stage2"));

        //when
        List<ProjectCard> loaded = projectFacade.loadAll(Set.of(projectId, projectId2));

        //then
        assertThat(loaded.stream().map(ProjectCard::projectId).toList()).containsExactlyInAnyOrder(projectId, projectId2);
    }

    @Test
    void canCreateAndSaveMoreComplexParallelization() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2");
        Stage stage3 = new Stage("Stage3");
        stage2 = stage2.dependsOn(stage1);
        stage3 = stage3.dependsOn(stage2);

        //and
        ProjectId projectId = projectFacade.addNewProject("project", stage1, stage2, stage3);

        //when
        ProjectCard loaded = projectFacade.load(projectId);

        //then
        assertEquals("Stage1 | Stage2 | Stage3", loaded.parallelizedStages().print());
    }

    @Test
    void canPlanDemands() {
        //given
        ProjectId projectId = projectFacade.addNewProject("project", new Stage("Stage1"));

        //when
        Demands demandForJava = Demands.of(demandFor(skill("JAVA")));
        projectFacade.addDemands(projectId, demandForJava);

        //then
        ProjectCard loaded = projectFacade.load(projectId);
        assertEquals(demandForJava, loaded.demands());
    }

    @Test
    void canPlanNewDemands() {
        //given
        ProjectId projectId = projectFacade.addNewProject("project", new Stage("Stage1"));

        //when
        Demand java = demandFor(skill("JAVA"));
        Demand csharp = demandFor(skill("C#"));
        projectFacade.addDemands(projectId, Demands.of(java));
        projectFacade.addDemands(projectId, Demands.of(csharp));

        //then
        ProjectCard loaded = projectFacade.load(projectId);
        assertEquals(Demands.of(java, csharp), loaded.demands());
    }

    @Test
    void canPlanDemandsPerStage() {
        //given
        ProjectId projectId = projectFacade.addNewProject("project", new Stage("Stage1"));

        //when
        Demands java = Demands.of(demandFor(skill("JAVA")));
        DemandsPerStage demandsPerStage = new DemandsPerStage(Map.of("Stage1", java));
        projectFacade.defineDemandsPerStage(projectId, demandsPerStage);

        //then
        ProjectCard loaded = projectFacade.load(projectId);
        assertEquals(demandsPerStage, loaded.demandsPerStage());
        assertEquals(java, loaded.demands());
    }

    @Test
    void canPlanNeededResourcesInTime() {
        //given
        ProjectId projectId = projectFacade.addNewProject("project");

        //when
        Set<ResourceId> neededResources = Set.of(ResourceId.newOne());
        TimeSlot firstHalfOfTheYear = new TimeSlot(Instant.parse("2021-01-01T00:00:00.00Z"), Instant.parse("2021-06-01T00:00:00.00Z"));
        projectFacade.defineResourcesWithinDates(projectId, neededResources, firstHalfOfTheYear);

        //then
        ProjectCard loaded = projectFacade.load(projectId);
        assertEquals(new ChosenResources(neededResources, firstHalfOfTheYear), loaded.neededResources());
    }

    @Test
    void canRedefineStages() {
        //given
        ProjectId projectId = projectFacade.addNewProject("project", new Stage("Stage1"));

        //when
        projectFacade.defineProjectStages(projectId, new Stage("Stage2"));

        //then
        ProjectCard loaded = projectFacade.load(projectId);
        assertEquals("Stage2", loaded.parallelizedStages().print());
    }

    @Test
    void canCalculateScheduleAfterPassingPossibleStart() {
        //given
        Stage stage1 = new Stage("Stage1").ofDuration(Duration.ofDays(2));
        Stage stage2 = new Stage("Stage2").ofDuration(Duration.ofDays(5));
        Stage stage3 = new Stage("Stage3").ofDuration(Duration.ofDays(7));

        //and
        ProjectId projectId = projectFacade.addNewProject("project", stage1, stage2, stage3);

        //when
        projectFacade.defineStartDate(projectId, Instant.parse("2021-01-01T00:00:00.00Z"));

        //then
        Map<String, TimeSlot> expectedSchedule = Map.of(
                "Stage1", new TimeSlot(Instant.parse("2021-01-01T00:00:00.00Z"), Instant.parse("2021-01-03T00:00:00.00Z")),
                "Stage2", new TimeSlot(Instant.parse("2021-01-01T00:00:00.00Z"), Instant.parse("2021-01-06T00:00:00.00Z")),
                "Stage3", new TimeSlot(Instant.parse("2021-01-01T00:00:00.00Z"), Instant.parse("2021-01-08T00:00:00.00Z")));
        ProjectCard loaded = projectFacade.load(projectId);
        assertThat(loaded.schedule().dates()).containsExactlyInAnyOrderEntriesOf(expectedSchedule);
    }

    @Test
    void canManuallyAddSchedule() {
        //given
        Stage stage1 = new Stage("Stage1").ofDuration(Duration.ofDays(2));
        Stage stage2 = new Stage("Stage2").ofDuration(Duration.ofDays(5));
        Stage stage3 = new Stage("Stage3").ofDuration(Duration.ofDays(7));
        //and
        ProjectId projectId = projectFacade.addNewProject("project", stage1, stage2, stage3);

        //when
        Map<String, TimeSlot> dates = Map.of(
                "Stage1", new TimeSlot(Instant.parse("2021-01-01T00:00:00.00Z"), Instant.parse("2021-01-03T00:00:00.00Z")),
                "Stage2", new TimeSlot(Instant.parse("2021-01-03T00:00:00.00Z"), Instant.parse("2021-01-08T00:00:00.00Z")),
                "Stage3", new TimeSlot(Instant.parse("2021-01-08T00:00:00.00Z"), Instant.parse("2021-01-15T00:00:00.00Z")));
        projectFacade.defineManualSchedule(projectId, new Schedule(dates));

        //then
        ProjectCard loaded = projectFacade.load(projectId);
        assertThat(loaded.schedule().dates()).containsExactlyInAnyOrderEntriesOf(dates);
    }

    @Test
    void capabilitiesDemandedEventIsEmittedAfterAddingDemands() {
        //given
        ProjectId projectId =
                projectFacade.addNewProject("project", new Stage("Stage1"));
        //and
        Demands demandForJava = Demands.of(demandFor(skill("JAVA")));
        projectFacade.addDemands(projectId, demandForJava);

        //then
        Mockito.verify(eventsPublisher).publish(argThat(capabilitiesDemanded(projectId, demandForJava)));
    }

    ArgumentMatcher<CapabilitiesDemanded> capabilitiesDemanded(ProjectId projectId, Demands demands) {
        return event ->
                event.projectId().equals(projectId) &&
                        event.demands().equals(demands) &&
                        event.uuid() != null;
    }

}