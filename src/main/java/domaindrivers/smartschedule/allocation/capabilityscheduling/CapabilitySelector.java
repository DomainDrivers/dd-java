package domaindrivers.smartschedule.allocation.capabilityscheduling;



import domaindrivers.smartschedule.shared.capability.Capability;

import java.io.Serializable;
import java.util.*;

class CapabilitySelector implements Serializable {

    public static CapabilitySelector canPerformOneOf(Set<Capability> capabilities) {
        return null;
    }

    public static CapabilitySelector canPerformAllAtTheTime(Set<Capability> beingAnAdmin) {
        return null;
    }

    public boolean canPerform(Capability capability ) {
        return false;
    }

    public boolean canPerform(Set<Capability> capabilities) {
        return false;
    }

}

