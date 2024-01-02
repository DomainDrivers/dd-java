package domaindrivers.smartschedule.planning;


import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Calendars;
import domaindrivers.smartschedule.shared.ResourceName;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.transaction.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class PlanChosenResources {

    private final ProjectRepository projectRepository;
    private final AvailabilityFacade availabilityFacade;

    public PlanChosenResources(ProjectRepository projectRepository, AvailabilityFacade availabilityFacade) {
        this.projectRepository = projectRepository;
        this.availabilityFacade = availabilityFacade;
    }

    @Transactional
    public void defineResourcesWithinDates(ProjectId projectId, Set<ResourceName> chosenResources, TimeSlot timeBoundaries) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addChosenResources(new ChosenResources(chosenResources, timeBoundaries));
    }

    @Transactional
    public void adjustStagesToResourceAvailability(ProjectId projectId, TimeSlot timeBoundaries, Stage... stages) {
        Set<ResourceName> neededResources = neededResources(stages);
        Project project = projectRepository.findById(projectId).orElseThrow();
        defineResourcesWithinDates(projectId, neededResources, timeBoundaries);
        //TODO when availability is implemented
        Calendars neededResourcesCalendars = availabilityFacade.availabilitiesOfResources();
        Schedule schedule = createScheduleAdjustingToCalendars(neededResourcesCalendars, List.of(stages));
        project.addSchedule(schedule);
    }

    private Schedule createScheduleAdjustingToCalendars(Calendars neededResourcesCalendars, List<Stage> stages) {
        return Schedule.basedOnChosenResourcesAvailability(neededResourcesCalendars, stages);
    }

    private Set<ResourceName> neededResources(Stage[] stages) {
        return Arrays.stream(stages)
                .map(Stage::resources)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }


}

