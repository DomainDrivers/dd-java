package domaindrivers.smartschedule.optimization;

public record Item(String name, double value, TotalWeight totalWeight) {

    boolean isWeightZero() {
        return totalWeight().components().isEmpty();
    }
}
