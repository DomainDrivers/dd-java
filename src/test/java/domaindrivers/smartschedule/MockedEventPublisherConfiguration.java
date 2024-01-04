package domaindrivers.smartschedule;

import domaindrivers.smartschedule.shared.EventsPublisher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration(proxyBeanMethods = false)
public class MockedEventPublisherConfiguration {

    @MockBean
    EventsPublisher eventsPublisher;

}
