package domaindrivers.smartschedule.simulation;


import java.math.BigDecimal;

record SimulatedProject(ProjectId projectId, BigDecimal earnings, Demands missingDemands) {

    public boolean allDemandsSatisfied() {
        return missingDemands.all().isEmpty();
    }
}
