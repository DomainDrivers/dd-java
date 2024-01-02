package domaindrivers.smartschedule.planning;


import domaindrivers.smartschedule.planning.parallelization.*;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.ResourceName;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.transaction.Transactional;

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

    public PlanningFacade(ProjectRepository projectRepository, StageParallelization parallelization, PlanChosenResources resourcesPlanning) {
        this.projectRepository = projectRepository;
        this.parallelization = parallelization;
        this.planChosenResourcesService = resourcesPlanning;
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

    @Transactional
    public void defineStartDate(ProjectId projectId, Instant possibleStartDate) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addSchedule(possibleStartDate);
    }

    @Transactional
    public void defineProjectStages(ProjectId projectId, Stage... stages) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        ParallelStagesList parallelizedStages = parallelization.of(new HashSet<>(asList(stages)));
        project.defineStages(parallelizedStages);
    }

    @Transactional
    public void addDemands(ProjectId projectId, Demands demands) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addDemands(demands);
    }

    @Transactional
    public void defineDemandsPerStage(ProjectId projectId, DemandsPerStage demandsPerStage) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addDemandsPerStage(demandsPerStage);
    }

    @Transactional
    public void defineResourcesWithinDates(ProjectId projectId, Set<ResourceName> chosenResources, TimeSlot timeBoundaries) {
        planChosenResourcesService.defineResourcesWithinDates(projectId, chosenResources, timeBoundaries);
    }

    @Transactional
    public void adjustStagesToResourceAvailability(ProjectId projectId, TimeSlot timeBoundaries, Stage... stages) {
        planChosenResourcesService.adjustStagesToResourceAvailability(projectId, timeBoundaries, stages);
    }

    @Transactional
    public void planCriticalStageWithResource(ProjectId projectId, Stage criticalStage, ResourceName criticalResource, TimeSlot stageTimeSlot) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addSchedule(criticalStage, stageTimeSlot);
    }

    @Transactional
    public void planCriticalStage(ProjectId projectId, Stage criticalStage, TimeSlot stageTimeSlot) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addSchedule(criticalStage, stageTimeSlot);
    }

    @Transactional
    public void defineManualSchedule(ProjectId projectId, Schedule schedule) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addSchedule(schedule);
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
                .findAllById(projectsIds)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private ProjectCard toSummary(Project project) {
        return new ProjectCard(project.id(), project.name(), project.getParallelizedStages(), project.getAllDemands(), project.getSchedule(), project.getDemandsPerStage(), project.getChosenResources());
    }

}

