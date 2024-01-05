package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.shared.capability.Capability;

public record Demand(Capability capability) {

    public static Demand demandFor(Capability capability) {
        return new Demand(capability);
    }
}
