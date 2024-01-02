package domaindrivers.smartschedule.planning.schedule;

import domaindrivers.smartschedule.planning.parallelization.Stage;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

class ScheduleBasedOnChosenResourcesAvailabilityCalculator {

    Map<String, TimeSlot> calculate(Calendars chosenResourcesCalendars, List<Stage> stages) {
        Map<String, TimeSlot> schedule = new HashMap<>();
        for (Stage stage : stages) {
            TimeSlot proposedSlot = findSlotForStage(chosenResourcesCalendars, stage);
            if (proposedSlot.equals(TimeSlot.empty())) {
                return Map.of();
            }
            schedule.put(stage.name(), proposedSlot);
        }
        return schedule;
    }

    private TimeSlot findSlotForStage(Calendars chosenResourcesCalendars, Stage stage) {
        List<TimeSlot> foundSlots = possibleSlots(chosenResourcesCalendars, stage);
        if (foundSlots.contains(TimeSlot.empty())) {
            return TimeSlot.empty();
        }
        TimeSlot commonSlotForAllResources = findCommonPartOfSlots(foundSlots);
        while (!isSlotLongEnoughForStage(stage, commonSlotForAllResources)) {
            commonSlotForAllResources = commonSlotForAllResources.stretch(Duration.ofDays(1));
        }
        return new TimeSlot(commonSlotForAllResources.from(), commonSlotForAllResources.from().plus(stage.duration()));
    }

    private boolean isSlotLongEnoughForStage(Stage stage, TimeSlot slot) {
        return slot.duration().compareTo(stage.duration()) >= 0;
    }

    private TimeSlot findCommonPartOfSlots(List<TimeSlot> foundSlots) {
        return foundSlots.stream()
                .reduce(TimeSlot::commonPartWith)
                .orElse(TimeSlot.empty());
    }

    private List<TimeSlot> possibleSlots(Calendars chosenResourcesCalendars, Stage stage) {
        return stage.resources()
                .stream()
                .map(resource ->
                        chosenResourcesCalendars
                                .get(resource)
                                .availableSlots()
                                .stream()
                                .sorted(comparing(TimeSlot::from))
                                .filter(slot -> isSlotLongEnoughForStage(stage, slot))
                                .findFirst()
                                .orElse(TimeSlot.empty()))
                .toList();
    }
}
