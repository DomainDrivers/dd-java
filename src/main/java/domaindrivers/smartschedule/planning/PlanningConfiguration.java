package domaindrivers.smartschedule.planning;


import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.planning.parallelization.StageParallelization;
import domaindrivers.smartschedule.shared.EventsPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class PlanningConfiguration {

    @Bean
    PlanningFacade planningFacade(ProjectRepository projectRepository, PlanChosenResources planChosenResourcesService, EventsPublisher eventsPublisher, Clock clock) {
        return new PlanningFacade(projectRepository, new StageParallelization(), planChosenResourcesService, eventsPublisher, clock);
    }

    @Bean
    PlanChosenResources planChosenResourcesService(ProjectRepository projectRepository, AvailabilityFacade availabilityFacade, EventsPublisher eventsPublisher, Clock clock) {
        return new PlanChosenResources(projectRepository, availabilityFacade, eventsPublisher, clock);
    }

}
