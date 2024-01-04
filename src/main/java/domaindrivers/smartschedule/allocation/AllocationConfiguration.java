package domaindrivers.smartschedule.allocation;


import domaindrivers.smartschedule.availability.AvailabilityFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class AllocationConfiguration {

    @Bean
    AllocationFacade allocationFacade(
            ProjectAllocationsRepository projectAllocationsRepository,
            AvailabilityFacade availabilityFacade,
            Clock clock) {
        return new AllocationFacade(projectAllocationsRepository, availabilityFacade, clock);
    }



}
