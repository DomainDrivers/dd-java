package domaindrivers.smartschedule.simulation;

import java.util.*;


public class SimulationFacade {

    public Result whichProjectWithMissingDemandsIsMostProfitableToAllocateResourcesTo(List<SimulatedProject> projects, SimulatedCapabilities totalCapability) {
        return new Result(0.0, List.of(), Map.of());
    }

}

