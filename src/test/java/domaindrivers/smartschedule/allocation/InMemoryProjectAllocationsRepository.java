package domaindrivers.smartschedule.allocation;


import java.time.Instant;
import java.util.*;


class InMemoryProjectAllocationsRepository implements ProjectAllocationsRepository {

    private final Map<ProjectAllocationsId, ProjectAllocations> projects = new HashMap<>();


    @Override
    public Optional<ProjectAllocations> findById(ProjectAllocationsId projectId) {
        return Optional.ofNullable(projects.get(projectId));
    }

    @Override
    public ProjectAllocations save(ProjectAllocations project) {
        return projects.put(project.id(), project);
    }

    @Override
    public List<ProjectAllocations> findAllById(Set<ProjectAllocationsId> projectIds) {
        return projects
                .values()
                .stream()
                .filter(project -> projectIds.contains(project.id()))
                .toList();
    }

    @Override
    public List<ProjectAllocations> findAll() {
        return new ArrayList<>(projects.values());
    }

    @Override
    public List<ProjectAllocations> findAllContainingDate(Instant when) {
        return projects
                .values()
                .stream()
                .filter(project -> project.timeSlot() != null)
                .toList();
    }
}
