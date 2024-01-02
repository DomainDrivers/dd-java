package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.shared.capability.Capability;

record Demand(Capability capability) {

    static Demand demandFor(Capability capability) {
        return new Demand(capability);
    }
}
