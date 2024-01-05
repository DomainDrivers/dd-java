package domaindrivers.smartschedule.planning;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.*;

@Configuration
public class PlanningDbTestConfiguration {

    static ProjectRepository inMemoryProjectDb() {
        return new InMemoryProjectRepository();
    }

    @Bean
    @Primary
    ProjectRepository inMemoryProjectDbBean() {
        return inMemoryProjectDb();
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

