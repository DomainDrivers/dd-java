package domaindrivers.smartschedule.optimization;

import java.util.*;

public class OptimizationFacade {

    public Result calculate(List<Item> items,
                            TotalCapacity totalCapacity) {
        int capacitiesSize = totalCapacity.size();
        double[] dp = new double[capacitiesSize + 1];
        List<Item>[] chosenItemsList = new List[capacitiesSize + 1];
        List<Set<CapacityDimension>> allocatedCapacitiesList = new ArrayList<>(capacitiesSize + 1);

        List<Item> automaticallyIncludedItems = items
                .stream()
                .filter(Item::isWeightZero)
                .toList();
        double guaranteedValue = automaticallyIncludedItems
                .stream()
                .mapToDouble(Item::value)
                .sum();

        for (int i = 0; i <= capacitiesSize; i++) {
            chosenItemsList[i] = new ArrayList<>();
            allocatedCapacitiesList.add(new HashSet<>());
        }

        List<CapacityDimension> allCapacities = totalCapacity.capacities();
        Map<Item, Set<CapacityDimension>> itemToCapacitiesMap = new HashMap<>();

        for (Item item : items.stream().sorted(Comparator.comparing(Item::value).reversed()).toList()) {
            List<CapacityDimension> chosenCapacities =
                    matchCapacities(item.totalWeight(), allCapacities);
            allCapacities.removeAll(chosenCapacities);

            if (chosenCapacities.isEmpty()) {
                continue;
            }

            double sumValue = item.value();
            int chosenCapacitiesCount = chosenCapacities.size();

            for (int j = capacitiesSize; j >= chosenCapacitiesCount; j--) {
                if (dp[j] < sumValue + dp[j - chosenCapacitiesCount]) {
                    dp[j] = sumValue + dp[j - chosenCapacitiesCount];

                    chosenItemsList[j] = new ArrayList<>(chosenItemsList[j - chosenCapacitiesCount]);
                    chosenItemsList[j].add(item);

                    allocatedCapacitiesList.get(j).addAll(chosenCapacities);
                }
            }
            itemToCapacitiesMap.put(item, new HashSet<>(chosenCapacities));
        }

        chosenItemsList[capacitiesSize].addAll(automaticallyIncludedItems);
        return new Result(dp[capacitiesSize] + guaranteedValue, chosenItemsList[capacitiesSize], itemToCapacitiesMap);
    }

    private List<CapacityDimension> matchCapacities(TotalWeight totalWeight, List<CapacityDimension> availableCapacities) {
        List<CapacityDimension> result = new ArrayList<>();
        for (WeightDimension weightComponent : totalWeight.components()) {
            CapacityDimension matchingCapacity = availableCapacities.
                    stream()
                    .filter(weightComponent::isSatisfiedBy)
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
