package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.planning.parallelization.StageParallelization;
import domaindrivers.smartschedule.shared.EventsPublisher;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;


public class PlanningTestConfiguration {

    static PlanningFacade planningFacade(EventsPublisher eventsPublisher, ProjectRepository projectRepository) {
        Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        PlanChosenResources planChosenResources = new PlanChosenResources(projectRepository, Mockito.mock(AvailabilityFacade.class), eventsPublisher, clock);
        return new PlanningFacade(projectRepository, new StageParallelization(), planChosenResources, eventsPublisher, clock);
    }
}

class InMemoryProjectRepository implements ProjectRepository {

    private final Map<ProjectId, Project> projects = new HashMap<>();

    @Override
    public Optional<Project> findById(ProjectId projectId) {
        return Optional.of(projects.get(projectId));
    }

    @Override
    public Project save(Project project) {
        return projects.put(project.id(), project);
    }

    @Override
    public List<Project> findAllByIdIn(Set<ProjectId> projectIds) {
        return projects
                .entrySet()
                .stream()
                .filter(entry -> projectIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

    @Override
    public List<Project> findAll() {
        return new ArrayList<>(projects.values());
    }
}
