package domaindrivers.smartschedule.allocation;

class DomainProjectAllocationsScenarioTest extends ProjectAllocationsScenario {

    private final ProjectAllocationsDsl dsl = new DomainProjectAllocationsDriver();

    @Override
    ProjectAllocationsDsl dsl() {
        return dsl;
    }
}
