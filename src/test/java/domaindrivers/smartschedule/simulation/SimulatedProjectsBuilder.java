package domaindrivers.smartschedule.simulation;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

class SimulatedProjectsBuilder {

    private ProjectId currentId;
    private final List<ProjectId> simulatedProjects = new ArrayList<>();
    private final Map<ProjectId, Demands> simulatedDemands = new HashMap<>();
    private final Map<ProjectId, Supplier<BigDecimal>> values = new HashMap<>();

    SimulatedProjectsBuilder withProject(ProjectId id) {
        this.currentId = id;
        simulatedProjects.add(id);
        return this;
    }

    SimulatedProjectsBuilder thatRequires(Demand... demands) {
        simulatedDemands.put(currentId, Demands.of(demands));
        return this;
    }

    SimulatedProjectsBuilder thatCanEarn(BigDecimal earnings) {
        this.values.put(currentId, () -> earnings);
        return this;
    }

    SimulatedProjectsBuilder thatCanGenerateReputationLoss(int factor) {
        this.values.put(currentId, () -> new BigDecimal(factor));
        return this;
    }

    List<SimulatedProject> build() {
        return simulatedProjects
                .stream()
                .map(id -> new SimulatedProject(id, values.get(id), simulatedDemands.get(id)))
                .toList();
    }
}
