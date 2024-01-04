package domaindrivers.smartschedule.shared;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class EventingConfiguration {

    @Bean
    EventsPublisher eventsPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }
}


