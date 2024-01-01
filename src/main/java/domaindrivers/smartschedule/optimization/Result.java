package domaindrivers.smartschedule.optimization;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record Result(Double profit, List<Item> chosenItems, Map<Item, Set<CapacityDimension>> itemToCapacities) {
    @Override
    public String toString() {
        return "Result{" +
                "profit=" + profit +
                ", chosenItems=" + chosenItems +
                '}';
    }
}
