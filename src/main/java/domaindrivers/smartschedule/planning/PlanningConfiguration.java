package domaindrivers.smartschedule.planning;


import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.planning.parallelization.StageParallelization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PlanningConfiguration {

    @Bean
    PlanningFacade planningFacade(ProjectRepository projectRepository, PlanChosenResources planChosenResourcesService) {
        return new PlanningFacade(projectRepository, new StageParallelization(), planChosenResourcesService);
    }

    @Bean
    PlanChosenResources planChosenResourcesService(ProjectRepository projectRepository) {
        return new PlanChosenResources(projectRepository, new AvailabilityFacade(null));
    }

}
