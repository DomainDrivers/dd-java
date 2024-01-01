package domaindrivers.smartschedule.simulation;


import java.util.ArrayList;
import java.util.List;

record SimulatedCapabilities(List<AvailableResourceCapability> capabilities) {

    SimulatedCapabilities add(List<AvailableResourceCapability> newCapabilities) {
        List<AvailableResourceCapability> newAvailabilities = new ArrayList<>(capabilities);
        newAvailabilities.addAll(newCapabilities);
        return new SimulatedCapabilities(newAvailabilities);
    }

    SimulatedCapabilities add(AvailableResourceCapability newCapability) {
        return add(List.of(newCapability));
    }

}
