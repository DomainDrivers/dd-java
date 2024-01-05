package domaindrivers.smartschedule.risk;


import domaindrivers.smartschedule.allocation.AllocationFacade;
import domaindrivers.smartschedule.allocation.PotentialTransfersService;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityFinder;
import domaindrivers.smartschedule.allocation.cashflow.CashFlowFacade;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.planning.PlanningFacade;
import domaindrivers.smartschedule.resource.ResourceFacade;
import domaindrivers.smartschedule.simulation.SimulationFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
class RiskConfiguration {

    @Bean
    RiskPeriodicCheckSagaDispatcher riskSagaDispatcher(RiskPeriodicCheckSagaRepository stateRepository, PotentialTransfersService potentialTransfersService, CapabilityFinder capabilityFinder, RiskPushNotification riskPushNotification, Clock clock) {
        return new RiskPeriodicCheckSagaDispatcher(stateRepository, potentialTransfersService, capabilityFinder, riskPushNotification, clock);
    }

    @Bean
    RiskPushNotification riskPushNotification() {
        return new RiskPushNotification();
    }

    @Bean
    VerifyEnoughDemandsDuringPlanning verifyEnoughDemandsDuringPlanning(PlanningFacade planningFacade, ResourceFacade resourceFacade, RiskPushNotification riskPushNotification) {
        return new VerifyEnoughDemandsDuringPlanning(planningFacade, new SimulationFacade(), resourceFacade, riskPushNotification);
    }

    @Bean
    VerifyCriticalResourceAvailableDuringPlanning verifyCriticalResourceAvailableDuringPlanning(AvailabilityFacade availabilityFacade, RiskPushNotification riskPushNotification) {
        return new VerifyCriticalResourceAvailableDuringPlanning(availabilityFacade, riskPushNotification);
    }

    @Bean
    VerifyNeededResourcesAvailableInTimeSlot verifyNeededResourcesAvailableInTimeSlot(AvailabilityFacade availabilityFacade, RiskPushNotification riskPushNotification) {
        return new VerifyNeededResourcesAvailableInTimeSlot(availabilityFacade, riskPushNotification);
    }

}
