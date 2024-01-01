package domaindrivers.smartschedule.simulation;


import java.util.ArrayList;
import java.util.List;

public record SimulatedCapabilities(List<AvailableResourceCapability> capabilities) {

    public static SimulatedCapabilities none() {
        return new SimulatedCapabilities(List.of());
    }

    SimulatedCapabilities add(List<AvailableResourceCapability> newCapabilities) {
        List<AvailableResourceCapability> newAvailabilities = new ArrayList<>(capabilities);
        newAvailabilities.addAll(newCapabilities);
        return new SimulatedCapabilities(newAvailabilities);
    }

    SimulatedCapabilities add(AvailableResourceCapability newCapability) {
        return add(List.of(newCapability));
    }

}
