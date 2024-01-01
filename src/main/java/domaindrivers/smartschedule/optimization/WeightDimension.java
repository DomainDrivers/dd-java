package domaindrivers.smartschedule.optimization;

public interface WeightDimension<T extends CapacityDimension> {
    boolean isSatisfiedBy(T capacityDimension);
}
