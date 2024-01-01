package domaindrivers.smartschedule.simulation;

import domaindrivers.smartschedule.optimization.*;

import java.util.*;


public class SimulationFacade {

    private final OptimizationFacade optimizationFacade;

    public SimulationFacade(OptimizationFacade optimizationFacade) {
        this.optimizationFacade = optimizationFacade;
    }

    public Result whichProjectWithMissingDemandsIsMostProfitableToAllocateResourcesTo(List<SimulatedProject> projectsSimulations, SimulatedCapabilities totalCapability) {
        return optimizationFacade.calculate(toItems(projectsSimulations), toCapacity(totalCapability), Comparator.comparing(Item::value).reversed());
    }

    private TotalCapacity toCapacity(SimulatedCapabilities simulatedCapabilities) {
        List<AvailableResourceCapability> capabilities = simulatedCapabilities.capabilities();
        List<CapacityDimension> capacityDimensions = new ArrayList<>(capabilities);
        return new TotalCapacity(capacityDimensions);
    }

    private List<Item> toItems(List<SimulatedProject> projectsSimulations) {
        return projectsSimulations
                .stream()
                .map(this::toItem)
                .toList();
    }

    private Item toItem(SimulatedProject simulatedProject) {
        List<Demand> missingDemands = simulatedProject.missingDemands().all();
        List<WeightDimension> weights = new ArrayList<>(missingDemands);
        return new Item(simulatedProject.projectId().toString(), simulatedProject.calculateValue().doubleValue(), new TotalWeight(weights));
    }

}