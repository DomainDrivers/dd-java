package domaindrivers.smartschedule.simulation;

import java.util.*;


public class SimulationFacade {

    public Result whichProjectWithMissingDemandsIsMostProfitableToAllocateResourcesTo(List<SimulatedProject> projectsSimulations, SimulatedCapabilities totalCapability) {
        List<AvailableResourceCapability> list = totalCapability.capabilities();
        int capacitiesSize = list.size();
        double[] dp = new double[capacitiesSize + 1];
        List<SimulatedProject>[] chosenItemsList = new List[capacitiesSize + 1];
        List<Set<AvailableResourceCapability>> allocatedCapacitiesList = new ArrayList<>(capacitiesSize + 1);

        List<SimulatedProject> automaticallyIncludedItems = projectsSimulations
                .stream()
                .filter(SimulatedProject::allDemandsSatisfied)
                .toList();
        double guaranteedValue = automaticallyIncludedItems
                .stream()
                .mapToDouble(p -> p.earnings().doubleValue())
                .sum();

        for (int i = 0; i <= capacitiesSize; i++) {
            chosenItemsList[i] = new ArrayList<>();
            allocatedCapacitiesList.add(new HashSet<>());
        }

        List<AvailableResourceCapability> allAvailabilities = new ArrayList<>(list);
        Map<SimulatedProject, Set<AvailableResourceCapability>> itemToCapacitiesMap = new HashMap<>();

        for (SimulatedProject project : projectsSimulations.stream().sorted(Comparator.comparing(SimulatedProject::earnings).reversed()).toList()) {
            List<AvailableResourceCapability> chosenCapacities =
                    matchCapacities(project.missingDemands(), allAvailabilities);
            allAvailabilities.removeAll(chosenCapacities);

            if (chosenCapacities.isEmpty()) {
                continue;
            }

            double sumValue = project.earnings().doubleValue();
            int chosenCapacitiesCount = chosenCapacities.size();

            for (int j = capacitiesSize; j >= chosenCapacitiesCount; j--) {
                if (dp[j] < sumValue + dp[j - chosenCapacitiesCount]) {
                    dp[j] = sumValue + dp[j - chosenCapacitiesCount];

                    chosenItemsList[j] = new ArrayList<>(chosenItemsList[j - chosenCapacitiesCount]);
                    chosenItemsList[j].add(project);

                    allocatedCapacitiesList.get(j).addAll(chosenCapacities);
                }
            }
            itemToCapacitiesMap.put(project, new HashSet<>(chosenCapacities));
        }

        chosenItemsList[capacitiesSize].addAll(automaticallyIncludedItems);
        return new Result(dp[capacitiesSize] + guaranteedValue, chosenItemsList[capacitiesSize], itemToCapacitiesMap);
    }

    private List<AvailableResourceCapability> matchCapacities(Demands demands, List<AvailableResourceCapability> availableCapacities) {
        List<AvailableResourceCapability> result = new ArrayList<>();
        for (Demand singleDemand : demands.all()) {
            AvailableResourceCapability matchingCapacity = availableCapacities.
                    stream()
                    .filter(singleDemand::isSatisfiedBy)
                    .findFirst()
                    .orElse(null);

            if (matchingCapacity != null) {
                result.add(matchingCapacity);
            } else {
                return Collections.emptyList();
            }
        }
        return result;
    }

}