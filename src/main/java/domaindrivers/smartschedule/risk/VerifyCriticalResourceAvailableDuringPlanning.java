package domaindrivers.smartschedule.risk;

import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Calendar;
import domaindrivers.smartschedule.planning.CriticalStagePlanned;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

class VerifyCriticalResourceAvailableDuringPlanning {

    private final AvailabilityFacade availabilityFacade;
    private final RiskPushNotification riskPushNotification;

    VerifyCriticalResourceAvailableDuringPlanning(AvailabilityFacade availabilityFacade, RiskPushNotification riskPushNotification) {
        this.availabilityFacade = availabilityFacade;
        this.riskPushNotification = riskPushNotification;
    }

    @Async
    @EventListener
    void handle(CriticalStagePlanned criticalStagePlanned) {
        if (criticalStagePlanned.criticalResource() == null) {
            return;
        }
        Calendar calendar = availabilityFacade.loadCalendar(criticalStagePlanned.criticalResource(), criticalStagePlanned.stageTimeSlot());
        if (!resourceIsAvailable(criticalStagePlanned.stageTimeSlot(), calendar)) {
            riskPushNotification.notifyAboutCriticalResourceNotAvailable(criticalStagePlanned.projectId(), criticalStagePlanned.criticalResource(), criticalStagePlanned.stageTimeSlot());
        }
    }

    private boolean resourceIsAvailable(TimeSlot timeSlot, Calendar calendar) {
        return calendar.availableSlots().stream().anyMatch(slot -> slot.equals(timeSlot));
    }
}
