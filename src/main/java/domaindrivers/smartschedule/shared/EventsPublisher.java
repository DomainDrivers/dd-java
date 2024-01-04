package domaindrivers.smartschedule.shared;


public interface EventsPublisher {
    void publishAfterCommit(Event event);
}


