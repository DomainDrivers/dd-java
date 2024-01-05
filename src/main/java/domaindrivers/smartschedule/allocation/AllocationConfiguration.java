package domaindrivers.smartschedule.allocation;


import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityFinder;
import domaindrivers.smartschedule.allocation.cashflow.CashFlowFacade;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.shared.EventsPublisher;
import domaindrivers.smartschedule.simulation.SimulationFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class AllocationConfiguration {

    @Bean
    AllocationFacade allocationFacade(
            ProjectAllocationsRepository projectAllocationsRepository,
            AvailabilityFacade availabilityFacade,
            CapabilityFinder capabilityFinder,
            EventsPublisher eventsPublisher,
            Clock clock) {
        return new AllocationFacade(projectAllocationsRepository, availabilityFacade, capabilityFinder, eventsPublisher, clock);
    }

    @Bean
    PotentialTransfersService potentialTransfersService(
            CashFlowFacade cashFlowFacade, ProjectAllocationsRepository projectAllocationsRepository) {
        return new PotentialTransfersService(new SimulationFacade(), cashFlowFacade, projectAllocationsRepository);
    }


}
