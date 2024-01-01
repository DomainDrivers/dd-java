package domaindrivers.smartschedule.optimization;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record TotalWeight(List<WeightDimension> components) {

    static TotalWeight zero() {
        return new TotalWeight(List.of());
    }

    static TotalWeight of(WeightDimension... components) {
        return new TotalWeight(Arrays.asList(components));
    }

    public List<WeightDimension> components() {
        return new ArrayList<>(components);
    }
}

