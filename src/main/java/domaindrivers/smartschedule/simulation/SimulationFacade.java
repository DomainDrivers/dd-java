package domaindrivers.smartschedule.simulation;

import domaindrivers.smartschedule.optimization.*;

import java.util.*;


public class SimulationFacade {

    private final OptimizationFacade optimizationFacade;

    public SimulationFacade(OptimizationFacade optimizationFacade) {
        this.optimizationFacade = optimizationFacade;
    }

    public double profitAfterBuyingNewCapability(List<SimulatedProject> projectsSimulations, SimulatedCapabilities capabilitiesWithoutNewOne, AdditionalPricedCapability newPricedCapability) {
        SimulatedCapabilities capabilitiesWithNewResource = capabilitiesWithoutNewOne.add(newPricedCapability.availableResourceCapability());
        Result resultWithout = optimizationFacade.calculate(toItems(projectsSimulations), toCapacity(capabilitiesWithoutNewOne), Comparator.comparing(Item::value).reversed());
        Result resultWith = optimizationFacade.calculate(toItems(projectsSimulations), toCapacity(capabilitiesWithNewResource), Comparator.comparing(Item::value).reversed());
        return (resultWith.profit() - newPricedCapability.value().doubleValue()) - resultWithout.profit();
    }

    public Result whatIsTheOptimalSetup(List<SimulatedProject> projectsSimulations, SimulatedCapabilities totalCapability) {
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