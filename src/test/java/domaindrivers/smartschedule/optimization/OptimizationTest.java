package domaindrivers.smartschedule.optimization;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OptimizationTest {

    OptimizationFacade facade = new OptimizationFacade();

    @Test
    void nothingIsChosenWhenNoCapacities() {
        //given
        List<Item> items = List.of(
                new Item("Item1", 100, TotalWeight.of(new CapabilityWeightDimension("COMMON SENSE", "Skill"))),
                new Item("Item2", 100, TotalWeight.of(new CapabilityWeightDimension("THINKING", "Skill"))));

        //when
        Result result = facade.calculate(items, TotalCapacity.zero());

        //then
        assertEquals(0, result.profit(), 0.0d);
        assertEquals(0, result.chosenItems().size());
    }

    @Test
    void everythingIsChosenWhenAllWeightsAreZero() {
        //given
        List<Item> items = List.of(
                new Item("Item1", 200, TotalWeight.zero()),
                new Item("Item2", 100, TotalWeight.zero()));

        //when
        Result result = facade.calculate(items, TotalCapacity.zero());

        //then
        assertEquals(300, result.profit(), 0.0d);
        assertEquals(2, result.chosenItems().size());
    }

    @Test
    void ifEnoughCapacityAllItemsAreChosen() {
        //given
        List<Item> items = List.of(
                new Item("Item1", 100, TotalWeight.of(new CapabilityWeightDimension("WEB DEVELOPMENT", "Skill"))),
                new Item("Item2", 300, TotalWeight.of(new CapabilityWeightDimension("WEB DEVELOPMENT", "Skill"))));
        CapacityDimension c1 = new CapabilityCapacityDimension("anna", "WEB DEVELOPMENT", "Skill");
        CapacityDimension c2 = new CapabilityCapacityDimension("zbyniu", "WEB DEVELOPMENT", "Skill");

        //when
        Result result = facade.calculate(items, TotalCapacity.of(c1, c2));

        //then
        assertEquals(400, result.profit(), 0.0d);
        assertEquals(2, result.chosenItems().size());
    }

    @Test
    void mostValuableItemsAreChosen() {
        //given
        Item item1 = new Item("Item1", 100, TotalWeight.of(new CapabilityWeightDimension("JAVA", "Skill")));
        Item item2 = new Item("Item2", 500, TotalWeight.of(new CapabilityWeightDimension("JAVA", "Skill")));
        Item item3 = new Item("Item3", 300, TotalWeight.of(new CapabilityWeightDimension("JAVA", "Skill")));
        CapacityDimension c1 = new CapabilityCapacityDimension("anna", "JAVA", "Skill");
        CapacityDimension c2 = new CapabilityCapacityDimension("zbyniu", "JAVA", "Skill");

        //when
        Result result = facade.calculate(List.of(item1, item2, item3), TotalCapacity.of(c1, c2));

        //then
        assertEquals(800, result.profit(), 0.0d);
        assertEquals(2, result.chosenItems().size());
        assertThat(result.itemToCapacities().get(item3)).hasSize(1);
        assertThat(result.itemToCapacities().get(item3)).containsAnyElementsOf(List.of(c1, c2));
        assertThat(result.itemToCapacities().get(item2)).hasSize(1);
        assertThat(result.itemToCapacities().get(item2)).containsAnyElementsOf(List.of(c1, c2));
        assertThat(result.itemToCapacities().get(item1)).isNull();
    }

}