package domaindrivers.smartschedule.planning;


import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.planning.parallelization.*;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.EventsPublisher;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class PlanningFacade {

    private final ProjectRepository projectRepository;
    private final StageParallelization parallelization;
    private final PlanChosenResources planChosenResourcesService;
    private EventsPublisher eventsPublisher;
    private Clock clock;

    public PlanningFacade(ProjectRepository projectRepository, StageParallelization parallelization, PlanChosenResources resourcesPlanning, EventsPublisher eventsPublisher, Clock clock) {
        this.projectRepository = projectRepository;
        this.parallelization = parallelization;
        this.planChosenResourcesService = resourcesPlanning;
        this.eventsPublisher = eventsPublisher;
        this.clock = clock;
    }

    public ProjectId addNewProject(String name, Stage... stages) {
        ParallelStagesList parallelizedStages = parallelization.of(new HashSet<>(asList(stages)));
        return addNewProject(name, parallelizedStages);
    }

    public ProjectId addNewProject(String name, ParallelStagesList parallelizedStages) {
        Project project = new Project(name, parallelizedStages);
        projectRepository.save(project);
        return project.id();
    }

    public void defineStartDate(ProjectId projectId, Instant possibleStartDate) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addSchedule(possibleStartDate);
        projectRepository.save(project);
    }

    public void defineProjectStages(ProjectId projectId, Stage... stages) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        ParallelStagesList parallelizedStages = parallelization.of(new HashSet<>(asList(stages)));
        project.defineStages(parallelizedStages);
        projectRepository.save(project);
    }

    public void addDemands(ProjectId projectId, Demands demands) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addDemands(demands);
        projectRepository.save(project);
        eventsPublisher.publish(new CapabilitiesDemanded(projectId, project.getAllDemands(), clock.instant()));
    }

    public void defineDemandsPerStage(ProjectId projectId, DemandsPerStage demandsPerStage) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addDemandsPerStage(demandsPerStage);
        projectRepository.save(project);
        eventsPublisher.publish(new CapabilitiesDemanded(projectId, project.getAllDemands(), clock.instant()));
    }

    public void defineResourcesWithinDates(ProjectId projectId, Set<ResourceId> chosenResources, TimeSlot timeBoundaries) {
        planChosenResourcesService.defineResourcesWithinDates(projectId, chosenResources, timeBoundaries);
    }

    public void adjustStagesToResourceAvailability(ProjectId projectId, TimeSlot timeBoundaries, Stage... stages) {
        planChosenResourcesService.adjustStagesToResourceAvailability(projectId, timeBoundaries, stages);
    }

    public void planCriticalStageWithResource(ProjectId projectId, Stage criticalStage, ResourceId resourceId, TimeSlot stageTimeSlot) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addSchedule(criticalStage, stageTimeSlot);
        eventsPublisher.publish(new CriticalStagePlanned(projectId, stageTimeSlot, resourceId, clock.instant()));
        projectRepository.save(project);
    }

    public void planCriticalStage(ProjectId projectId, Stage criticalStage, TimeSlot stageTimeSlot) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addSchedule(criticalStage, stageTimeSlot);
        eventsPublisher.publish(new CriticalStagePlanned(projectId, stageTimeSlot, null, clock.instant()));
        projectRepository.save(project);
    }

    public void defineManualSchedule(ProjectId projectId, Schedule schedule) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addSchedule(schedule);
        projectRepository.save(project);
    }

    public Duration durationOf(Stage... stages) {
        return new DurationCalculator().apply(List.of(stages));
    }

    public ProjectCard load(ProjectId projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        return toSummary(project);
    }

    public List<ProjectCard> loadAll(Set<ProjectId> projectsIds) {
        return projectRepository
                .findAllByIdIn(projectsIds)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public List<ProjectCard> loadAll() {
        return projectRepository
                .findAll()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private ProjectCard toSummary(Project project) {
        return new ProjectCard(project.id(), project.name(), project.getParallelizedStages(), project.getAllDemands(), project.getSchedule(), project.getDemandsPerStage(), project.getChosenResources());
    }

}

