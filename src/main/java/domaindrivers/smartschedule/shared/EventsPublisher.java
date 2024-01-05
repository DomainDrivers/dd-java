package domaindrivers.smartschedule.shared;


public interface EventsPublisher {
    //remember about transactions scope
    void publish(PublishedEvent event);
}


