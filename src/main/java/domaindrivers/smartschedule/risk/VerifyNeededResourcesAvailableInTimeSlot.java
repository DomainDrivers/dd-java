package domaindrivers.smartschedule.risk;

import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Calendars;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.planning.NeededResourcesChosen;
import domaindrivers.smartschedule.planning.ProjectId;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.HashSet;
import java.util.Set;


class VerifyNeededResourcesAvailableInTimeSlot {

    private final AvailabilityFacade availabilityFacade;
    private final RiskPushNotification riskPushNotification;

    VerifyNeededResourcesAvailableInTimeSlot(AvailabilityFacade availabilityFacade, RiskPushNotification riskPushNotification) {
        this.availabilityFacade = availabilityFacade;
        this.riskPushNotification = riskPushNotification;
    }

    @Async
    @EventListener
    void handle(NeededResourcesChosen resourcesNeeded) {
        notifyAboutNotAvailableResources(resourcesNeeded.neededResources(), resourcesNeeded.timeSlot(), resourcesNeeded.projectId());
    }

    private void notifyAboutNotAvailableResources(Set<ResourceId> resourcedIds, TimeSlot timeSlot, ProjectId projectId) {
        Set<ResourceId> notAvailable = new HashSet<>();
        Calendars calendars = availabilityFacade.loadCalendars(resourcedIds, timeSlot);
        for (ResourceId resourceId : resourcedIds) {
            if (calendars.get(resourceId).availableSlots().stream().noneMatch(timeSlot::within)) {
                notAvailable.add(resourceId);
            }
        }
        if (!notAvailable.isEmpty()) {
            riskPushNotification.notifyAboutResourcesNotAvailable(projectId, notAvailable);
        }
    }
}
