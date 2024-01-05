package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.EventsPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toMap;

class PublishMissingDemandsService {

    private final ProjectAllocationsRepository projectAllocationsRepository;
    private final CreateHourlyDemandsSummaryService createHourlyDemandsSummaryService;
    private final EventsPublisher eventsPublisher;
    private final Clock clock;

    PublishMissingDemandsService(ProjectAllocationsRepository projectAllocationsRepository, CreateHourlyDemandsSummaryService createHourlyDemandsSummaryService, EventsPublisher eventsPublisher, Clock clock) {
        this.projectAllocationsRepository = projectAllocationsRepository;
        this.createHourlyDemandsSummaryService = createHourlyDemandsSummaryService;
        this.eventsPublisher = eventsPublisher;
        this.clock = clock;
    }

    @Scheduled(cron = "@hourly")
    void publish() {
        Instant when = clock.instant();
        List<ProjectAllocations> projectAllocations =
                projectAllocationsRepository.findAllContainingDate(when);
        NotSatisfiedDemands missingDemands = createHourlyDemandsSummaryService.create(projectAllocations, when);
        //add metadata to event
        //if needed call EventStore and translate multiple private events to a new published event
        eventsPublisher.publish(missingDemands);
    }


}

class CreateHourlyDemandsSummaryService {

    NotSatisfiedDemands create(List<ProjectAllocations> projectAllocations, Instant when) {
        return new NotSatisfiedDemands(projectAllocations
                .stream()
                .collect(toMap(ProjectAllocations::id, ProjectAllocations::missingDemands)), when);
    }
}



