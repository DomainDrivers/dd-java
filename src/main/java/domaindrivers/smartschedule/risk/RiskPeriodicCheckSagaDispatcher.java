package domaindrivers.smartschedule.risk;

import domaindrivers.smartschedule.allocation.*;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilitiesSummary;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityFinder;
import domaindrivers.smartschedule.allocation.cashflow.EarningsRecalculated;
import domaindrivers.smartschedule.availability.ResourceTakenOver;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RiskPeriodicCheckSagaDispatcher {

    private final RiskPeriodicCheckSagaRepository riskSagaRepository;
    private final PotentialTransfersService potentialTransfersService;
    private final CapabilityFinder capabilityFinder;
    private final RiskPushNotification riskPushNotification;
    private final Clock clock;

    public RiskPeriodicCheckSagaDispatcher(RiskPeriodicCheckSagaRepository stateRepository, PotentialTransfersService potentialTransfersService, CapabilityFinder capabilityFinder, RiskPushNotification riskPushNotification, Clock clock) {
        this.riskSagaRepository = stateRepository;
        this.potentialTransfersService = potentialTransfersService;
        this.capabilityFinder = capabilityFinder;
        this.riskPushNotification = riskPushNotification;
        this.clock = clock;
    }

    @Async
    @EventListener
        //remember about transactions spanning saga and potential external system
    void handle(ProjectAllocationsDemandsScheduled event) {
        RiskPeriodicCheckSaga found = riskSagaRepository.findByProjectId(event.projectId());
        if (found == null) {
            found = new RiskPeriodicCheckSaga(event.projectId(), event.missingDemands());
        }
        RiskPeriodicCheckSagaStep nextStep = found.handle(event);
        this.riskSagaRepository.save(found);
        perform(nextStep, found);
    }

    @Async
    @EventListener
        //remember about transactions spanning saga and potential external system
    void handle(EarningsRecalculated event) {
        RiskPeriodicCheckSaga found = riskSagaRepository.findByProjectId(event.projectId());
        if (found == null) {
            found = new RiskPeriodicCheckSaga(event.projectId(), event.earnings());
        }
        RiskPeriodicCheckSagaStep nextStep = found.handle(event);
        this.riskSagaRepository.save(found);
        perform(nextStep, found);
    }

    @Async
    @EventListener
        //remember about transactions spanning saga and potential external system
    void handle(ProjectAllocationScheduled event) {
        RiskPeriodicCheckSaga found = riskSagaRepository.findByProjectId(event.projectId());
        RiskPeriodicCheckSagaStep nextStep = found.handle(event);
        this.riskSagaRepository.save(found);
        perform(nextStep, found);
    }

    @Async
    @EventListener
        //remember about transactions spanning saga and potential external system
    void handle(CapabilitiesAllocated event) {
        RiskPeriodicCheckSaga saga = riskSagaRepository.findByProjectId(event.projectId());
        RiskPeriodicCheckSagaStep nextStep = saga.handle(event);
        this.riskSagaRepository.save(saga);
        perform(nextStep, saga);
    }

    @Async
    @EventListener
        //remember about transactions spanning saga and potential external system
    void handle(CapabilityReleased event) {
        RiskPeriodicCheckSaga saga = riskSagaRepository.findByProjectId(event.projectId());
        RiskPeriodicCheckSagaStep nextStep = saga.handle(event);
        this.riskSagaRepository.save(saga);
        perform(nextStep, saga);
    }

    @Async
    @EventListener
        //remember about transactions spanning saga and potential external system
    void handle(ResourceTakenOver event) {
        List<ProjectAllocationsId> interested = event.previousOwners()
                .stream()
                .map(owner -> new ProjectAllocationsId(owner.id()))
                .collect(Collectors.toList());
        //transaction per one saga
        riskSagaRepository.findByProjectIdIn(interested)
                .forEach(saga -> handle(saga, event));
    }

    void handle(RiskPeriodicCheckSaga saga, ResourceTakenOver event) {
        RiskPeriodicCheckSagaStep nextStep = saga.handle(event);
        this.riskSagaRepository.save(saga);
        perform(nextStep, saga);
    }

    @Scheduled(cron = "@weekly")
    void handleWeeklyCheck() {
        List<RiskPeriodicCheckSaga> sagas = riskSagaRepository.findAll();
        sagas.forEach(saga -> {
            RiskPeriodicCheckSagaStep nextStep = saga.handleWeeklyCheck(Instant.now(clock));
            this.riskSagaRepository.save(saga);
            perform(nextStep, saga);
        });
    }

    private void perform(RiskPeriodicCheckSagaStep nextStep, RiskPeriodicCheckSaga saga) {
        switch (nextStep) {
            case NOTIFY_ABOUT_DEMANDS_SATISFIED:
                riskPushNotification.notifyDemandsSatisfied(saga.projectId());
                break;
            case FIND_AVAILABLE:
                handleFindAvailableFor(saga);
                break;
            case DO_NOTHING:
                break;
            case SUGGEST_REPLACEMENT:
                handleSimulateRelocation(saga);
                break;
            case NOTIFY_ABOUT_POSSIBLE_RISK:
                riskPushNotification.notifyAboutPossibleRisk(saga.projectId());
                break;
        }
    }

    private void handleFindAvailableFor(RiskPeriodicCheckSaga saga) {
        Map<Demand, AllocatableCapabilitiesSummary> replacements = findAvailableReplacementsFor(saga.missingDemands());
        if (!replacements.values().stream().flatMap(ac -> ac.all().stream()).toList().isEmpty()) {
            riskPushNotification.notifyAboutAvailability(saga.projectId(), replacements);
        }
    }

    private void handleSimulateRelocation(RiskPeriodicCheckSaga saga) {
        findPossibleReplacements(saga.missingDemands())
                .forEach((demand, replacements) -> {
                            replacements.all().forEach(
                                    replacement -> {
                                        double profitAfterMovingCapabilities = potentialTransfersService.profitAfterMovingCapabilities(saga.projectId(), replacement, replacement.timeSlot());
                                        if (profitAfterMovingCapabilities > 0) {
                                            riskPushNotification.notifyProfitableRelocationFound(saga.projectId(), replacement.id());
                                        }
                                    }
                            );
                        }
                );
    }

    private Map<Demand, AllocatableCapabilitiesSummary> findAvailableReplacementsFor(Demands demands) {
        return demands
                .all()
                .stream()
                .collect(Collectors.toMap(
                        demand -> demand,
                        demand -> capabilityFinder.findAvailableCapabilities(demand.capability(), demand.slot())
                ));
    }

    private Map<Demand, AllocatableCapabilitiesSummary> findPossibleReplacements(Demands demands) {
        return demands
                .all()
                .stream()
                .collect(Collectors.toMap(
                        demand -> demand,
                        demand -> capabilityFinder.findCapabilities(demand.capability(), demand.slot())
                ));
    }

}

