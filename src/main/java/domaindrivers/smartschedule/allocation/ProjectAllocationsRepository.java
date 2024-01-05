package domaindrivers.smartschedule.allocation;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;


interface ProjectAllocationsRepository  {

    List<ProjectAllocations> findAllContainingDate(Instant when);

    Optional<ProjectAllocations> findById(ProjectAllocationsId projectId);

    ProjectAllocations save(ProjectAllocations project);

    List<ProjectAllocations> findAllById(Set<ProjectAllocationsId> projectIds);

    List<ProjectAllocations> findAll();
}
