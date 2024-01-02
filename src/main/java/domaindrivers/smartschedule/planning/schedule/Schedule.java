package domaindrivers.smartschedule.planning.schedule;

import domaindrivers.smartschedule.availability.Calendars;
import domaindrivers.smartschedule.planning.parallelization.ParallelStages;
import domaindrivers.smartschedule.planning.parallelization.ParallelStagesList;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record Schedule(Map<String, TimeSlot> dates) {

    public static Schedule none() {
        return new Schedule(Map.of());
    }

    public static Schedule basedOnStartDay(Instant startDate, ParallelStagesList parallelizedStages) {
        Map<String, TimeSlot> scheduleMap = new ScheduleBasedOnStartDayCalculator().calculate(startDate, parallelizedStages, Comparator.comparing(ParallelStages::print));
        return new Schedule(scheduleMap);
    }

    public static Schedule basedOnReferenceStageTimeSlot(Stage referenceStage, TimeSlot stageProposedTimeSlot, ParallelStagesList parallelizedStages) {
        Map<String, TimeSlot> scheduleMap = new ScheduleBasedOnReferenceStageCalculator().calculate(referenceStage, stageProposedTimeSlot, parallelizedStages, Comparator.comparing(ParallelStages::print));
        return new Schedule(scheduleMap);
    }

    public static Schedule basedOnChosenResourcesAvailability(Calendars chosenResourcesCalendars, List<Stage> stages) {
        Map<String, TimeSlot> schedule = new ScheduleBasedOnChosenResourcesAvailabilityCalculator().calculate(chosenResourcesCalendars, stages);
        return new Schedule(schedule);
    }


}

