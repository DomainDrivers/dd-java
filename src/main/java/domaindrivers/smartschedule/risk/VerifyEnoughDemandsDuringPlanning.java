package domaindrivers.smartschedule.risk;

import domaindrivers.smartschedule.optimization.*;
import domaindrivers.smartschedule.planning.CapabilitiesDemanded;
import domaindrivers.smartschedule.planning.PlanningFacade;
import domaindrivers.smartschedule.planning.ProjectCard;
import domaindrivers.smartschedule.resource.ResourceFacade;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.simulation.*;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

class VerifyEnoughDemandsDuringPlanning {

    static final int SAME_ARBITRARY_VALUE_FOR_EVERY_PROJECT = 100;

    private final PlanningFacade planningFacade;
    private final SimulationFacade simulationFacade;
    private final ResourceFacade resourceFacade;
    private final RiskPushNotification riskPushNotification;

    VerifyEnoughDemandsDuringPlanning(PlanningFacade planningFacade, SimulationFacade simulationFacade, ResourceFacade resourceFacade, RiskPushNotification riskPushNotification) {
        this.planningFacade = planningFacade;
        this.simulationFacade = simulationFacade;
        this.resourceFacade = resourceFacade;
        this.riskPushNotification = riskPushNotification;
    }

    @Async
    @EventListener
    void handle(CapabilitiesDemanded capabilitiesDemanded) {
        List<ProjectCard> projectSummaries = planningFacade.loadAll();
        List<Capability> allCapabilities = resourceFacade.findAllCapabilities();
        if (notAbleToHandleAllProjectsGivenCapabilities(projectSummaries, allCapabilities)) {
            riskPushNotification.notifyAboutPossibleRiskDuringPlanning(capabilitiesDemanded.projectId(), capabilitiesDemanded.demands());
        }
    }

    private boolean notAbleToHandleAllProjectsGivenCapabilities(List<ProjectCard> projectSummaries, List<Capability> allCapabilities) {
        List<AvailableResourceCapability> capabilities = allCapabilities
                .stream()
                .map(cap -> new AvailableResourceCapability(UUID.randomUUID(), CapabilitySelector.canJustPerform(cap), TimeSlot.empty()))
                .toList();
        List<SimulatedProject> simulatedProjects = projectSummaries
                .stream()
                .map(this::createSamePriceSimulatedProject)
                .toList();
        Result result = simulationFacade.whatIsTheOptimalSetup(simulatedProjects, new SimulatedCapabilities(capabilities));
        return result.chosenItems().size() != projectSummaries.size();
    }

    private SimulatedProject createSamePriceSimulatedProject(ProjectCard card) {
        List<Demand> simulatedDemands = card.demands().all().stream().map(demand -> new Demand(demand.capability(), TimeSlot.empty())).toList();
        return new SimulatedProject(ProjectId.from(card.projectId().id()), () -> new BigDecimal(SAME_ARBITRARY_VALUE_FOR_EVERY_PROJECT), new Demands(simulatedDemands));
    }
}



