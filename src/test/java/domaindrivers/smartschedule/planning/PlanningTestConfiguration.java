package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.planning.parallelization.StageParallelization;
import domaindrivers.smartschedule.shared.EventsPublisher;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;



class PlanningTestConfiguration {

    static PlanningFacade planningFacadeWithInMemoryDb(EventsPublisher eventsPublisher) {
        Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        ProjectRepository projectRepository = PlanningDbTestConfiguration.inMemoryProjectDb();
        PlanChosenResources planChosenResources = new PlanChosenResources(projectRepository, Mockito.mock(AvailabilityFacade.class), eventsPublisher, clock);
        return new PlanningFacade(projectRepository, new StageParallelization(), planChosenResources, eventsPublisher, clock);
    }
}
