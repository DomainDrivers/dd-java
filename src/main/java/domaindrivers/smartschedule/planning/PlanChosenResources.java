package domaindrivers.smartschedule.planning;


import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Calendars;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.EventsPublisher;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class PlanChosenResources {

    private final ProjectRepository projectRepository;
    private final AvailabilityFacade availabilityFacade;
    private final EventsPublisher eventsPublisher;
    private final Clock clock;

    public PlanChosenResources(ProjectRepository projectRepository, AvailabilityFacade availabilityFacade, EventsPublisher eventsPublisher, Clock clock) {
        this.projectRepository = projectRepository;
        this.availabilityFacade = availabilityFacade;
        this.eventsPublisher = eventsPublisher;
        this.clock = clock;
    }

    @Transactional
    public void defineResourcesWithinDates(ProjectId projectId, Set<ResourceId> chosenResources, TimeSlot timeBoundaries) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.addChosenResources(new ChosenResources(chosenResources, timeBoundaries));
        eventsPublisher.publish(new NeededResourcesChosen(projectId, chosenResources, timeBoundaries, clock.instant()));
    }

    @Transactional
    public void adjustStagesToResourceAvailability(ProjectId projectId, TimeSlot timeBoundaries, Stage... stages) {
        Set<ResourceId> neededResources = neededResources(stages);
        Project project = projectRepository.findById(projectId).orElseThrow();
        defineResourcesWithinDates(projectId, neededResources, timeBoundaries);
        Calendars neededResourcesCalendars = availabilityFacade.loadCalendars(neededResources, timeBoundaries);
        Schedule schedule = createScheduleAdjustingToCalendars(neededResourcesCalendars, List.of(stages));
        project.addSchedule(schedule);
    }

    private Schedule createScheduleAdjustingToCalendars(Calendars neededResourcesCalendars, List<Stage> stages) {
        return Schedule.basedOnChosenResourcesAvailability(neededResourcesCalendars, stages);
    }

    private Set<ResourceId> neededResources(Stage[] stages) {
        return Arrays.stream(stages)
                .map(Stage::resources)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }


}

