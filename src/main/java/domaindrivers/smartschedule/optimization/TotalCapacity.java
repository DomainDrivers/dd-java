package domaindrivers.smartschedule.optimization;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public record TotalCapacity(List<CapacityDimension> capacities) {

    public static TotalCapacity of(CapacityDimension... capacities) {
        return new TotalCapacity(Arrays.asList(capacities));
    }

    public static TotalCapacity of(List<CapacityDimension> capacities) {
        return new TotalCapacity(capacities);
    }

    static TotalCapacity zero() {
        return new TotalCapacity(List.of());
    }

    int size() {
        return capacities.size();
    }

    public List<CapacityDimension> capacities() {
        return new ArrayList<>(capacities);
    }

    public TotalCapacity add(List<CapacityDimension> capacities) {
        List<CapacityDimension> newCapacities = new ArrayList<>(this.capacities);
        newCapacities.addAll(capacities);
        return new TotalCapacity(new ArrayList<>(newCapacities));
    }
}


