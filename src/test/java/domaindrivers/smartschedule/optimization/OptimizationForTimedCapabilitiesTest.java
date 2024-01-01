package domaindrivers.smartschedule.optimization;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptimizationForTimedCapabilitiesTest {

    OptimizationFacade facade = new OptimizationFacade();

    @Test
    void nothingIsChosenWhenNoCapacitiesInTimeSlot() {
        //given
        TimeSlot june = TimeSlot.createMonthlyTimeSlotAtUTC(2020, 6);
        TimeSlot october = TimeSlot.createMonthlyTimeSlotAtUTC(2020, 10);

        List<Item> items = List.of(
                new Item("Item1", 100,
                        TotalWeight.of(new CapabilityTimedWeightDimension("COMMON SENSE", "Skill", june))),
                new Item("Item2", 100,
                        TotalWeight.of(new CapabilityTimedWeightDimension("THINKING", "Skill", june))));

        //when
        Result result = facade.calculate(items, TotalCapacity.of(
                new CapabilityTimedCapacityDimension("anna", "COMMON SENSE", "Skill", october)
        ));

        //then
        assertEquals(0, result.profit(), 0.0d);
        assertEquals(0, result.chosenItems().size());
    }

    @Test
    void mostProfitableItemIsChosen() {
        //given
        TimeSlot june = TimeSlot.createMonthlyTimeSlotAtUTC(2020, 6);

        List<Item> items = List.of(
                new Item("Item1", 200,
                        TotalWeight.of(new CapabilityTimedWeightDimension("COMMON SENSE", "Skill", june))),
                new Item("Item2", 100,
                        TotalWeight.of(new CapabilityTimedWeightDimension("THINKING", "Skill", june))));

        //when
        Result result = facade.calculate(items, TotalCapacity.of(
                new CapabilityTimedCapacityDimension("anna", "COMMON SENSE", "Skill", june)
        ));

        //then
        assertEquals(200, result.profit(), 0.0d);
        assertEquals(1, result.chosenItems().size());
    }

}