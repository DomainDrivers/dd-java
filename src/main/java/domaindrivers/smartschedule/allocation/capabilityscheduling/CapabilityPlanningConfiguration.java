package domaindrivers.smartschedule.allocation.capabilityscheduling;

import domaindrivers.smartschedule.availability.AvailabilityFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CapabilityPlanningConfiguration {

    @Bean
    CapabilityScheduler capabilityScheduler(AvailabilityFacade availabilityFacade, AllocatableCapabilityRepository allocatableResourceRepository) {
        return new CapabilityScheduler(availabilityFacade, allocatableResourceRepository);
    }

    @Bean
    CapabilityFinder capabilityFinder(AvailabilityFacade availabilityFacade, AllocatableCapabilityRepository allocatableResourceRepository) {
        return new CapabilityFinder(availabilityFacade, allocatableResourceRepository);
    }

}