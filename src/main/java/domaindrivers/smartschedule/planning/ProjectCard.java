package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.planning.parallelization.ParallelStagesList;
import domaindrivers.smartschedule.planning.schedule.Schedule;


record ProjectCard(ProjectId projectId, String name, ParallelStagesList parallelizedStages, Demands demands,
                   Schedule schedule, DemandsPerStage demandsPerStage, ChosenResources neededResources) {

    ProjectCard(ProjectId projectId, String name, ParallelStagesList parallelizedStages, Demands demands) {
        this(projectId, name, parallelizedStages, demands, Schedule.none(), DemandsPerStage.empty(), ChosenResources.none());
    }

}

