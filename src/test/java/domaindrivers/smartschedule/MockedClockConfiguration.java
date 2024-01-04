package domaindrivers.smartschedule;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;

@TestConfiguration(proxyBeanMethods = false)
public class MockedClockConfiguration {

    @MockBean
    Clock clock;

}
