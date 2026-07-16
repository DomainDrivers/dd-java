package domaindrivers.smartschedule.availability;

import org.springframework.context.event.EventListener;

class ReleaseResourceHandler {

    private final AvailabilityFacade availabilityFacade;
    private final ProcessedMessages processedMessages;

    ReleaseResourceHandler(AvailabilityFacade availabilityFacade, ProcessedMessages processedMessages) {
        this.availabilityFacade = availabilityFacade;
        this.processedMessages = processedMessages;
    }

    @EventListener
    void handle(ReleaseResource command) {
        if (processedMessages.claim(command.eventId())) {
            availabilityFacade.release(command.resourceId(), command.timeSlot(), command.owner());
        }
    }
}
