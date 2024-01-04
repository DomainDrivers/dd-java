package domaindrivers.smartschedule.planning;


import domaindrivers.smartschedule.allocation.AllocationFacade;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import jakarta.transaction.Transactional;

public class CreateProjectAllocations {

    private final AllocationFacade allocationFacade;
    private final ProjectRepository projectRepository;

    public CreateProjectAllocations(AllocationFacade allocationFacade, ProjectRepository projectRepository) {
        this.allocationFacade = allocationFacade;
        this.projectRepository = projectRepository;
    }

    //can react to ScheduleCalculated event
    @Transactional
    public void createProjectAllocations(ProjectId projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        Schedule schedule = project.getSchedule();
        //for each stage in schedule
            //create allocation
            //allocate chosen resources (or find equivalents)
            //start risk analysis


    }

}

