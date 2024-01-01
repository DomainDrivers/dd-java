package domaindrivers.smartschedule.simulation;


import java.util.List;
import java.util.Map;
import java.util.Set;

record Result(Double profit, List<SimulatedProject> chosenProjects,
              Map<SimulatedProject, Set<AvailableResourceCapability>> resourcesAllocatedToProjects) {
    @Override
    public String toString() {
        return "Result{" +
                "profit=" + profit +
                ", items=" + chosenProjects +
                '}';
    }
}
