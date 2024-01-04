package domaindrivers.smartschedule.planning;


import domaindrivers.smartschedule.allocation.AllocationFacade;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.transaction.Transactional;

public class EditStageDateService {

    private final AllocationFacade allocationFacade;
    private final ProjectRepository projectRepository;

    public EditStageDateService(AllocationFacade allocationFacade, ProjectRepository projectRepository) {
        this.allocationFacade = allocationFacade;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public void editStageDate(ProjectId projectId, Stage stage, TimeSlot newSlot) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        Schedule schedule = project.getSchedule();
        //redefine schedule
        //for each stage in schedule
            //recreate allocation
            //reallocate chosen resources (or find equivalents)
            //start risk analysis


    }

}

