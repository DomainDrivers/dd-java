package domaindrivers.smartschedule.planning;



import java.util.List;
import java.util.Optional;
import java.util.Set;


interface ProjectRepository  {

    Optional<Project> findById(ProjectId projectId);

    Project save(Project project);

    List<Project> findAllByIdIn(Set<ProjectId> projectId);

    List<Project> findAll();

}