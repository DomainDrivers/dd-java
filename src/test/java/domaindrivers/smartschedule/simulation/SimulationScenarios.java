package domaindrivers.smartschedule.simulation;

import domaindrivers.smartschedule.optimization.OptimizationFacade;
import domaindrivers.smartschedule.optimization.Result;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static domaindrivers.smartschedule.simulation.Capability.skill;
import static domaindrivers.smartschedule.simulation.Demand.demandFor;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.*;

class SimulationScenarios {

    static final TimeSlot JAN_1 = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
    static final ProjectId PROJECT_1 = ProjectId.newOne();
    static final ProjectId PROJECT_2 = ProjectId.newOne();
    static final ProjectId PROJECT_3 = ProjectId.newOne();
    static final UUID STASZEK = UUID.randomUUID();
    static final UUID LEON = UUID.randomUUID();

    SimulationFacade simulationFacade = new SimulationFacade(new OptimizationFacade());

    @Test
    void picksOptimalProjectBasedOnEarnings() {
        //given
        List<SimulatedProject> simulatedProjects = simulatedProjects()
                .withProject(PROJECT_1)
                .thatRequires(demandFor(skill("JAVA-MID"), JAN_1))
                .thatCanEarn(valueOf(9))
                .withProject(PROJECT_2)
                .thatRequires(demandFor(skill("JAVA-MID"), JAN_1))
                .thatCanEarn(valueOf(99))
                .withProject(PROJECT_3)
                .thatRequires(demandFor(skill("JAVA-MID"), JAN_1))
                .thatCanEarn(valueOf(2))
                .build();

        //and there are
        SimulatedCapabilities simulatedAvailability = simulatedCapabilities()
                .withEmployee(STASZEK)
                .thatBrings(skill("JAVA-MID"))
                .thatIsAvailableAt(JAN_1)
                .withEmployee(LEON)
                .thatBrings(skill("JAVA-MID"))
                .thatIsAvailableAt(JAN_1)
                .build();

        //when
        Result result = simulationFacade.whichProjectWithMissingDemandsIsMostProfitableToAllocateResourcesTo(simulatedProjects, simulatedAvailability);

        //then
        assertEquals(108d, result.profit());
        assertEquals(2, result.chosenItems().size());
    }

    @Test
    void picksAllWhenEnoughCapabilities() {
        //given
        List<SimulatedProject> simulatedProjects = simulatedProjects()
                .withProject(PROJECT_1)
                .thatRequires(demandFor(skill("JAVA-MID"), JAN_1))
                .thatCanEarn(valueOf(99))
                .build();

        //and there are
        SimulatedCapabilities simulatedAvailability = simulatedCapabilities()
                .withEmployee(STASZEK)
                .thatBrings(skill("JAVA-MID"))
                .thatIsAvailableAt(JAN_1)
                .withEmployee(LEON)
                .thatBrings(skill("JAVA-MID"))
                .thatIsAvailableAt(JAN_1)
                .build();

        //when
        Result result = simulationFacade.whichProjectWithMissingDemandsIsMostProfitableToAllocateResourcesTo(simulatedProjects, simulatedAvailability);

        //then
        assertEquals(99d, result.profit());
        assertEquals(1, result.chosenItems().size());
    }

    @Test
    void canSimulateHavingExtraResources() {
        //given
        List<SimulatedProject> simulatedProjects = simulatedProjects()
                .withProject(PROJECT_1)
                .thatRequires(demandFor(skill("YT DRAMA COMMENTS"), JAN_1))
                .thatCanEarn(valueOf(9))
                .withProject(PROJECT_2)
                .thatRequires(demandFor(skill("YT DRAMA COMMENTS"), JAN_1))
                .thatCanEarn(valueOf(99))
                .build();

        //and there are
        SimulatedCapabilities simulatedAvailability = simulatedCapabilities()
                .withEmployee(STASZEK)
                .thatBrings(skill("YT DRAMA COMMENTS"))
                .thatIsAvailableAt(JAN_1)
                .build();

        //and there are
        AvailableResourceCapability extraCapability = new AvailableResourceCapability(UUID.randomUUID(), skill("YT DRAMA COMMENTS"), JAN_1);

        //when
        Result resultWithoutExtraResource = simulationFacade.whichProjectWithMissingDemandsIsMostProfitableToAllocateResourcesTo(simulatedProjects, simulatedAvailability);
        Result resultWithExtraResource = simulationFacade.whichProjectWithMissingDemandsIsMostProfitableToAllocateResourcesTo(simulatedProjects, simulatedAvailability.add(extraCapability));

        //then
        assertEquals(99d, resultWithoutExtraResource.profit());
        assertEquals(108d, resultWithExtraResource.profit());
    }

    SimulatedProjectsBuilder simulatedProjects() {
        return new SimulatedProjectsBuilder();
    }

    AvailableCapabilitiesBuilder simulatedCapabilities() {
        return new AvailableCapabilitiesBuilder();
    }

}