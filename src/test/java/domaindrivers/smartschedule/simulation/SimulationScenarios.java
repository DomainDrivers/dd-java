package domaindrivers.smartschedule.simulation;

import domaindrivers.smartschedule.optimization.OptimizationFacade;
import domaindrivers.smartschedule.optimization.Result;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static domaindrivers.smartschedule.shared.capability.Capability.skill;
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
        Result result = simulationFacade.whatIsTheOptimalSetup(simulatedProjects, simulatedAvailability);

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
        Result result = simulationFacade.whatIsTheOptimalSetup(simulatedProjects, simulatedAvailability);

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
        Result resultWithoutExtraResource = simulationFacade.whatIsTheOptimalSetup(simulatedProjects, simulatedAvailability);
        Result resultWithExtraResource = simulationFacade.whatIsTheOptimalSetup(simulatedProjects, simulatedAvailability.add(extraCapability));

        //then
        assertEquals(99d, resultWithoutExtraResource.profit());
        assertEquals(108d, resultWithExtraResource.profit());
    }

    @Test
    void picksOptimalProjectBasedOnReputation() {
        //given
        List<SimulatedProject> simulatedProjects = simulatedProjects()
                .withProject(PROJECT_1)
                .thatRequires(demandFor(skill("JAVA-MID"), JAN_1))
                .thatCanGenerateReputationLoss(100)
                .withProject(PROJECT_2)
                .thatRequires(demandFor(skill("JAVA-MID"), JAN_1))
                .thatCanGenerateReputationLoss(40)
                .build();

        //and there are
        SimulatedCapabilities simulatedAvailability = simulatedCapabilities()
                .withEmployee(STASZEK)
                .thatBrings(skill("JAVA-MID"))
                .thatIsAvailableAt(JAN_1)
                .build();


        //when
        Result result = simulationFacade.whatIsTheOptimalSetup(simulatedProjects, simulatedAvailability);

        //then
        assertEquals(PROJECT_1.toString(), result.chosenItems().get(0).name());
    }

    @Test
    void checkIfItPaysOffToPayForCapability() {
        //given
        List<SimulatedProject> simulatedProjects = simulatedProjects()
                .withProject(PROJECT_1)
                .thatRequires(demandFor(skill("JAVA-MID"), JAN_1))
                .thatCanEarn(valueOf(100))
                .withProject(PROJECT_2)
                .thatRequires(demandFor(skill("JAVA-MID"), JAN_1))
                .thatCanEarn(valueOf(40))
                .build();

        //and there are
        SimulatedCapabilities simulatedAvailability = simulatedCapabilities()
                .withEmployee(STASZEK)
                .thatBrings(skill("JAVA-MID"))
                .thatIsAvailableAt(JAN_1)
                .build();

        //and there are
        AdditionalPricedCapability slawek = new AdditionalPricedCapability(valueOf(9999), new AvailableResourceCapability(UUID.randomUUID(), skill("JAVA-MID"), JAN_1));
        AdditionalPricedCapability staszek = new AdditionalPricedCapability(valueOf(3), new AvailableResourceCapability(UUID.randomUUID(), skill("JAVA-MID"), JAN_1));

        //when
        double buyingSlawek = simulationFacade.profitAfterBuyingNewCapability(simulatedProjects, simulatedAvailability, slawek);
        double buyingStaszek = simulationFacade.profitAfterBuyingNewCapability(simulatedProjects, simulatedAvailability, staszek);

        //then
        assertEquals(-9959d, buyingSlawek); //we pay 9999 and get the project for 40
        assertEquals(37d, buyingStaszek); //we pay 3 and get the project for 40
    }

    SimulatedProjectsBuilder simulatedProjects() {
        return new SimulatedProjectsBuilder();
    }

    AvailableCapabilitiesBuilder simulatedCapabilities() {
        return new AvailableCapabilitiesBuilder();
    }

}